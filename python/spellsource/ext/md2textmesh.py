import html
import re
from itertools import chain
from urllib.parse import quote

from mistletoe import block_token, span_token
from mistletoe.base_renderer import BaseRenderer


class TextMeshRenderer(BaseRenderer):
    _DETAILS_SUMMARY_REGEXP = re.compile(r'</?details>|</?summary>')
    """
    HTML renderer class.

    See mistletoe.base_renderer module for more info.
    """

    def __init__(self, *extras):
        """
        Args:
            extras (list): allows subclasses to add even more custom tokens.
        """
        self._suppress_ptag_stack = [False]
        super().__init__(*chain((block_token.HTMLBlock, span_token.HTMLSpan), extras))
        # html.entities.html5 includes entitydefs not ending with ';',
        # CommonMark seems to hate them, so...
        self._stdlib_charref = html._charref
        _charref = re.compile(r'&(#[0-9]+;'
                              r'|#[xX][0-9a-fA-F]+;'
                              r'|[^\t\n\f <&#;]{1,32};)')
        html._charref = _charref

    def __exit__(self, *args):
        super().__exit__(*args)
        html._charref = self._stdlib_charref

    def render_to_plain(self, token):
        if hasattr(token, 'children'):
            return self.render_inner(token)
        return self.escape_html(token.content)

    def render_strong(self, token):
        template = '<b>{}</b>'
        return template.format(self.render_inner(token))

    def render_emphasis(self, token):
        template = '<i>{}</i>'
        return template.format(self.render_inner(token))

    def render_inline_code(self, token):
        template = '<indent=16>{}</indent>'
        inner = html.escape(token.children[0].content)
        return template.format(inner)

    def render_strikethrough(self, token):
        template = '<s>{}</s>'
        return template.format(self.render_inner(token))

    def render_image(self, token):
        # Cannot render images
        return '\n'
        # template = '<img src="{}" alt="{}"{} />'
        # render_func = self.render
        # self.render = self.render_to_plain
        # inner = self.render_inner(token)
        # self.render = render_func
        # if token.title:
        #     title = ' title="{}"'.format(self.escape_html(token.title))
        # else:
        #     title = ''
        # return template.format(token.src, inner, title)

    def render_link(self, token):
        template = '<color=#40d2f0><u><link="{target}">{inner}</link></u></color>'
        target = self.escape_url(token.target)
        inner = self.render_inner(token)
        return template.format(target=target, inner=inner)

    def render_auto_link(self, token):
        return self.render_link(token)

    def render_escape_sequence(self, token):
        return self.render_inner(token)

    def render_raw_text(self, token):
        return self.escape_html(token.content)

    def render_html_span(self, token):
        return token.content

    def render_heading(self, token):
        template = '<size=+{level}><b>{inner}</b></size>\n'
        inner = self.render_inner(token)
        level = min(7, max(7 - token.level, 0)) * 2
        return template.format(level=level, inner=inner)

    def render_quote(self, token):
        elements = ['<indent=16>']
        self._suppress_ptag_stack.append(False)
        elements.extend([self.render(child) for child in token.children])
        self._suppress_ptag_stack.pop()
        elements.append('</indent>')
        return '\n'.join(elements)

    def render_paragraph(self, token):
        if self._suppress_ptag_stack[-1]:
            return '{}'.format(self.render_inner(token))
        return '{}\n'.format(self.render_inner(token))

    def render_block_code(self, token):
        inner = html.escape(token.children[0].content)
        return inner
        # template = '<pre><code{attr}>{inner}</code></pre>'
        # if token.language:
        #     attr = ' class="{}"'.format('language-{}'.format(self.escape_html(token.language)))
        # else:
        #     attr = ''
        # inner = html.escape(token.children[0].content)
        # return template.format(attr=attr, inner=inner)

    def render_list(self, token):
        return ''.join([self.render(child) for child in token.children])

        # template = '<{tag}{attr}>\n{inner}\n</{tag}>'
        # if token.start is not None:
        #     tag = 'ol'
        #     attr = ' start="{}"'.format(token.start) if token.start != 1 else ''
        # else:
        #     tag = 'ul'
        #     attr = ''
        # self._suppress_ptag_stack.append(not token.loose)
        # inner = '\n'.join([self.render(child) for child in token.children])
        # self._suppress_ptag_stack.pop()
        # return template.format(tag=tag, attr=attr, inner=inner)

    def render_list_item(self, token):
        if len(token.children) == 0:
            return '<indent=16></indent>\n'
        self._suppress_ptag_stack.append(True)
        inner = ''.join([self.render(child) for child in token.children])
        self._suppress_ptag_stack.pop()
        # inner_template = '\n{}\n'
        # if self._suppress_ptag_stack[-1]:
        #     if token.children[0].__class__.__name__ == 'Paragraph':
        #         inner_template = inner_template[1:]
        #     if token.children[-1].__class__.__name__ == 'Paragraph':
        #         inner_template = inner_template[:-1]
        return ' - <indent=16>{}</indent>\n'.format(inner)

    def render_table(self, token):
        # This is actually gross and I wonder if there's a better way to do it.
        #
        # The primary difficulty seems to be passing down alignment options to
        # reach individual cells.
        template = '<table>\n{inner}</table>'
        if hasattr(token, 'header'):
            head_template = '<thead>\n{inner}</thead>\n'
            head_inner = self.render_table_row(token.header, is_header=True)
            head_rendered = head_template.format(inner=head_inner)
        else:
            head_rendered = ''
        body_template = '<tbody>\n{inner}</tbody>\n'
        body_inner = self.render_inner(token)
        body_rendered = body_template.format(inner=body_inner)
        return template.format(inner=head_rendered + body_rendered)

    def render_table_row(self, token, is_header=False):
        template = '<tr>\n{inner}</tr>\n'
        inner = ''.join([self.render_table_cell(child, is_header)
                         for child in token.children])
        return template.format(inner=inner)

    def render_table_cell(self, token, in_header=False):
        template = '<{tag}{attr}>{inner}</{tag}>\n'
        tag = 'th' if in_header else 'td'
        if token.align is None:
            align = 'left'
        elif token.align == 0:
            align = 'center'
        elif token.align == 1:
            align = 'right'
        attr = ' align="{}"'.format(align)
        inner = self.render_inner(token)
        return template.format(tag=tag, attr=attr, inner=inner)

    def render_thematic_break(self, token):
        return ''

    def render_line_break(self, token):
        return '\n' if token.soft else '<br />\n'

    def render_html_block(self, token):
        return TextMeshRenderer._DETAILS_SUMMARY_REGEXP.sub('', token.content)

    def render_document(self, token):
        self.footnotes.update(token.footnotes)
        inner = '\n'.join([self.render(child) for child in token.children])
        return '{}\n'.format(inner) if inner else ''

    @staticmethod
    def escape_html(raw):
        return raw
        # return html.escape(html.unescape(raw)).replace('&#x27;', "'")

    @staticmethod
    def escape_url(raw):
        """
        Escape urls to prevent code injection craziness. (Hopefully.)
        """
        return html.escape(quote(html.unescape(raw), safe='/#:()*?=%@+,&'))
