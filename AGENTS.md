# AGENTS.md

Orientações para quem (humano ou agente de IA) for mexer neste repositório.
Para instruções de build/execução passo a passo, ver `BUILD.md` na raiz e
`box-showcase/README.md`.

## O que é este projeto

Sistema de BPM (gestão de processos) do IFFar, em Jakarta Faces (JSF) 4.x
rodando em Open Liberty, com PostgreSQL via Flyway.

Reactor Maven multi-módulo:

```
bpm-parent (pom, agregador)
├── box            - lib de componentes Jakarta Faces reutilizável (jar),
│                    sem dependência de banco ou de regra de negócio
├── box-showcase   - vitrine/documentação viva dos componentes do box (war,
│                    sem banco) + onde ficam os testes end-to-end deles
└── bpm-app        - aplicação de negócio (war), roda no Open Liberty,
                     depende de Postgres
```

`box-showcase` e `bpm-app` dependem de `box` e são resolvidos
automaticamente dentro do reactor.

## Convenções de código

- **Nomes em português**: classes, métodos, variáveis, mensagens de commit
  e comentários usam português (ex.: `Schedule.parseDataHora`,
  `renderizarGrade`, `ScheduleDemoBean`). Siga esse padrão em código novo —
  não misture inglês.
- **Sem comentários óbvios**: só comente o "porquê" quando não for óbvio
  (uma decisão contra-intuitiva, um workaround, uma armadilha conhecida).
  Não descreva o que o código já deixa claro pelo nome.
- Componentes JSF customizados (`box`) seguem o padrão
  `UIComponentBase implements ClientBehaviorHolder`: `decode()` lê
  `jakarta.faces.source`/`jakarta.faces.behavior.event` e delega a cada
  `ClientBehavior` registrado, deixando `<f:ajax event="..." listener="..." render="...">`
  funcionar em componentes totalmente customizados como se fossem
  `h:commandLink`. Veja `Schedule.java`/`Schedule2.java` como referência.
- JS de componentes usa `window.faces.ajax.*` (Jakarta Faces 4.x — **não**
  existe mais `window.jsf`). Qualquer checagem de `window.faces` deve
  acontecer dentro de um handler de `DOMContentLoaded`, porque o JS de cada
  componente é incluído (via `@ResourceDependencies`) antes do próprio
  `faces.js` no `<head>`.
- Ao montar DOM em JS, prefira `createElement`/`textContent` a `innerHTML`
  (evita XSS). Ver `schedule2.js` como exemplo.
- Bibliotecas JS/CSS de terceiros (Quill, Bootstrap Icons, FullCalendar)
  **não são versionadas no git** — são baixadas via `download-maven-plugin`
  (checksum SHA-256 fixado no `pom.xml` raiz) na fase `generate-resources`.
  Ver seção "Bibliotecas JS/CSS de terceiros" do `BUILD.md`.

## Testes

- Testes unitários (`box/src/test/`, JUnit 5) rodam normalmente em
  `mvn test`/`install`.
- Testes end-to-end (Playwright) ficam **exclusivamente** em
  `box-showcase/src/e2e-test/java/.../e2e/`, isolados no profile Maven
  `e2e` (não ativo por padrão — rodar com `mvn verify -Pe2e`). Cobrem
  comportamento de componentes do `box`, não regras de negócio do
  `bpm-app`.
- **Antes de escrever testes novos (unitários ou E2E), confirme com o
  usuário.** Nem sempre é o momento certo — pergunte antes de assumir.

## Git

- **Nunca dê `git push` sem perguntar antes**, mesmo que o usuário já
  tenha aprovado pushes anteriores na mesma conversa — confirme a cada
  novo commit.
- Commits novos, nunca `--amend` a menos que pedido explicitamente.
- Mensagens de commit em português, curtas, focadas no "porquê".

## Onde documentar o quê

- `BUILD.md` (raiz): setup, build, banco, variáveis de ambiente, como
  rodar `bpm-app` e `box-showcase`, testes.
- `box-showcase/README.md`: manual específico de execução da vitrine.
- `box-showcase/src/main/webapp/*.xhtml`: cada página de componente é a
  documentação viva da API daquele componente (tabela de atributos/client
  behaviors + trecho de código de exemplo) — ao adicionar/alterar um
  componente do `box`, atualize a página correspondente na vitrine.
