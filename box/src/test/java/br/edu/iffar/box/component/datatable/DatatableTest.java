package br.edu.iffar.box.component.datatable;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testa a lógica pura de Datatable (parsing/cálculo, sem FacesContext):
 * ciclo de ordenação, parse do JSON de filtros vindo do request, e o
 * clamp de página quando um filtro reduz o total abaixo da página atual.
 */
class DatatableTest {

    @Test
    void clicarNumCampoNovoSempreVaiPraAscendente() {
        assertEquals(new Datatable.Ordenacao("nome", true), Datatable.proximaOrdenacao("nome", null, true));
        assertEquals(new Datatable.Ordenacao("nome", true), Datatable.proximaOrdenacao("nome", "email", false));
    }

    @Test
    void clicarDeNovoNoMesmoCampoAlternaAscDescNenhum() {
        // nenhum -> asc (campo ainda nao era o ordenado)
        assertEquals(new Datatable.Ordenacao("nome", true), Datatable.proximaOrdenacao("nome", null, true));
        // asc -> desc
        assertEquals(new Datatable.Ordenacao("nome", false), Datatable.proximaOrdenacao("nome", "nome", true));
        // desc -> nenhum (campo null - Datatable.ordenarPorAtual() cai pro padrao, se houver)
        Datatable.Ordenacao voltaAoNenhum = Datatable.proximaOrdenacao("nome", "nome", false);
        assertNull(voltaAoNenhum.campo());
        assertTrue(voltaAoNenhum.ascendente());
    }

    @Test
    void parseFiltrosValorNuloOuVazioViraMapaVazio() {
        assertEquals(Map.of(), Datatable.parseFiltros(null));
        assertEquals(Map.of(), Datatable.parseFiltros(""));
        assertEquals(Map.of(), Datatable.parseFiltros("   "));
    }

    @Test
    void parseFiltrosJsonValido() {
        assertEquals(Map.of("nome", "ana", "cargo", "dev"),
                Datatable.parseFiltros("{\"nome\":\"ana\",\"cargo\":\"dev\"}"));
    }

    @Test
    void parseFiltrosIgnoraCamposEmBranco() {
        // campo em branco = "sem filtro pra esse campo", nao "filtrar por vazio"
        assertEquals(Map.of("nome", "ana"), Datatable.parseFiltros("{\"nome\":\"ana\",\"cargo\":\"\",\"email\":\"  \"}"));
    }

    @Test
    void parseFiltrosJsonAdulteradoOuMalformadoViraMapaVazio() {
        assertEquals(Map.of(), Datatable.parseFiltros("nao e json"));
        assertEquals(Map.of(), Datatable.parseFiltros("[1,2,3]"));
        assertEquals(Map.of(), Datatable.parseFiltros("{\"nome\": 42}"));
    }

    @Test
    void parseInteiroValorNuloVazioOuInvalidoViraNull() {
        assertNull(Datatable.parseInteiro(null));
        assertNull(Datatable.parseInteiro(""));
        assertNull(Datatable.parseInteiro("abc"));
    }

    @Test
    void parseInteiroValorValido() {
        assertEquals(3, Datatable.parseInteiro("3"));
    }

    @Test
    void corrigirPaginaDentroDoLimiteNaoMuda() {
        assertEquals(2, Datatable.corrigirPaginaForaDoLimite(2, 10, 25));
    }

    @Test
    void corrigirPaginaForaDoLimiteVaiPraUltimaPaginaValida() {
        // 25 registros, 10 por pagina -> paginas 0,1,2 (25 vai na pagina 2: 20-24)
        assertEquals(2, Datatable.corrigirPaginaForaDoLimite(5, 10, 25));
        // total exato multiplo do tamanho de pagina - ultima pagina e' a anterior
        assertEquals(1, Datatable.corrigirPaginaForaDoLimite(4, 10, 20));
    }

    @Test
    void corrigirPaginaSemRegistrosVaiPraZero() {
        assertEquals(0, Datatable.corrigirPaginaForaDoLimite(3, 10, 0));
    }
}
