package br.edu.iffar.showcase.e2e;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Cobre /confirm.xhtml (b:confirm), nas duas formas de uso: como behavior
 * aninhado num h:commandLink, e como atributo puro data-box-confirm sem
 * componente Faces. As duas convivem na mesma página sem conflito.
 */
class ConfirmIT extends PlaywrightSuporte {

    @Test
    void confirmaViaBehaviorNoHCommandLink() {
        page.navigate(BASE_URL + "/confirm.xhtml");
        excluirEConfirmar("#formBehavior", "Item A");
    }

    @Test
    void confirmaViaAtributoDataBoxConfirm() {
        page.navigate(BASE_URL + "/confirm.xhtml");
        excluirEConfirmar("#formAtributo", "Item X");
    }

    @Test
    void cancelarMantemOItemNaLista() {
        page.navigate(BASE_URL + "/confirm.xhtml");

        Locator linha = page.locator("#formBehavior tr", new Page.LocatorOptions().setHasText("Item B"));
        linha.locator(".link-danger").click();

        Locator popup = page.locator(".box-confirmar-popup");
        popup.waitFor();
        popup.locator(".box-confirmar-nao").click();

        popup.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.DETACHED));
        assertEquals(1, page.locator("#formBehavior tr", new Page.LocatorOptions().setHasText("Item B")).count(),
                "cancelar não deveria excluir o item");
    }

    private void excluirEConfirmar(String formSeletor, String item) {
        Locator linha = page.locator(formSeletor + " tr", new Page.LocatorOptions().setHasText(item));
        linha.locator(".link-danger").click();

        Locator popup = page.locator(".box-confirmar-popup");
        popup.waitFor();
        assertTrue(popup.textContent().contains(item),
                "mensagem de confirmação deveria citar o item certo (" + item + ")");

        popup.locator(".box-confirmar-sim").click();

        Locator linhaAposExcluir = page.locator(formSeletor + " tr", new Page.LocatorOptions().setHasText(item));
        linhaAposExcluir.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.DETACHED));
        assertEquals(0, linhaAposExcluir.count());
    }
}
