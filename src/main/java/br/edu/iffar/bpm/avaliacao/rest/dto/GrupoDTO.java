package br.edu.iffar.bpm.avaliacao.rest.dto;

import java.util.List;

public record GrupoDTO(Long id, String titulo, String descricao, List<QuestaoDTO> questoes) {
}
