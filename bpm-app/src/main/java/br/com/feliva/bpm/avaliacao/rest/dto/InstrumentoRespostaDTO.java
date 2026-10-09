package br.com.feliva.bpm.avaliacao.rest.dto;

import java.util.List;
import java.util.UUID;

public record InstrumentoRespostaDTO(
        UUID id,
        String titulo,
        String descricao,
        boolean anonimo,
        boolean aberto,
        List<GrupoDTO> grupos) {
}
