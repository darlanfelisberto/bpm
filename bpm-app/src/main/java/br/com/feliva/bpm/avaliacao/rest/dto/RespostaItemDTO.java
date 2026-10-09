package br.com.feliva.bpm.avaliacao.rest.dto;

import java.util.UUID;

public record RespostaItemDTO(UUID questaoId, UUID opcaoId, String textoLivre) {
}
