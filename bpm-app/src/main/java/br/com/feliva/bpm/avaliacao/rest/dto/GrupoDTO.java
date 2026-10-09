package br.com.feliva.bpm.avaliacao.rest.dto;

import java.util.List;
import java.util.UUID;

public record GrupoDTO(UUID id, String titulo, String descricao, List<QuestaoDTO> questoes) {
}
