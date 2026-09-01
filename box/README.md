# box

Biblioteca de componentes Jakarta Faces (JSF 4.x) reutilizável do IFFar.
Sem dependência de banco de dados ou de regra de negócio — apenas componentes
de UI prontos para uso em qualquer aplicação Jakarta EE.

## Componentes disponíveis

| Tag | Descrição |
|---|---|
| `<b:autocomplete>` | Input com sugestões dinâmicas via AJAX |
| `<b:datatable>` | Tabela com paginação, ordenação e filtros client/server |
| `<b:editor>` | Editor de texto rico (Quill), com sanitização server-side |
| `<b:growl>` | Toast de mensagens do FacesContext flutuante |
| `<b:menu>` | Menu vertical com itens, submenus e separadores |
| `<b:menuitem>` | Item de menu (link bookmarkable ou ação server/AJAX) |
| `<b:submenu>` | Agrupador colapsível de itens de menu |
| `<b:separator>` | Separador visual dentro de um menu |
| `<b:question>` | Questão avaliativa (descritiva com textarea ou alternativas com radio buttons) |
| `<b:panel>` | Painel com cabeçalho opcional |
| `<b:popup>` | Diálogo modal (`<dialog>` nativo) |
| `<b:schedule>` | Calendário (FullCalendar) com eventos via bean |
| `<b:schedule2>` | Calendário v2 — renderização JS pura via AJAX |
| `<box-confirm>` | Custom element de confirmação inline (sem componente Faces) |

Namespace XHTML: `xmlns:b="http://iffar.edu.br/box"`

---

## Estrutura do módulo

```
box/
├── src/main/java/br/edu/iffar/box/component/   # componentes Java
├── src/main/resources/
│   └── META-INF/
│       ├── box.taglib.xml                       # taglib Facelets
│       └── resources/box/                       # recursos estáticos servidos
│           ├── box.css                          # bundle CSS gerado pelo SASS
│           ├── core/box-core.js                 # utilitários JS compartilhados
│           ├── <componente>/<componente>.js      # JS de cada componente
│           └── vendor/                          # quill.js, fullcalendar.js (baixados pelo build)
└── src/main/sass/                               # fontes SASS
```

---

## CSS e temas

O CSS da biblioteca é gerenciado via **Dart Sass** e compilado durante o build Maven.

### Fontes SASS

```
src/main/sass/
├── _variables.scss        # Variáveis SASS (z-index, transições, tamanhos)
├── _mixins.scss           # Mixins: box-transition(), box-focus-ring()
├── _default-theme.scss    # Tema padrão — verde IFFar
├── _dark-theme.scss       # Tema escuro
├── components/            # Um arquivo .scss por componente
│   ├── autocomplete.scss
│   ├── confirm.scss
│   ├── datatable.scss
│   ├── editor.scss
│   ├── growl.scss
│   ├── menu.scss
│   ├── popup.scss
│   ├── question.scss
│   ├── schedule.scss
│   └── schedule2.scss
└── box.scss               # Entry point — importa temas + todos os componentes
```

O arquivo gerado `box/box.css` é o **único CSS que os componentes carregam**
via `@ResourceDependency`. Todos os estilos (temas + componentes) são incluídos
nesse bundle.

### Variáveis CSS de tema

Os temas definem variáveis CSS (`--color-*`) consumidas por todos os componentes:

| Variável | Default (claro) | Dark |
|---|---|---|
| `--color-primary` | `#2f6f4f` (verde IFFar) | `#4ade80` |
| `--color-primary-dark` | `#24563e` | `#22c55e` |
| `--color-primary-contrast` | `#ffffff` | `#052e16` |
| `--color-danger` | `#b3261e` | `#f87171` |
| `--color-warn` | `#b58105` | `#fbbf24` |
| `--color-text` | `#1f2933` | `#e2e8f0` |
| `--color-text-muted` | `#616e7c` | `#94a3b8` |
| `--color-surface` | `#ffffff` | `#1e293b` |
| `--color-bg` | `#f4f6f8` | `#0f172a` |
| `--color-border` | `#dfe4ea` | `#334155` |
| `--shadow` | `0 1px 2px rgba(16,24,32,0.06)` | `0 1px 4px rgba(0,0,0,0.4)` |
| `--radius` | `8px` | `8px` |

### Usando temas

**Tema padrão** — aplicado automaticamente em `:root`. Nenhuma configuração necessária.

**Tema escuro explícito** — adicione a classe `box-theme-dark` em qualquer ancestral:

```html
<!-- Aplica o tema escuro em toda a página -->
<html class="box-theme-dark">

<!-- Ou apenas em um bloco específico -->
<div class="box-theme-dark">
  <b:menu>...</b:menu>
</div>
```

**Tema escuro automático** — se o usuário tiver `prefers-color-scheme: dark` no
sistema e a página não tiver nenhuma classe `box-theme-*` no `<html>`, o tema
escuro é aplicado automaticamente via `@media`.

**Forçar tema claro** — use `box-theme-default` para ignorar o `prefers-color-scheme`:

```html
<html class="box-theme-default">
```

### Criando um novo tema

1. Crie `src/main/sass/themes/_meu-tema.scss` redefinindo as variáveis CSS:

```scss
.box-theme-meu-tema {
  --color-primary:          #1d4ed8;
  --color-primary-dark:     #1e40af;
  --color-primary-contrast: #ffffff;
  --color-danger:           #dc2626;
  --color-warn:             #d97706;
  --color-success:          #16a34a;
  --color-text:             #111827;
  --color-text-muted:       #6b7280;
  --color-surface:          #ffffff;
  --color-bg:               #f9fafb;
  --color-border:           #e5e7eb;
  --shadow:                 0 1px 3px rgba(0, 0, 0, 0.1);
  --radius:                 6px;
}
```

2. Importe no entry point `src/main/sass/box.scss`:

```scss
@use 'themes/meu-tema';
```

3. Recompile (ver seção Build abaixo).

---

## Build

### Pré-requisitos

| Ferramenta | Versão | Observação |
|---|---|---|
| JDK | 25 | `maven.compiler.source/target` no `pom.xml` raiz |
| Maven | 3.9+ | — |
| Dart Sass | 1.x | Deve estar no `PATH` como `sass` |

> **Instalando o Sass**: `npm install -g sass`  
> No projeto, o executável é referenciado pelo caminho absoluto em `box/pom.xml`
> (`<executable>`). Se o caminho do `sass` na sua máquina for diferente do padrão
> (`~/.nvm/versions/node/<versão>/bin/sass`), ajuste `<executable>` na execução
> `compile-sass` do `exec-maven-plugin` em `box/pom.xml`.

### Compilar SASS manualmente

```bash
# Bundle completo (temas + todos os componentes) — comprimido, sem source map
sass src/main/sass/box.scss \
     src/main/resources/META-INF/resources/box/box.css \
     --style=compressed --no-source-map

# Modo watch (recompila automaticamente a cada alteração nos .scss)
sass --watch \
     src/main/sass/box.scss:src/main/resources/META-INF/resources/box/box.css
```

### Build Maven (automático)

O SASS é compilado automaticamente na fase `generate-resources` via
`exec-maven-plugin` configurado em `box/pom.xml`:

```bash
# Só o módulo box
mvn install -DskipTests -pl box

# Reactor completo (a partir da raiz)
mvn install -DskipTests
```

O plugin compila `box.scss` para `target/generated-resources/sass/META-INF/resources/box/box.css`
(bundle único empacotado no JAR final e consumido pelos componentes). Nenhum arquivo CSS derivado
é versionado no diretório `src/`.

### Adicionando ou alterando estilos

1. Edite o arquivo SCSS correspondente em `src/main/sass/components/`.
2. Para mudanças de tema, edite `_default-theme.scss` ou `_dark-theme.scss`.
3. Rode `mvn install -DskipTests -pl box` para recompilar e reinstalar.
4. Se estiver rodando o `box-showcase` com `liberty:dev`, o Liberty recarrega o
   JAR automaticamente após o `mvn install` — recarregue a página no browser.

---

## Usando a biblioteca em outro módulo

Dentro do mesmo reactor Maven, a dependência já é resolvida automaticamente.
Em um módulo externo, adicione ao `pom.xml`:

```xml
<dependency>
    <groupId>br.edu.iffar</groupId>
    <artifactId>box</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

Declare o namespace no XHTML e use os componentes:

```xml
<html xmlns:b="http://iffar.edu.br/box">
  <h:head/>
  <h:body>
    <b:growl/>
    <b:datatable value="#{bean.items}" var="item">
      <b:column header="Nome" field="nome"/>
    </b:datatable>
  </h:body>
</html>
```

O `box.css` e os scripts JS de cada componente são injetados automaticamente
no `<head>` via `@ResourceDependency` — sem `<link>` ou `<script>` manuais.

Para o `<box-confirm>` (custom element HTML, não é um componente Faces), inclua
manualmente o script no template da aplicação:

```xml
<h:outputScript library="box" name="confirm/confirm.js" target="head"/>
```

O CSS do `<box-confirm>` já está incluso no `box.css`.

---

## Documentação viva

Cada componente tem uma página de exemplo no `box-showcase`:

```bash
cd box-showcase
mvn -o liberty:run
# Acesse: http://localhost:9081
```

As páginas `.xhtml` em `box-showcase/src/main/webapp/` documentam a API de
atributos, eventos e client behaviors de cada componente com exemplos executáveis.
Para mais detalhes sobre como rodar o showcase, ver `box-showcase/README.md`.
