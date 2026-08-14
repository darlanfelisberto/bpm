package br.edu.iffar.showcase.e2e;

import com.microsoft.playwright.Locator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers /editor.xhtml (b:editor): typing, applying bold, saving via ajax
 * and checking that the displayed value matches. Saves TWICE on purpose -
 * the first edit hits editors that just loaded (always worked); the
 * second, done without reloading the page, is what caught a real bug in
 * editor.js (the listener that reinitializes Quill after an ajax was never
 * registered: the script checked "window.jsf", a name Jakarta Faces 4.x no
 * longer uses - the global object is now "window.faces" - and it also
 * checked this before faces.js had even loaded). Without this second round
 * of editing, the test would pass even with the bug present.
 */
class EditorIT extends PlaywrightSupport {

    @Test
    void editBoldSaveAndRemainEditableAfterAjax() {
        page.navigate(BASE_URL + "/editor.xhtml");

        typeAndSave("First edit", false);
        assertSavedContent("First edit", false);

        // Without reloading the page: the editor needs to remain
        // interactive (Quill reinitialized) after the previous "Save"
        // ajax.
        typeAndSave("Second edit after ajax", true);
        assertSavedContent("Second edit after ajax", true);
    }

    private void typeAndSave(String text, boolean bold) {
        Locator editorArea = page.locator(".box-editor-quill .ql-editor");
        editorArea.click();
        page.keyboard().press("Control+a");
        editorArea.pressSequentially(text);
        if (bold) {
            page.keyboard().press("Control+a");
            page.locator(".ql-toolbar .ql-bold").click();
        }
        // #resultado already exists in the DOM since the page's initial
        // load (only its content is swapped by ajax) - a waitFor() on it
        // would return immediately, without actually waiting for the ajax
        // to finish. Wait for the POST response instead.
        page.waitForResponse(response -> response.url().contains("/editor.xhtml") && "POST".equals(response.request().method()),
                () -> page.locator("#formEditor input[type=submit]").click());
    }

    private void assertSavedContent(String expectedText, boolean bold) {
        Locator resultado = page.locator("#resultado");
        // Quill saves a typed space as &nbsp; (U+00A0), not a regular space.
        String text = resultado.textContent().replace(' ', ' ');
        assertTrue(text.contains(expectedText),
                "saved content should contain \"" + expectedText + "\", but was: " + text);
        if (bold) {
            assertTrue(resultado.locator("strong").count() > 0,
                    "saved content should be bold (<strong>)");
        }
    }
}
