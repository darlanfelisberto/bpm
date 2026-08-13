package br.edu.iffar.bpm.e2e;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Cobre o caminho principal de macroprocessos/list.xhtml contra a aplicacao
 * real (Open Liberty + Postgres local, subidos pelo liberty-maven-plugin e
 * flyway-maven-plugin nas fases pre-integration-test - ver bpm-app/pom.xml):
 * criar um macroprocesso usando o editor Quill (b:editor), verificar que a
 * formatacao (negrito) sobrevive ao round-trip salvar/exibir, e excluir via
 * o popup de confirmacao (data-box-confirm, sem componente Faces).
 *
 * Roda via "mvn verify -Pe2e" (profile e2e - ver bpm-app/pom.xml para o
 * motivo de nao estar no build normal). "mvn test"/"mvn install" sem o
 * profile nem compilam esta classe (fonte fora de src/test/java).
 */
class MacroprocessoEditorIT {

    private static final String BASE_URL = "http://localhost:9080";

    private static Playwright playwright;
    private static Browser browser;
    private BrowserContext context;
    private Page page;

    @BeforeAll
    static void iniciarBrowser() {
        esperarServidorPronto();

        playwright = Playwright.create();
        // --no-sandbox: sem isso o Chromium falha silenciosamente em
        // ambientes em container/CI sem suporte a user namespace pro
        // sandbox do proprio processo (sintoma enganoso: parece erro de
        // rede - ERR_CONNECTION_REFUSED - na primeira navegacao, nao um
        // erro de inicializacao do browser).
        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setArgs(List.of("--no-sandbox")));
    }

    // O log CWWKF0011I que o liberty-maven-plugin espera antes de devolver o
    // controle pro Maven (goal "start") indica que o Liberty terminou de
    // inicializar as features - na pratica, ainda houve casos aqui em que a
    // porta HTTP so aceitava conexao um instante depois disso, derrubando a
    // primeira navegacao do Playwright com ERR_CONNECTION_REFUSED. Mesmo
    // "until curl ...; do sleep ...; done" usado manualmente o resto desta
    // sessao pra evitar isso, só que dentro do proprio teste.
    private static void esperarServidorPronto() {
        HttpClient cliente = HttpClient.newHttpClient();
        HttpRequest requisicao = HttpRequest.newBuilder(URI.create(BASE_URL + "/index.xhtml"))
                .timeout(Duration.ofSeconds(2))
                .build();
        long limite = System.currentTimeMillis() + Duration.ofSeconds(30).toMillis();
        while (System.currentTimeMillis() < limite) {
            try {
                HttpResponse<Void> resposta = cliente.send(requisicao, HttpResponse.BodyHandlers.discarding());
                if (resposta.statusCode() == 200) {
                    return;
                }
            } catch (IOException | InterruptedException ignorado) {
                // servidor ainda nao aceitando conexoes - tenta de novo
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                fail("interrompido esperando o servidor ficar pronto");
            }
        }
        fail(BASE_URL + " nao respondeu 200 em 30s");
    }

    @AfterAll
    static void fecharBrowser() {
        browser.close();
        playwright.close();
    }

    @BeforeEach
    void novaAba() {
        context = browser.newContext();
        page = context.newPage();
    }

    @AfterEach
    void fecharAba() {
        context.close();
    }

    @Test
    void criarComEditorRicoEExcluirComConfirmacao() {
        String nome = "Teste E2E " + System.currentTimeMillis();

        page.navigate(BASE_URL + "/macroprocessos/list.xhtml");

        page.locator("#formNovo\\:nome").fill(nome);

        Locator areaEditor = page.locator(".box-editor-quill .ql-editor");
        areaEditor.click();
        areaEditor.pressSequentially("Objetivo em negrito");
        page.keyboard().press("Control+a");
        page.locator(".ql-toolbar .ql-bold").click();

        page.locator("#formNovo input[type=submit]").click();

        Locator linha = page.locator("tr", new Page.LocatorOptions().setHasText(nome));
        linha.waitFor();
        // Quill grava espaço digitado como &nbsp; ( ) no HTML, não " " -
        // normaliza antes de comparar.
        String textoNegrito = linha.locator("strong").textContent().replace(' ', ' ');
        assertTrue(textoNegrito.contains("Objetivo em negrito"),
                "objetivo deveria estar em negrito (<strong>) apos o round-trip salvar/exibir");

        linha.locator(".link-danger").click();

        Locator popup = page.locator(".box-confirmar-popup");
        popup.waitFor();
        assertTrue(popup.textContent().contains(nome),
                "mensagem de confirmacao deveria citar o nome do macroprocesso certo");

        popup.locator(".box-confirmar-sim").click();

        page.locator("tr", new Page.LocatorOptions().setHasText(nome)).waitFor(
                new Locator.WaitForOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.DETACHED));
        assertEquals(0, page.locator("tr", new Page.LocatorOptions().setHasText(nome)).count());
    }
}
