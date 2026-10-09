package br.com.feliva.bpm.avaliacao.rest.dto;

import java.util.List;

public record RespostaEnvioDTO(boolean completo, List<RespostaItemDTO> respostas) {
}
