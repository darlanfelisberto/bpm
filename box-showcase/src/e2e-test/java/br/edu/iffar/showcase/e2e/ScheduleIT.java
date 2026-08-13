package br.edu.iffar.showcase.e2e;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Mouse;
import com.microsoft.playwright.options.BoundingBox;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Cobre /schedule.xhtml (b:schedule): clicar num evento existente ("click"),
 * e arrastar um evento pra outro dia ("move") - o segundo caso é o que
 * pegou um bug real (ver commit da correção): o FullCalendar manda a nova
 * data com offset de fuso (ex.: "2026-08-16T10:00:00-03:00") ao mover um
 * evento, formato que nem LocalDateTime.parse() nem LocalDate.parse()
 * aceitam sozinhos - só apareceu testando um evento com horário real
 * sendo arrastado, não com a seleção simples de "select".
 */
class ScheduleIT extends PlaywrightSuporte {

    @Test
    void clicarNoEventoReportaOId() {
        page.navigate(BASE_URL + "/schedule.xhtml");

        Locator evento = page.locator(".box-schedule-calendario").getByText("Reunião de equipe").first();
        evento.waitFor();

        // #ultima-acao ja existe no DOM desde o carregamento inicial - so
        // o conteudo troca pelo ajax. Um waitFor() nele retornaria na
        // hora, sem esperar o ajax terminar de verdade (mesma pegadinha
        // documentada em EditorIT). Espera a resposta do POST em vez disso.
        page.waitForResponse(resposta -> resposta.url().contains("/schedule.xhtml") && "POST".equals(resposta.request().method()),
                evento::click);

        Locator resultado = page.locator("#ultima-acao");
        assertTrue(resultado.textContent().startsWith("click: evento"),
                "clicar num evento deveria reportar \"click: evento <id>\", mas era: " + resultado.textContent());
    }

    @Test
    void arrastarEventoParaOutroDiaReportaNovasDatas() {
        page.navigate(BASE_URL + "/schedule.xhtml");
        // Visao semana: blocos de evento maiores/mais faceis de arrastar
        // com precisao que na visao mes (onde o evento e so uma pilula
        // fina de uma linha).
        page.locator(".fc-timeGridWeek-button").click();

        Locator evento = page.locator(".box-schedule-calendario").getByText("Reunião de equipe").first();
        evento.waitFor();
        // Sem isso o bounding box vem relativo à página inteira (não ao
        // viewport visível) - o evento fica bem abaixo da dobra (a página
        // tem bastante documentação acima do calendário), e os cliques do
        // mouse em coordenadas "fora da tela" simplesmente não acontecem.
        evento.scrollIntoViewIfNeeded();
        BoundingBox origemEvento = evento.boundingBox();

        // Acha a coluna do dia atual do evento e a coluna seguinte (destino
        // do arrasto) medindo as colunas de verdade, em vez de estimar um
        // deslocamento fixo em pixel - mais confiavel que so somar um
        // valor "tipico" de largura de coluna.
        Locator colunas = page.locator(".fc-timegrid-col[data-date]");
        int totalColunas = colunas.count();
        double centroEventoX = origemEvento.x + origemEvento.width / 2;
        int indiceAtual = -1;
        BoundingBox[] caixas = new BoundingBox[totalColunas];
        for (int i = 0; i < totalColunas; i++) {
            caixas[i] = colunas.nth(i).boundingBox();
            if (centroEventoX >= caixas[i].x && centroEventoX <= caixas[i].x + caixas[i].width) {
                indiceAtual = i;
            }
        }
        BoundingBox destino = caixas[indiceAtual + 1];
        double destinoX = destino.x + destino.width / 2;
        double destinoY = origemEvento.y + origemEvento.height / 2;

        page.mouse().move(centroEventoX, origemEvento.y + origemEvento.height / 2);
        page.mouse().down();
        page.mouse().move(destinoX, destinoY, new Mouse.MoveOptions().setSteps(5));
        page.mouse().up();

        Locator resultado = page.locator("#ultima-acao");
        resultado.getByText("move:").waitFor();
        String texto = resultado.textContent();
        assertTrue(texto.startsWith("move: evento"),
                "arrastar um evento deveria reportar \"move: evento <id> para <inicio> até <fim>\", mas era: " + texto);
        assertTrue(texto.contains("10:00") && texto.contains("11:00"),
                "mover pra outro dia não deveria mudar o horário (10:00-11:00), mas era: " + texto);
    }
}
