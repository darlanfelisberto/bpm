package br.edu.iffar.showcase.e2e;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers /confirm.xhtml: the &lt;box-confirm&gt; custom element nested
 * inside an h:commandLink, with no Faces component/behavior involved.
 */
class ConfirmIT extends PlaywrightSupport {

    @Test
    void confirmsViaBoxConfirmElement() {
        page.navigate(BASE_URL + "/confirm.xhtml");
        deleteAndConfirm("Item A");
    }

    @Test
    void cancelingKeepsTheItemInTheList() {
        page.navigate(BASE_URL + "/confirm.xhtml");

        Locator row = page.locator("#form tr", new Page.LocatorOptions().setHasText("Item B"));
        row.locator(".link-danger").click();

        Locator popup = page.locator(".box-confirm-popup");
        popup.waitFor();
        popup.locator(".box-confirm-no").click();

        popup.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.DETACHED));
        assertEquals(1, page.locator("#form tr", new Page.LocatorOptions().setHasText("Item B")).count(),
                "canceling should not delete the item");
    }

    private void deleteAndConfirm(String item) {
        Locator row = page.locator("#form tr", new Page.LocatorOptions().setHasText(item));
        row.locator(".link-danger").click();

        Locator popup = page.locator(".box-confirm-popup");
        popup.waitFor();
        assertTrue(popup.textContent().contains(item),
                "confirmation message should mention the right item (" + item + ")");

        popup.locator(".box-confirm-yes").click();

        Locator rowAfterDelete = page.locator("#form tr", new Page.LocatorOptions().setHasText(item));
        rowAfterDelete.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.DETACHED));
        assertEquals(0, rowAfterDelete.count());
    }
}
