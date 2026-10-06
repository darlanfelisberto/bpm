package br.edu.iffar.bpm.avaliacao.rest.dto;

import java.util.UUID;

public record RespostaItemDTO(UUID questaoId, UUID opcaoId, String textoLivre) {
}
