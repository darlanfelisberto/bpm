package br.edu.iffar.box.component.datatable;

import java.util.Map;

/**
 * Parâmetros de uma página pedida ao {@link DatatableLazyModel}: offset e
 * tamanho da página, campo/direção de ordenação (null/false = sem
 * ordenação) e filtros por coluna (campo -> texto digitado, nunca null,
 * mas pode vir vazio).
 */
public record DatatableConsulta(
        int primeiro,
        int quantidade,
        String ordenarPor,
        boolean ordenarAscendente,
        Map<String, String> filtros) {
}
