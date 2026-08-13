# box-showcase

Vitrine dos componentes Jakarta Faces nativos da lib `box` (módulo
`br.edu.iffar:box`, reutilizável entre projetos do IFFar): uma página de
demonstração por componente, com exemplo ao vivo, trecho de código XHTML
pra reproduzi-lo e descrição dos atributos.

## Pré-requisitos

| Ferramenta | Versão |
|---|---|
| JDK | 25 |
| Maven | 3.9+ |

**Sem banco de dados** — nenhum componente do `box` precisa de
persistência, diferente do `bpm-app`.

## Como rodar

Da raiz do repositório:

```bash
cd box-showcase
mvn -o liberty:run
```

- Aplicação: **http://localhost:9081**
- Ctrl+C para parar (`liberty:run` roda em primeiro plano).
- Pra desenvolvimento com hot-reload de código Java/Facelets, use
  `mvn -o liberty:dev` no lugar de `liberty:run`.
- Porta diferente da do `bpm-app` (9080/9443): dá pra rodar os dois ao
  mesmo tempo sem conflito.

Se for a primeira vez rodando o reactor (ou depois de um `git pull` que
trouxe módulo/dependência nova), compile tudo antes, da raiz do
repositório:

```bash
mvn clean install -DskipTests
```

(Sem essa flag `-o` na primeira vez: o `download-maven-plugin` precisa de
rede pra baixar Quill/Bootstrap Icons/FullCalendar. Ver `BUILD.md` na raiz
do repositório para mais detalhes sobre esse mecanismo.)

## O que tem na vitrine

| Página | Componente | Demonstra |
|---|---|---|
| `/index.xhtml` | — | Lista com link pra cada página abaixo |
| `/panel.xhtml` | `b:panel` | Quadro com título opcional |
| `/confirm.xhtml` | `b:confirm` | Confirmação inline antes de uma ação, como behavior aninhado num `h:commandLink` e como atributo puro `data-box-confirm` (sem componente Faces) |
| `/editor.xhtml` | `b:editor` | Editor de texto rico (Quill): negrito, itálico, fonte, cor, listas, colar imagens, etc. |
| `/schedule.xhtml` | `b:schedule` | Agenda de eventos (FullCalendar): mês/semana/dia, criar/mover/redimensionar evento, clicar num evento |
| `/schedule2.xhtml` | `b:schedule2` | Mesma ideia do `b:schedule`, sem nenhuma lib externa (só visão de mês, sem redimensionar) — pra comparar |

Cada página de componente tem uma tabela "Atributos" e/ou "Client
behaviors" documentando a API, e um bloco `EXEMPLO` com o trecho de XHTML
exato usado na própria demonstração.

## Estado das demonstrações

Os beans por trás de cada página (`ConfirmDemoBean`, `EditorDemoBean`,
`ScheduleDemoBean`, `Schedule2DemoBean`) são `@SessionScoped`: o estado
(itens excluídos, conteúdo do editor, eventos movidos) persiste enquanto
a sessão do navegador durar, e volta ao normal numa aba anônima nova ou
depois que a sessão expirar. Não precisa limpar nada manualmente — não
tem banco de dados por trás, é tudo em memória.

## Testes end-to-end

`box-showcase/src/e2e-test/java/.../e2e/*IT.java` (Playwright) cobrem os
comportamentos interativos dos componentes (editor continuar editável
após um ajax, popups de confirmação, arrastar um evento no calendário,
etc.). Rodam via `mvn verify -Pe2e` — ver a seção "Testes" do `BUILD.md`
na raiz do repositório para os detalhes (por que é um profile separado,
como instalar o Chromium do Playwright na primeira vez, etc.).
