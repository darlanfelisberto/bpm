package br.edu.iffar.box.component.schedule;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Testa Schedule.parseDataHora() contra os tres formatos que o
 * FullCalendar realmente manda (descobertos testando manualmente no
 * navegador): sem offset ("select"), com offset de fuso (mover/
 * redimensionar um evento), e so a data (selecao/evento de dia inteiro).
 */
class ScheduleParseDataHoraTest {

    @Test
    void valorNuloOuVazioViraNull() {
        assertNull(Schedule.parseDataHora(null));
        assertNull(Schedule.parseDataHora(""));
        assertNull(Schedule.parseDataHora("   "));
    }

    @Test
    void dataHoraSemOffset() {
        // formato que o "select" manda
        assertEquals(LocalDateTime.of(2026, 9, 3, 0, 0), Schedule.parseDataHora("2026-09-03T00:00"));
        assertEquals(LocalDateTime.of(2026, 8, 20, 10, 30), Schedule.parseDataHora("2026-08-20T10:30:00"));
    }

    @Test
    void dataHoraComOffsetDeFuso() {
        // formato que mover/redimensionar um evento manda - a hora local
        // (nao a hora UTC) e o que importa aqui, o offset e so descartado.
        assertEquals(LocalDateTime.of(2026, 8, 14, 10, 0), Schedule.parseDataHora("2026-08-14T10:00:00-03:00"));
        assertEquals(LocalDateTime.of(2026, 8, 14, 11, 0), Schedule.parseDataHora("2026-08-14T11:00:00-03:00"));
    }

    @Test
    void soData_viraInicioDoDia() {
        // formato de selecao/evento de dia inteiro
        assertEquals(LocalDateTime.of(2026, 8, 20, 0, 0), Schedule.parseDataHora("2026-08-20"));
    }
}
