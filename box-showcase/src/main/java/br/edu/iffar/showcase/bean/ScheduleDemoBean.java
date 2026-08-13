package br.edu.iffar.showcase.bean;

import br.edu.iffar.box.component.schedule.Schedule;
import br.edu.iffar.box.component.schedule.ScheduleEvent;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.event.AjaxBehaviorEvent;
import jakarta.inject.Named;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Estado da página de demonstração do b:schedule (/schedule.xhtml).
 * SessionScoped pelo mesmo motivo dos outros beans de demo: sobrevive a
 * reload da página, útil pros testes E2E inspecionarem o estado.
 */
@Named
@SessionScoped
public class ScheduleDemoBean implements Serializable {

    private List<ScheduleEvent> eventos;
    private String ultimaAcao = "";

    @PostConstruct
    void iniciar() {
        LocalDate hoje = LocalDate.now();
        eventos = new ArrayList<>();
        eventos.add(new ScheduleEvent("1", "Reunião de equipe", hoje.atTime(10, 0), hoje.atTime(11, 0)));
        eventos.add(new ScheduleEvent("2", "Entrega do relatório", hoje.plusDays(2).atTime(14, 0), hoje.plusDays(2).atTime(15, 0)));
    }

    public List<ScheduleEvent> getEventos() {
        return eventos;
    }

    public String getUltimaAcao() {
        return ultimaAcao;
    }

    public void aoSelecionar(AjaxBehaviorEvent evento) {
        Schedule schedule = (Schedule) evento.getComponent();
        LocalDateTime inicio = schedule.getInicio();
        LocalDateTime fim = schedule.getFim();
        String id = String.valueOf(System.currentTimeMillis());
        eventos.add(new ScheduleEvent(id, "Novo evento", inicio, fim));
        ultimaAcao = "select: " + inicio + " até " + fim;
    }

    public void aoMover(AjaxBehaviorEvent evento) {
        Schedule schedule = (Schedule) evento.getComponent();
        atualizarEvento(schedule.getEventoId(), schedule.getInicio(), schedule.getFim());
        ultimaAcao = "move: evento " + schedule.getEventoId() + " para " + schedule.getInicio() + " até " + schedule.getFim();
    }

    public void aoRedimensionar(AjaxBehaviorEvent evento) {
        Schedule schedule = (Schedule) evento.getComponent();
        atualizarEvento(schedule.getEventoId(), schedule.getInicio(), schedule.getFim());
        ultimaAcao = "resize: evento " + schedule.getEventoId() + " até " + schedule.getFim();
    }

    public void aoClicar(AjaxBehaviorEvent evento) {
        Schedule schedule = (Schedule) evento.getComponent();
        ultimaAcao = "click: evento " + schedule.getEventoId();
    }

    private void atualizarEvento(String id, LocalDateTime inicio, LocalDateTime fim) {
        for (ScheduleEvent evento : eventos) {
            if (evento.getId().equals(id)) {
                evento.setInicio(inicio);
                evento.setFim(fim);
                return;
            }
        }
    }
}
