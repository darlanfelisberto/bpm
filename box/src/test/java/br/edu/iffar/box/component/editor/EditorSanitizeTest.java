package br.edu.iffar.box.component.editor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests Editor.sanitize() - the only line of defense against stored XSS in
 * the HTML submitted by b:editor (the client is just Quill/JS, a forged
 * POST can send anything straight to the server, without going through
 * the UI).
 */
class EditorSanitizeTest {

    @Test
    void nullValueStaysNull() {
        assertNull(Editor.sanitize(null));
    }

    @Test
    void basicFormattingSurvives() {
        String html = "<p><strong>bold</strong> <em>italic</em> <u>underline</u> <s>strikethrough</s></p>";
        assertEquals(html, Editor.sanitize(html));
    }

    @Test
    void scriptTagIsFullyRemoved() {
        String result = Editor.sanitize("<p>ok</p><script>alert(1)</script>");
        assertFalse(result.contains("script"));
        assertFalse(result.contains("alert"));
    }

    @Test
    void onErrorAttributeIsRemoved() {
        String result = Editor.sanitize("<img src=\"x\" onerror=\"alert(1)\">");
        assertFalse(result.contains("onerror"));
        assertFalse(result.contains("alert"));
    }

    @Test
    void javascriptProtocolLinkLosesHref() {
        String result = Editor.sanitize("<a href=\"javascript:alert(1)\">click</a>");
        assertFalse(result.contains("javascript:"));
        assertFalse(result.contains("href"));
        assertTrue(result.contains("click"));
    }

    @Test
    void httpsLinkForcesTargetBlankAndSafeRel() {
        String result = Editor.sanitize("<a href=\"https://example.com\" target=\"_self\">go</a>");
        assertTrue(result.contains("href=\"https://example.com\""));
        assertTrue(result.contains("target=\"_blank\""));
        assertTrue(result.contains("rel=\"noopener noreferrer\""));
    }

    @Test
    void dataUriImageSurvives() {
        String html = "<img src=\"data:image/png;base64,iVBORw0KGgo=\">";
        assertEquals(html, Editor.sanitize(html));
    }

    @Test
    void iframeIsFullyRemoved() {
        String result = Editor.sanitize("<iframe src=\"https://example.com\"></iframe>");
        assertFalse(result.contains("iframe"));
    }

    @Test
    void hexFontColorSurvives() {
        String html = "<span style=\"color: #e63946\">text</span>";
        assertEquals(html, Editor.sanitize(html));
    }

    @Test
    void rgbBackgroundColorSurvives() {
        String html = "<span style=\"background-color: rgb(230, 57, 70)\">text</span>";
        assertEquals(html, Editor.sanitize(html));
    }

    @Test
    void boldWithColorKeepsStyleOnSameTag() {
        // Quill applies color directly on the bold tag when combined with
        // bold, not only on <span> (see comment on SAFELIST).
        String html = "<strong style=\"color: #ff0000\">text</strong>";
        assertEquals(html, Editor.sanitize(html));
    }

    @Test
    void styleWithDangerousPropertyIsRemoved() {
        String result = Editor.sanitize(
                "<span style=\"background-image:url(http://evil.example/steal);color:red\">test</span>");
        assertFalse(result.contains("style"));
        assertFalse(result.contains("evil.example"));
        assertTrue(result.contains("test"));
    }

    @Test
    void styleWithFixedPositionIsRemoved() {
        String result = Editor.sanitize(
                "<strong style=\"color:red;position:fixed;top:0;left:0\">evil</strong>");
        assertFalse(result.contains("style"));
        assertTrue(result.contains("evil"));
    }

    @Test
    void fontSizeAlignmentAndIndentClassesSurvive() {
        String html = "<p class=\"ql-align-center ql-indent-1\">"
                + "<span class=\"ql-font-georgia ql-size-large\">text</span></p>";
        assertEquals(html, Editor.sanitize(html));
    }

    @Test
    void listsHeadingsBlockquoteAndCodeSurvive() {
        String html = "<h2>title</h2>"
                + "<blockquote>quote</blockquote>"
                + "<pre data-language=\"plain\">code();</pre>"
                + "<ol><li>one</li></ol><ul><li>two</li></ul>";
        assertEquals(html, Editor.sanitize(html));
    }

    @Test
    void subAndSuperscriptSurvive() {
        String html = "<p>x<sub>2</sub> and y<sup>3</sup></p>";
        assertEquals(html, Editor.sanitize(html));
    }

    @Test
    void divAndIframeOutsideAllowlistAreUnwrappedOrRemoved() {
        // div is not in the allowlist: Jsoup unwraps it (keeps the text
        // inside, removes only the tag) - ensures no div is left in the
        // result.
        String result = Editor.sanitize("<div class=\"whatever\">text</div>");
        assertFalse(result.contains("<div"));
        assertTrue(result.contains("text"));
    }
}
