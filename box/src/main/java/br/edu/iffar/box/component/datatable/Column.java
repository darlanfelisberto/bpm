package br.edu.iffar.box.component.datatable;

import jakarta.faces.component.FacesComponent;
import jakarta.faces.component.UIComponentBase;

/**
 * Metadado de uma coluna do b:datatable - não desenha nada sozinha (o
 * Datatable é quem lê os atributos e, linha a linha, os filhos dela).
 *
 * Sem filhos, a célula mostra o valor de "field" (via resolução de
 * propriedade padrão, igual #{var.field}). Com filhos, eles definem o
 * corpo da célula, avaliados uma vez por linha - útil pra formatação
 * (ex.: <b:column header="Status"><span class="badge">#{item.status}
 * </span></b:column>).
 *
 * Uso: xmlns:b="http://iffar.edu.br/box"
 *      <b:column field="nome" header="Nome" sortable="true" filterable="true"/>
 */
@FacesComponent(
        value = Column.COMPONENT_TYPE,
        createTag = true,
        tagName = "column",
        namespace = "http://iffar.edu.br/box")
public class Column extends UIComponentBase {

    public static final String COMPONENT_TYPE = "br.edu.iffar.box.Column";
    public static final String COMPONENT_FAMILY = "br.edu.iffar.box.Column";

    @Override
    public String getFamily() {
        return COMPONENT_FAMILY;
    }

    /** Nome da propriedade lida do objeto de linha (var) e enviada ao backend como chave de ordenação/filtro. */
    public String getField() {
        return (String) getStateHelper().eval("field");
    }

    public void setField(String field) {
        getStateHelper().put("field", field);
    }

    /** Rótulo exibido no cabeçalho. Sem valor, usa o próprio "field". */
    public String getHeader() {
        return (String) getStateHelper().eval("header");
    }

    public void setHeader(String header) {
        getStateHelper().put("header", header);
    }

    public boolean isSortable() {
        Boolean sortable = (Boolean) getStateHelper().eval("sortable");
        return sortable != null && sortable;
    }

    public void setSortable(boolean sortable) {
        getStateHelper().put("sortable", sortable);
    }

    public boolean isFilterable() {
        Boolean filterable = (Boolean) getStateHelper().eval("filterable");
        return filterable != null && filterable;
    }

    public void setFilterable(boolean filterable) {
        getStateHelper().put("filterable", filterable);
    }
}
