import os
from json import loads, dumps
from mimetypes import MimeTypes
from typing import Sequence
from urllib.parse import urlparse, ParseResult

from autoboto.services import rekognition, s3
from autoboto.services.rekognition.shapes import Image, S3Object, DetectTextResponse
from botocore.exceptions import ClientError
from requests import get

_MIME = MimeTypes()


class RekognitionGenerator(object):
    def __init__(self, *image_uris: Sequence[str], bucket='minionate', results_cache_prefix='image2card/results',
                 image_cache_prefix='image2card/images'):
        self.image_uris = image_uris
        self._results_cache_prefix = results_cache_prefix


        self._bucket = bucket
        self._requests = {}
        self._image_cache_prefix = image_cache_prefix

    def __len__(self) -> int:
        return len(self.image_uris)

    def __del__(self):
        if self._s3 is not None:
            self._s3.close()

    def __iter__(self) -> DetectTextResponse:
        self._s3 = s3.Client()
        try:
            region_name = self._s3.get_bucket_location(bucket=self._bucket).location_constraint
        except KeyError as workaround_key_error:
            region_name = workaround_key_error.args[0]
        self._rekognition = rekognition.Client(region_name=region_name)
        for image_uri in self.image_uris:
            # Check if we already have the exact URI result locally
            if image_uri in self._requests:
                yield DetectTextResponse.from_boto_dict(self._requests[image_uri])
                continue

            uri = urlparse(image_uri)  # type: ParseResult

            # Check if we've already processed and saved this URI to S3
            normalized_path = uri.path[1:] if uri.path[0] in (os.path.pathsep, '/') else uri.path
            result_key = os.path.join(self._results_cache_prefix, normalized_path + '.json')
            try:
                rekognition_res_s3 = self._s3.get_object(bucket=self._bucket,
                                                         key=result_key)
            except ClientError as ex:
                if ex.response['Error']['Code'] in ('404', 'NoSuchKey'):
                    rekognition_res_s3 = None
                else:
                    raise ex

            if rekognition_res_s3 is not None:
                # Return this json dict as the result
                assert rekognition_res_s3.content_type in ('application/json', 'text/json', 'text/plain')
                self._requests[image_uri] = loads(rekognition_res_s3.body.read())
                yield DetectTextResponse.from_boto_dict(self._requests[image_uri])
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
                except ClientError as ex:
                    if ex.response['Error']['Code'] not in ('404', 'NoSuchKey'):
                        raise ex
                    if uri.scheme in ('http', 'https'):
                        # download and upload
                        with get(image_uri) as image_res:
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
            self._requests[image_uri] = rekognition_res.to_boto_dict()
            # Save the result to S3
            self._s3.put_object(bucket=self._bucket, key=result_key, content_type='application/json',
                                body=dumps(self._requests[image_uri]))
            yield rekognition_res
