package br.edu.iffar.showcase.e2e;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Playwright lifecycle (browser + a new tab per test) shared by all E2E
 * tests of the box components - extend instead of duplicating in every
 * new *IT.java.
 */
abstract class PlaywrightSupport {

    protected static final String BASE_URL = "http://localhost:9081";

    private static Playwright playwright;
    private static Browser browser;
    protected BrowserContext context;
    protected Page page;

    @BeforeAll
    static void startBrowser() {
        waitForServerReady();

        playwright = Playwright.create();
        // --no-sandbox: without it, Chromium fails silently in
        // container/CI environments with no user-namespace support for
        // the process's own sandbox (misleading symptom: looks like a
        // network error - ERR_CONNECTION_REFUSED - on the first
        // navigation, not a browser startup error).
        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setArgs(java.util.List.of("--no-sandbox")));
    }

    @AfterAll
    static void closeBrowser() {
        browser.close();
        playwright.close();
    }

    @BeforeEach
    void newTab() {
        // Fixed viewport (not Playwright's default): tests that compute
        // drag coordinates from bounding boxes (e.g. ScheduleIT) need a
        // predictable screen size.
        context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1280, 900));
        page = context.newPage();
    }

    @AfterEach
    void closeTab() {
        context.close();
    }

    // The CWWKF0011I log line the liberty-maven-plugin waits for before
    // handing control back to Maven (the "start" goal) means Liberty
    // finished initializing its features - in practice, there have still
    // been cases here where the HTTP port only started accepting
    // connections a moment after that, breaking Playwright's first
    // navigation with ERR_CONNECTION_REFUSED.
    private static void waitForServerReady() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder(URI.create(BASE_URL + "/index.xhtml"))
                .timeout(Duration.ofSeconds(2))
                .build();
        long deadline = System.currentTimeMillis() + Duration.ofSeconds(30).toMillis();
        while (System.currentTimeMillis() < deadline) {
            try {
                HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
                if (response.statusCode() == 200) {
                    return;
                }
            } catch (IOException | InterruptedException ignored) {
                // server not accepting connections yet - try again
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                fail("interrupted while waiting for the server to be ready");
            }
        }
        fail(BASE_URL + " did not respond 200 within 30s");
    }
}
