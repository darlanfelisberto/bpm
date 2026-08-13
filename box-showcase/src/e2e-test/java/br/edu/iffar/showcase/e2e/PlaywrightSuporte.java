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
 * Ciclo de vida do Playwright (browser + aba nova por teste) compartilhado
 * por todos os testes E2E dos componentes do box - estender em vez de
 * duplicar em cada *IT.java novo.
 */
abstract class PlaywrightSuporte {

    protected static final String BASE_URL = "http://localhost:9081";

    private static Playwright playwright;
    private static Browser browser;
    protected BrowserContext context;
    protected Page page;

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
                new BrowserType.LaunchOptions().setArgs(java.util.List.of("--no-sandbox")));
    }

    @AfterAll
    static void fecharBrowser() {
        browser.close();
        playwright.close();
    }

    @BeforeEach
    void novaAba() {
        // Viewport fixo (nao o default do Playwright): testes que calculam
        // coordenadas de drag a partir de bounding boxes (ex.: ScheduleIT)
        // precisam de um tamanho de tela previsivel.
        context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1280, 900));
        page = context.newPage();
    }

    @AfterEach
    void fecharAba() {
        context.close();
    }

    // O log CWWKF0011I que o liberty-maven-plugin espera antes de devolver o
    // controle pro Maven (goal "start") indica que o Liberty terminou de
    // inicializar as features - na pratica, ainda houve casos aqui em que a
    // porta HTTP so aceitava conexao um instante depois disso, derrubando a
    // primeira navegacao do Playwright com ERR_CONNECTION_REFUSED.
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
}
