package br.edu.iffar.bpm.avaliacao.rest.dto;

import java.util.List;

public record InstrumentoRespostaDTO(
        Long id,
        String titulo,
        String descricao,
        boolean anonimo,
        boolean aberto,
        List<GrupoDTO> grupos) {
}
