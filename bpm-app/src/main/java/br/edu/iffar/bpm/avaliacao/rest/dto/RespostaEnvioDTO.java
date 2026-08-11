package br.edu.iffar.bpm.avaliacao.rest.dto;

import java.util.List;

public record RespostaEnvioDTO(boolean completo, List<RespostaItemDTO> respostas) {
}
