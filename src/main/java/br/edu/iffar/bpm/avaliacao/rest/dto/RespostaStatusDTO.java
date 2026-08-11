package br.edu.iffar.bpm.avaliacao.rest.dto;

import java.util.List;

public record RespostaStatusDTO(String status, List<RespostaItemDTO> respostas) {
}
