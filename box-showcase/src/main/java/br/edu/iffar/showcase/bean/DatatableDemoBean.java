package br.edu.iffar.showcase.bean;

import br.edu.iffar.box.component.datatable.Datatable;
import br.edu.iffar.box.component.datatable.DatatableConsulta;
import br.edu.iffar.box.component.datatable.DatatableLazyModel;
import br.edu.iffar.box.component.datatable.DatatablePage;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.event.AjaxBehaviorEvent;
import jakarta.inject.Named;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Estado da página de demonstração do b:datatable (/datatable.xhtml).
 * Implementa DatatableLazyModel diretamente no bean - dataset sintético em
 * memória, mas carregar() só devolve a fatia pedida (como uma consulta
 * paginada faria contra um banco de verdade). Não define o atributo
 * "linhasPorPagina" na tag: quem controla o tamanho da página aqui é o
 * método linhasPorPaginaPadrao() da própria interface.
 */
@Named
@SessionScoped
public class DatatableDemoBean implements Serializable, DatatableLazyModel<Pessoa> {

    private static final String[] NOMES = {
            "Ana", "Bruno", "Carla", "Diego", "Elisa", "Fábio", "Gabriela", "Heitor",
            "Íris", "João", "Karina", "Leonardo", "Marina", "Nelson", "Olívia", "Pedro"
    };
    private static final String[] SOBRENOMES = {
            "Silva", "Souza", "Oliveira", "Pereira", "Costa", "Rodrigues", "Almeida", "Nunes"
    };
    private static final String[] CARGOS = {
            "Desenvolvedor(a)", "Analista", "Coordenador(a)", "Estagiário(a)", "Gerente"
    };

    private List<Pessoa> pessoas;
    private String ultimaSelecao = "";

    @PostConstruct
    void iniciar() {
        popular();
    }

    private void popular() {
        pessoas = new ArrayList<>();
        for (int i = 1; i <= 63; i++) {
            String nome = NOMES[i % NOMES.length] + " " + SOBRENOMES[(i / NOMES.length) % SOBRENOMES.length];
            String email = nome.toLowerCase(Locale.ROOT).replace(" ", ".").replace("í", "i") + "@iffar.edu.br";
            pessoas.add(new Pessoa((long) i, nome, email, CARGOS[i % CARGOS.length],
                    LocalDate.of(2016, 1, 1).plusDays(i * 17L)));
        }
    }

    public String getUltimaSelecao() {
        return ultimaSelecao;
    }

    public void aoSelecionar(AjaxBehaviorEvent evento) {
        Datatable datatable = (Datatable) evento.getComponent();
        Object selecionado = datatable.getObjetoSelecionado();
        ultimaSelecao = selecionado != null
                ? "select: " + selecionado
                : "select: id " + datatable.getLinhaId() + " não encontrado";
    }

    public void restaurar() {
        popular();
        ultimaSelecao = "";
    }

    @Override
    public int linhasPorPaginaPadrao() {
        return 8;
    }

    @Override
    public DatatablePage<Pessoa> carregar(DatatableConsulta consulta) {
        List<Pessoa> filtradas = pessoas.stream()
                .filter(pessoa -> corresponde(pessoa, consulta.filtros()))
                .collect(Collectors.toCollection(ArrayList::new));

        Comparator<Pessoa> comparador = comparadorPara(consulta.ordenarPor());
        if (comparador != null) {
            filtradas.sort(consulta.ordenarAscendente() ? comparador : comparador.reversed());
        }

        int total = filtradas.size();
        if (consulta.primeiro() >= total) {
            return new DatatablePage<>(List.of(), total);
        }
        int fim = Math.min(consulta.primeiro() + consulta.quantidade(), total);
        return new DatatablePage<>(new ArrayList<>(filtradas.subList(consulta.primeiro(), fim)), total);
    }

    private boolean corresponde(Pessoa pessoa, Map<String, String> filtros) {
        for (Map.Entry<String, String> filtro : filtros.entrySet()) {
            String valor = valorDoCampo(pessoa, filtro.getKey());
            if (valor == null || !valor.toLowerCase(Locale.ROOT).contains(filtro.getValue().toLowerCase(Locale.ROOT))) {
                return false;
            }
        }
        return true;
    }

    private String valorDoCampo(Pessoa pessoa, String campo) {
        return switch (campo) {
            case "nome" -> pessoa.getNome();
            case "email" -> pessoa.getEmail();
            case "cargo" -> pessoa.getCargo();
            case "dataAdmissao" -> pessoa.getDataAdmissao() != null ? pessoa.getDataAdmissao().toString() : null;
            default -> null;
        };
    }

    private Comparator<Pessoa> comparadorPara(String campo) {
        if (campo == null) {
            return null;
        }
        return switch (campo) {
            case "nome" -> Comparator.comparing(Pessoa::getNome, Comparator.nullsLast(String::compareTo));
            case "email" -> Comparator.comparing(Pessoa::getEmail, Comparator.nullsLast(String::compareTo));
            case "cargo" -> Comparator.comparing(Pessoa::getCargo, Comparator.nullsLast(String::compareTo));
            case "dataAdmissao" ->
                    Comparator.comparing(Pessoa::getDataAdmissao, Comparator.nullsLast(Comparator.naturalOrder()));
            default -> null;
        };
    }

    /** Usado pelo PessoaConverter (injetado por CDI) pra reconstruir a linha selecionada a partir do id. */
    Pessoa buscarPorId(long id) {
        return pessoas.stream().filter(pessoa -> pessoa.getId() == id).findFirst().orElse(null);
    }
}
