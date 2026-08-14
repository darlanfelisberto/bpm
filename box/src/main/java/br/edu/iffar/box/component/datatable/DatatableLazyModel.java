package br.edu.iffar.box.component.datatable;

/**
 * Fonte de dados do b:datatable. Cada interação (paginar, ordenar, filtrar)
 * chama {@link #carregar(DatatableConsulta)} de novo - nenhuma linha fica
 * guardada no estado da view entre requisições, só os parâmetros da
 * consulta (página/ordenação/filtros), então a implementação é responsável
 * por buscar (banco, serviço, etc.) só a fatia pedida.
 */
public interface DatatableLazyModel<T> {

    DatatablePage<T> carregar(DatatableConsulta consulta);

    /** Usado quando o componente não recebe o atributo "linhasPorPagina". */
    default int linhasPorPaginaPadrao() {
        return 10;
    }
}
