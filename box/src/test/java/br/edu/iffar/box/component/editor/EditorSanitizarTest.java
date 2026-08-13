package br.edu.iffar.box.component;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testa Editor.sanitizar() - o unico ponto de defesa contra XSS armazenado
 * no HTML submetido pelo b:editor (o cliente e so Quill/JS, um POST forjado
 * pode mandar qualquer coisa direto pro servidor, sem passar pela UI).
 */
class EditorSanitizarTest {

    @Test
    void valorNuloPermanceNulo() {
        assertNull(Editor.sanitizar(null));
    }

    @Test
    void formatacaoBasicaSobrevive() {
        String html = "<p><strong>negrito</strong> <em>italico</em> <u>sublinhado</u> <s>tachado</s></p>";
        assertEquals(html, Editor.sanitizar(html));
    }

    @Test
    void scriptEhRemovidoPorCompleto() {
        String resultado = Editor.sanitizar("<p>ok</p><script>alert(1)</script>");
        assertFalse(resultado.contains("script"));
        assertFalse(resultado.contains("alert"));
    }

    @Test
    void atributoOnErrorEhRemovido() {
        String resultado = Editor.sanitizar("<img src=\"x\" onerror=\"alert(1)\">");
        assertFalse(resultado.contains("onerror"));
        assertFalse(resultado.contains("alert"));
    }

    @Test
    void linkComProtocoloJavascriptPerdeOHref() {
        String resultado = Editor.sanitizar("<a href=\"javascript:alert(1)\">clique</a>");
        assertFalse(resultado.contains("javascript:"));
        assertFalse(resultado.contains("href"));
        assertTrue(resultado.contains("clique"));
    }

    @Test
    void linkHttpsForcaTargetBlankERelSeguro() {
        String resultado = Editor.sanitizar("<a href=\"https://exemplo.com\" target=\"_self\">ir</a>");
        assertTrue(resultado.contains("href=\"https://exemplo.com\""));
        assertTrue(resultado.contains("target=\"_blank\""));
        assertTrue(resultado.contains("rel=\"noopener noreferrer\""));
    }

    @Test
    void imagemComDataUriSobrevive() {
        String html = "<img src=\"data:image/png;base64,iVBORw0KGgo=\">";
        assertEquals(html, Editor.sanitizar(html));
    }

    @Test
    void iframeEhRemovidoPorCompleto() {
        String resultado = Editor.sanitizar("<iframe src=\"https://exemplo.com\"></iframe>");
        assertFalse(resultado.contains("iframe"));
    }

    @Test
    void corDeFonteEmHexSobrevive() {
        String html = "<span style=\"color: #e63946\">texto</span>";
        assertEquals(html, Editor.sanitizar(html));
    }

    @Test
    void corDeFundoEmRgbSobrevive() {
        String html = "<span style=\"background-color: rgb(230, 57, 70)\">texto</span>";
        assertEquals(html, Editor.sanitizar(html));
    }

    @Test
    void negritoComCorMantemStyleNaPropriaTag() {
        // Quill aplica cor direto na tag de negrito quando combinada com
        // negrito, nao so em <span> (ver comentario no SAFELIST).
        String html = "<strong style=\"color: #ff0000\">texto</strong>";
        assertEquals(html, Editor.sanitizar(html));
    }

    @Test
    void estiloComPropriedadePerigosaEhRemovido() {
        String resultado = Editor.sanitizar(
                "<span style=\"background-image:url(http://evil.example/roubo);color:red\">teste</span>");
        assertFalse(resultado.contains("style"));
        assertFalse(resultado.contains("evil.example"));
        assertTrue(resultado.contains("teste"));
    }

    @Test
    void estiloComPosicaoFixaEhRemovido() {
        String resultado = Editor.sanitizar(
                "<strong style=\"color:red;position:fixed;top:0;left:0\">evil</strong>");
        assertFalse(resultado.contains("style"));
        assertTrue(resultado.contains("evil"));
    }

    @Test
    void classesDeFonteTamanhoAlinhamentoERecuoSobrevivem() {
        String html = "<p class=\"ql-align-center ql-indent-1\">"
                + "<span class=\"ql-font-georgia ql-size-large\">texto</span></p>";
        assertEquals(html, Editor.sanitizar(html));
    }

    @Test
    void listasCabecalhosCitacaoECodigoSobrevivem() {
        String html = "<h2>titulo</h2>"
                + "<blockquote>citacao</blockquote>"
                + "<pre data-language=\"plain\">codigo();</pre>"
                + "<ol><li>um</li></ol><ul><li>dois</li></ul>";
        assertEquals(html, Editor.sanitizar(html));
    }

    @Test
    void subESobrescritoSobrevivem() {
        String html = "<p>x<sub>2</sub> e y<sup>3</sup></p>";
        assertEquals(html, Editor.sanitizar(html));
    }

    @Test
    void divEIframeForaDaListaDePermissaoSaoDesempacotadosOuRemovidos() {
        // div nao esta na lista de permissao: Jsoup desempacota (mantem o
        // texto de dentro, remove so a tag) - garante que nao sobra div
        // nenhuma no resultado.
        String resultado = Editor.sanitizar("<div class=\"qualquer\">texto</div>");
        assertFalse(resultado.contains("<div"));
        assertTrue(resultado.contains("texto"));
    }
}
