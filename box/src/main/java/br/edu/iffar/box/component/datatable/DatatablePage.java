package br.edu.iffar.box.component.datatable;

import java.util.List;

/** Uma página de resultado: as linhas pedidas e o total de registros (sem o filtro de página, só o filtro/busca). */
public record DatatablePage<T>(List<T> linhas, long total) {
}
