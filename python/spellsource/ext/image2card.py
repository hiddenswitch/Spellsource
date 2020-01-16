import os
import re
import logging
import dataclasses
from collections import deque
from copy import deepcopy
from json import loads, JSONEncoder
from mimetypes import MimeTypes
from typing import Union, Mapping, Iterable, Optional, Deque, List, Generator, Dict
from urllib.parse import urlparse, ParseResult

from autoboto.services import rekognition, s3
from autoboto.services.rekognition.shapes import Image, S3Object, DetectTextResponse, TextTypes, TextDetection
from botocore.exceptions import ClientError
from requests import get
from scrapy.http import TextResponse
from scrapy.spiders import Spider, Request

from ..ext.hearthcards import enrich_from_description

_MIME = MimeTypes()


class _Encoder(JSONEncoder):
    def default(self, o):
        try:
            boolable = bool(o)
        except TypeError:
            pass
        else:
            return bool(boolable)
            # Let the base class default method raise the TypeError
        return JSONEncoder.default(self, o)


_ENCODER = _Encoder()


class PageToImages(Iterable[str]):
    class _Spider(Spider):
        name = 'hearthpwn'

        def start_requests(self):
            for url in self.start_urls:
                yield Request(url=url, callback=self.parse, headers={
                    'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14) AppleWebKit/605.1.15 (KHTML, '
                                  'like Gecko) Version/12.0 Safari/605.1.15'
                })

        def parse(self, response: TextResponse):
            yield PageToImages.to_images(response=response)

    def __init__(self, *urls: str):
        super(PageToImages, self).__init__()
        self.urls = urls
        self._results = {}  # type: Dict[str, Iterable[str]]

    def __iter__(self):
        for url in self.urls:
            if url in self._results:
                for image_element in self._results[url]:
                    yield image_element
                continue
            results = self._results[url] = deque()
            for image_element in PageToImages.to_images(url=url):
                results.append(image_element)
                yield image_element

    @staticmethod
    def to_images(url: Optional[str] = None, request: Optional[Request] = None,
                  response: Optional[TextResponse] = None) -> Generator[str, None, None]:
        if url is not None:
            request = get(url)
        if request is not None:
            response = TextResponse(request.url, body=request.text, encoding='utf-8')

        selector_string = 'img'
        if 'hearthpwn.com/forums' in response.url:
            selector_string = '.forum-post-body ' + selector_string
        for img_selector in response.selector.css(selector_string):
            src = img_selector.css('::attr(src)').extract()[0]  # type: str
            width = img_selector.css('::attr(width)').extract()  # type: List[str]
            height = img_selector.css('::attr(height)').extract()  # type: List[str]
            if len(width) == 0 or len(height) == 0:
                # return any image without its width or height specified that appears in the post body
                yield src
            elif len(width) > 0 and len(height) > 0:
                if 0.68 <= int(width[0]) / int(height[0]) <= 0.78:
                    yield src


class Enricher(Iterable[Dict]):
    def __init__(self, *card_descs: Dict, hero_class: Optional[str] = 'ANY'):
        self.card_descs = card_descs
        self.hero_class = hero_class

    def __len__(self):
        return len(self.card_descs)

    def __iter__(self) -> Mapping:
        for card_desc in self.card_descs:
            yield enrich_from_description(card_dict=deepcopy(card_desc),
                                          description=card_desc['description'],
                                          card_type=card_desc['type'],
                                          card_attack=card_desc['baseAttack'] if 'baseAttack' in card_desc else None,
                                          card_health=card_desc['baseHp'] if 'baseHp' in card_desc else None,
                                          hero_class=self.hero_class)


class RekognitionGenerator(Iterable[DetectTextResponse]):
    _LOGGER = logging.getLogger('RekognitionGenerator')

    def __init__(self, *images: Union[str, Mapping, DetectTextResponse], bucket='minionate',
                 results_cache_prefix='image2card/results',
                 image_cache_prefix='image2card/images'):
        self.images = images
        self._results_cache_prefix = results_cache_prefix
        self._image_cache_prefix = image_cache_prefix
        self._bucket = bucket
        self._requests = {}

    def __len__(self) -> int:
        return len(self.images)

    def __iter__(self):
        self._s3 = s3.Client()
        try:
            region_name = self._s3.get_bucket_location(bucket=self._bucket).location_constraint
        except KeyError as workaround_key_error:
            region_name = workaround_key_error.args[0]
        self._rekognition = rekognition.Client(region_name=region_name)
        for image in self.images:
            # If we're given a detect text response or boto json, yield it immediately
            if isinstance(image, DetectTextResponse):
                yield image
                continue
            if isinstance(image, Mapping):
                yield DetectTextResponse.from_boto(image)
                continue

            # Check if we already have the exact URI result locally
            if image in self._requests:
                yield DetectTextResponse.from_boto(self._requests[image])
                continue

            uri = urlparse(image)  # type: ParseResult

            # Check if we've already processed and saved this URI to S3
            normalized_path = uri.path[1:] if uri.path[0] in (os.path.pathsep, '/') else uri.path
            result_key = os.path.join(self._results_cache_prefix, normalized_path + '.json')
            try:
                rekognition_res_s3 = self._s3.get_object(bucket=self._bucket,
                                                         key=result_key)
                RekognitionGenerator._LOGGER.info('Found cached result in S3 for URI %s' % (repr(uri)))
            except ClientError as ex:
                if ex.response['Error']['Code'] in ('404', 'NoSuchKey'):
                    rekognition_res_s3 = None
                else:
                    raise ex

            if rekognition_res_s3 is not None:
                # Return this json dict as the result
                assert rekognition_res_s3.content_type in ('application/json', 'text/json', 'text/plain')
                self._requests[image] = loads(rekognition_res_s3.body.read())
                yield DetectTextResponse.from_boto(self._requests[image])
                continue

            # Figure out if we need to upload
            if uri.scheme == 's3':
                # don't need to upload
                location = uri
            else:
                image_key = os.path.join(self._image_cache_prefix, normalized_path)
                # check if we already have the image
                try:
                    self._s3.head_object(bucket=self._bucket, key=image_key)
                    RekognitionGenerator._LOGGER.info('Found cached image in S3 for URI %s' % (repr(uri)))
                except ClientError as ex:
                    if ex.response['Error']['Code'] not in ('404', 'NoSuchKey'):
                        raise ex
                    if uri.scheme in ('http', 'https'):
                        # download and upload
                        with get(image) as image_res:
                            content_type = image_res.headers['Content-Type']
                            assert 'image' in content_type
                            image_bytes = image_res.content

                    elif uri.scheme in (None, 'file', ''):
                        # located on file system
                        uri_without_scheme = uri._replace(scheme='')
                        file_path = uri_without_scheme.geturl()
                        content_type, _ = _MIME.guess_type(file_path)
                        with open(file_path, 'rb') as file:
                            image_bytes = file.read()
                    else:
                        raise ValueError('scheme')

                    self._s3.put_object(bucket=self._bucket, key=image_key,
                                        content_type=content_type, body=image_bytes)
                location = image_key

            rekognition_res = self._rekognition.detect_text(
                image=Image(s3_object=S3Object(bucket=self._bucket, name=location)))
            self._requests[image] = dataclasses.asdict(rekognition_res)
            # Save the result to S3
            self._s3.put_object(bucket=self._bucket, key=result_key, content_type='application/json',
                                body=_ENCODER.encode(self._requests[image]))
            yield rekognition_res


class SpellsourceCardDescGenerator(Iterable[Dict]):
    _DIGIT_CONVERTERS = {  # type: Mapping[re.Pattern, str]
        re.compile(r'[Oo]'): '0',
        re.compile(r'[lLI|]'): '1',
        re.compile(r'[Ss]'): '5'
    }

    _LOGGER = logging.getLogger('SpellsourceCardDescGenerator')

    def __init__(self, *detect_text_responses: DetectTextResponse):
        self.detect_text_responses = detect_text_responses  # type: Iterable[DetectTextResponse]

    def __len__(self):
        return len(self.detect_text_responses)

    def __iter__(self):
        # The spatially first number reading top to bottom is typically the cost
        # The spatially first non-numeric text reading top to bottom is we encounter is the title
        # The last word spatially reading top to bottom could be a tribe.
        for detect_text in self.detect_text_responses:
            text_detections = sorted(detect_text.text_detections, key=lambda x: x.id)
            # strategy 1: use lines
            lines = [td for td in text_detections if td.type == TextTypes.LINE]  # type: List[TextDetection]

            if len(lines) < 2:
                SpellsourceCardDescGenerator._LOGGER.exception(
                    'Invalid card detected. Insufficient lines (%d) for request %s' % (
                        len(lines), dataclasses.asdict(detect_text)))
                continue

            # First line is the mana line
            mana_cost = SpellsourceCardDescGenerator._to_digits(lines[0].detected_text)

            # Second line is name
            name = lines[1].detected_text

            # try to pick apart the ending entities, looking for a number
            last_words = deque()  # type: Deque[str]
            digits_counted = 0
            lines_consumed = 0
            for i in range(-1, -4, -1):
                lines_consumed += 1
                if abs(i) > len(lines):
                    continue
                words_in_line = lines[i].detected_text.split(' ')
                for word in reversed(words_in_line):
                    last_words.appendleft(word)
                    if SpellsourceCardDescGenerator._to_digits(word) is not None:
                        digits_counted += 1
                if len(last_words) >= 3 or digits_counted == 2:
                    break

            card_desc = {}

            if digits_counted == 2:
                # check if we're in a situation where we have two digits
                card_desc['baseAttack'] = SpellsourceCardDescGenerator._to_digits(last_words[0])
                card_desc['baseHp'] = SpellsourceCardDescGenerator._to_digits(last_words[-1])
                if len(last_words) == 3:
                    # attack, tribe, health
                    race = last_words[1].upper()
                    if race == 'MECHANICAL':
                        race = 'MECH'
                    card_desc['race'] = race
                description_lines = lines[2:-lines_consumed]
                card_type = 'MINION'
            else:
                # Parse failure or otherwise
                card_type = 'SPELL'
                description_lines = lines[2:]

            description = ' '.join([line.detected_text for line in description_lines])
            card_desc.update({
                'name': name,
                'type': card_type,
                'baseManaCost': mana_cost,
                'description': description,
                'collectible': True,
                'set': 'CUSTOM',
                'fileFormatVersion': 1
            })
            yield card_desc

    @staticmethod
    def _to_digits(line: str) -> Optional[int]:
        # Don't permit numbers that are too long
        if len(line) > 2:
            return None
        for regexp, repl in SpellsourceCardDescGenerator._DIGIT_CONVERTERS.items():
            line = regexp.sub(repl, line)
        try:
            return int(line)
        except:
            return None
