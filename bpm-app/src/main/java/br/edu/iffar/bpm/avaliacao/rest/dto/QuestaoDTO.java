package br.edu.iffar.bpm.avaliacao.rest.dto;

import java.util.List;
import java.util.UUID;

public record QuestaoDTO(UUID id, String enunciado, String tipo, boolean obrigatoria, List<OpcaoDTO> opcoes) {
}
