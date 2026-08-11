# frontend — tela de resposta (SPA)

App TypeScript + Vite (sem framework) usado pelo respondente para preencher um
instrumento avaliativo. Consome a API REST em `/api/avaliacao/instrumentos/...`
(backend Java, veja `AvaliacaoRespostaResource`). A parte administrativa
(cadastros) continua em JSF, fora desta pasta.

## Pré-requisitos

Para **desenvolver** com hot-reload, é preciso ter Node instalado na máquina
(recomendado: mesma versão do `<version.node>` no `bpm-app/pom.xml`, hoje
`v22.17.0`). Para apenas **buildar via Maven**, não precisa de nada: o
`frontend-maven-plugin` baixa seu próprio Node em `target/` durante o build.

## Desenvolvimento (hot-reload)

A partir de `bpm-app/` (esta pasta é `bpm-app/frontend/`):

```bash
cd frontend
npm install
npm run dev
```

Abre em `http://localhost:5173/responder/` (o `/responder/` no final vem do
`base` configurado em `vite.config.ts`, que precisa bater com o contexto onde
o WAR serve a SPA em produção). O mesmo arquivo já tem um proxy de `/api` para
`http://localhost:9080`, então as chamadas à API funcionam desde que o backend
esteja rodando (em outro terminal, dentro de `bpm-app/`):

```bash
cd bpm-app
set -a && source .env && set +a && mvn -o liberty:run
```

Este projeto é multi-módulo (`box` + `bpm-app`, ver `pom.xml` da raiz). Se
`mvn liberty:run` reclamar que não encontra o artefato `br.edu.iffar:box`,
rode `mvn install` uma vez a partir da raiz do repositório (não de `bpm-app/`)
para publicar `box` no repositório Maven local.

Para abrir uma tela específica em dev, use os mesmos parâmetros de URL do app
em produção, por exemplo:

```
http://localhost:5173/responder/?instrumentoId=2
```

(o `instrumentoId` precisa existir no banco; veja `V5__dados_exemplo_avaliacao.sql`
para o exemplo já cadastrado). O parâmetro `ref` é preenchido automaticamente
pelo próprio app (token anônimo ou identificação, ver `src/token.ts`).

## Build de produção

A partir de `bpm-app/`:

```bash
cd frontend
npm run build
```

Roda `tsc` (checagem de tipos, sem emitir arquivos — `noEmit: true`) e depois
`vite build`. A saída vai para `../target/responder-dist` (fora de `frontend/`,
mesma lógica do `target/` do Maven — não é versionada).

Não é preciso rodar isso manualmente para publicar: o build completo do
projeto (abaixo) já faz isso.

## Build completo (Maven, o caminho normal)

Na raiz do repositório (constrói `box` e `bpm-app` juntos, na ordem certa):

```bash
mvn clean package
```

O `pom.xml` tem um `frontend-maven-plugin` configurado para, antes de
empacotar o WAR:

1. Baixar Node/npm (em `target/`, isolado da máquina) — só na primeira vez ou
   quando a versão mudar;
2. `npm install` em `frontend/`;
3. `npm run build` em `frontend/`, gerando `target/responder-dist`.

Em seguida o `maven-war-plugin` copia `target/responder-dist` para dentro do
WAR, servido no contexto **`/responder`**. Ou seja, o resultado final do build
Maven já contém a SPA — não precisa buildar o frontend à parte antes.

## Onde a SPA fica servida

Depois de `mvn clean package` (ou `liberty:run`), a tela fica em:

```
http://localhost:9080/responder/index.html?instrumentoId=<id>
```

O link "Responder" na listagem de instrumentos (tela administrativa) já aponta
para essa URL. Importante: é `index.html` explícito, não `/responder/` — o
`FacesServlet` da parte JSF intercepta a resolução de welcome-file por
extensão `.xhtml` antes de tentar `index.html`, então um link para o diretório
sem o nome do arquivo dá 404.

## Estrutura

```
frontend/
  index.html          entrada da SPA
  vite.config.ts       base "/responder/", outDir "../target/responder-dist"
  src/
    main.ts             orquestra as telas (identificação → formulário)
    api.ts               chamadas fetch para /api/avaliacao/instrumentos/...
    types.ts              tipos espelhando os DTOs Java (rest/dto)
    token.ts               geração/leitura do token anônimo (localStorage)
    style.css               visual (mesma paleta do app.css do JSF)
```
