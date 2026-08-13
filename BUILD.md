# Build e configuração

Guia para compilar e rodar o BPM IFFar localmente.

## Pré-requisitos

| Ferramenta | Versão usada no desenvolvimento | Observação |
|---|---|---|
| JDK | 25 | `maven.compiler.source/target` no `pom.xml` raiz |
| Maven | 3.9+ | reactor multi-módulo (`box` + `bpm-app`) |
| PostgreSQL | 17 | banco `bpm`, aplicado via Flyway |

Node/npm **não** precisam ser instalados manualmente: o `frontend-maven-plugin`
(configurado em `bpm-app/pom.xml`) baixa a própria versão do Node
(`v22.17.0`) dentro de `bpm-app/target/` durante o build, isolada do que
estiver instalado na máquina.

## Estrutura do reactor

```
bpm-parent (pom, agregador)
├── box       - biblioteca de componentes Jakarta Faces reutilizável (jar)
└── bpm-app   - aplicação web (war), roda no Open Liberty
```

`bpm-app` depende de `box`; o Maven resolve isso automaticamente dentro do
mesmo reactor (não precisa instalar `box` à parte antes).

## 1. Banco de dados

Crie o banco (o schema é criado pelo Flyway, só o banco em si precisa
existir antes):

```bash
createdb bpm
# ou: psql -U postgres -c "CREATE DATABASE bpm;"
```

As migrations ficam em `bpm-app/src/main/resources/db/migration/` e rodam
automaticamente:
- ao empacotar (`mvn install` roda os testes/build, mas quem efetivamente
  aplica as migrations no banco é o `liberty-maven-plugin` ao subir a
  aplicação, ou manualmente via `mvn flyway:migrate` — ver seção 3)

## 2. Variáveis de ambiente (`.env`)

`bpm-app/server.xml` e o `flyway-maven-plugin` (pom.xml) leem duas
variáveis de ambiente para a conexão com o banco: `APP_DB_USER` e
`APP_DB_PASSWORD`. Host/porta/nome do banco estão fixos em `server.xml`
(`localhost:5432/bpm`).

```bash
cp bpm-app/.env.example bpm-app/.env
# edite bpm-app/.env com as credenciais do seu Postgres local
```

`bpm-app/.env` **não é versionado** (está no `.gitignore`). Antes de
qualquer comando Maven que precise do banco (`liberty:run`,
`flyway:migrate`, etc.), exporte essas variáveis a partir do arquivo:

```bash
cd bpm-app
set -a && source .env && set +a
```

## 3. Build

Da raiz do repositório:

```bash
mvn clean install -DskipTests
```

Isso compila `box`, gera o `box-*.jar`, compila `bpm-app` (incluindo o
frontend Vite em `bpm-app/frontend/`, que vira `bpm-app/target/responder-dist/`
e é empacotado servido em `/responder`), e gera `bpm-app/target/bpm.war`.

### Bibliotecas JS/CSS de terceiros (Quill, Bootstrap Icons)

Essas bibliotecas **não são versionadas no git**. O `download-maven-plugin`
baixa os arquivos (jsdelivr) durante a fase `generate-resources` de cada
módulo, valida o SHA-256 contra o hash fixado no `pom.xml` raiz (o build
falha se não bater) e os coloca em `target/vendor-download/`.

- Primeiro build (ou depois de limpar o cache): precisa de rede.
- Builds seguintes: o download-maven-plugin cacheia o arquivo em
  `~/.m2/repository/.cache/download-maven-plugin/` e reaproveita esse cache,
  então `mvn -o` (offline) funciona normalmente no dia a dia.

Para atualizar uma dessas libs: mude `version.quill` ou
`version.bootstrap-icons` (e o(s) `*.sha256` correspondente(s)) nas
`<properties>` do `pom.xml` raiz, e rode o build normalmente.

## 4. Rodar localmente (Open Liberty)

```bash
cd bpm-app
set -a && source .env && set +a
mvn -o liberty:run
```

- Aplicação: http://localhost:9080
- HTTPS: porta 9443 (ver `liberty.var.default.https.port` em `bpm-app/pom.xml`)
- `liberty:run` roda em primeiro plano (Ctrl+C para parar). Para
  desenvolvimento com hot-reload de código Java/Facelets, use
  `mvn -o liberty:dev` no lugar de `liberty:run`.

O driver JDBC do PostgreSQL é copiado automaticamente para
`lib/global` do servidor Liberty pelo próprio `liberty-maven-plugin`
(`copyDependencies` no `pom.xml` de `bpm-app`) — não precisa instalar nada
manualmente no servidor.

## 5. Migrations do banco (Flyway) sem subir o servidor

```bash
cd bpm-app
set -a && source .env && set +a
mvn -o flyway:info      # mostra o estado atual das migrations
mvn -o flyway:migrate   # aplica as pendentes
```

## Resumo rápido (depois do primeiro setup)

```bash
cd bpm-app
set -a && source .env && set +a
cd ..
mvn -q -o clean install -DskipTests
cd bpm-app
mvn -o liberty:run
```
