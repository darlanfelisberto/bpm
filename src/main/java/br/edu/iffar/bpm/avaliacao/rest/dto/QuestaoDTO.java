package br.edu.iffar.bpm.avaliacao.rest.dto;

import java.util.List;

public record QuestaoDTO(Long id, String enunciado, String tipo, boolean obrigatoria, List<OpcaoDTO> opcoes) {
}
