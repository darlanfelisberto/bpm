package br.edu.iffar.showcase.e2e;

import com.microsoft.playwright.Locator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Cobre /editor.xhtml (b:editor): digitar, aplicar negrito, salvar via ajax
 * e conferir que o valor exibido bate. Salva DUAS vezes de propósito - a
 * primeira alteração pega editores recém-carregados (sempre funcionou); a
 * segunda, feita sem recarregar a página, é o que pegou um bug real do
 * editor.js (o listener que reinicializa o Quill depois de um ajax nunca
 * era registrado: o script conferia "window.jsf", nome que o Jakarta
 * Faces 4.x não usa mais - o objeto global agora é "window.faces" - além de
 * conferir isso antes do faces.js sequer ter carregado). Sem essa segunda
 * rodada de edição, o teste passaria mesmo com o bug presente.
 */
class EditorIT extends PlaywrightSuporte {

    @Test
    void editarNegritoSalvarEContinuarEditavelAposAjax() {
        page.navigate(BASE_URL + "/editor.xhtml");

        digitarESalvar("Primeira edicao", false);
        assertConteudoSalvo("Primeira edicao", false);

        // Sem recarregar a página: o editor precisa continuar interativo
        // (Quill reinicializado) depois do ajax do "Salvar" anterior.
        digitarESalvar("Segunda edicao apos ajax", true);
        assertConteudoSalvo("Segunda edicao apos ajax", true);
    }

    private void digitarESalvar(String texto, boolean negrito) {
        Locator areaEditor = page.locator(".box-editor-quill .ql-editor");
        areaEditor.click();
        page.keyboard().press("Control+a");
        areaEditor.pressSequentially(texto);
        if (negrito) {
            page.keyboard().press("Control+a");
            page.locator(".ql-toolbar .ql-bold").click();
        }
        // #resultado já existe no DOM desde o carregamento inicial da
        // página (só o conteúdo é trocado pelo ajax) - um waitFor() nele
        // retornaria na hora, sem esperar o ajax terminar de verdade.
        // Espera a resposta do POST em vez disso.
        page.waitForResponse(resposta -> resposta.url().contains("/editor.xhtml") && "POST".equals(resposta.request().method()),
                () -> page.locator("#formEditor input[type=submit]").click());
    }

    private void assertConteudoSalvo(String textoEsperado, boolean negrito) {
        Locator resultado = page.locator("#resultado");
        // Quill grava espaço digitado como &nbsp; (U+00A0), não espaço comum.
        String texto = resultado.textContent().replace(' ', ' ');
        assertTrue(texto.contains(textoEsperado),
                "conteúdo salvo deveria conter \"" + textoEsperado + "\", mas era: " + texto);
        if (negrito) {
            assertTrue(resultado.locator("strong").count() > 0,
                    "conteúdo salvo deveria estar em negrito (<strong>)");
        }
    }
}
