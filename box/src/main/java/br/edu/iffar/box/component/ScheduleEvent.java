package br.edu.iffar.box.component;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Um evento exibido pelo b:schedule. POJO simples - quem usa o componente
 * gerencia a própria lista (criar/mover/redimensionar chegam via os
 * client behaviors "select"/"move"/"resize"; este objeto só carrega o que
 * já existe pra desenhar o calendário).
 */
public class ScheduleEvent implements Serializable {

    private String id;
    private String titulo;
    private LocalDateTime inicio;
    private LocalDateTime fim;
    private boolean diaTodo;
    private String cor;

    public ScheduleEvent() {
    }

    public ScheduleEvent(String id, String titulo, LocalDateTime inicio, LocalDateTime fim) {
        this.id = id;
        this.titulo = titulo;
        this.inicio = inicio;
        this.fim = fim;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public LocalDateTime getInicio() {
        return inicio;
    }

    public void setInicio(LocalDateTime inicio) {
        this.inicio = inicio;
    }

    public LocalDateTime getFim() {
        return fim;
    }

    public void setFim(LocalDateTime fim) {
        this.fim = fim;
    }

    public boolean isDiaTodo() {
        return diaTodo;
    }

    public void setDiaTodo(boolean diaTodo) {
        this.diaTodo = diaTodo;
    }

    public String getCor() {
        return cor;
    }

    public void setCor(String cor) {
        this.cor = cor;
    }
}
