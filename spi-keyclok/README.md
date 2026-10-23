# Federação SIG no Keycloak

Este módulo autentica somente usuários vinculados a `sig.user_sig` no banco
do `Sistema-DCLC/auth`. O login usa `auth.auth_user.username`, a senha SIG
legada usa MD5 em hexadecimal minúsculo e contas `inativo` são recusadas.
A extensão não altera o banco externo nem armazena senhas no Keycloak. O ID
federado deriva de `auth.auth_user.auth_user_id`.

## Keycloak local

Requer JDK 21 ou superior, Maven, `curl`, `unzip`, `sha256sum` e `jq`. A versão
fixada é **26.8.1**. O Keycloak usa `dev-file` somente para desenvolvimento;
o PostgreSQL externo é um datasource separado chamado `sigstore`.
O datasource SIG usa transações não XA porque a instância PostgreSQL local
tem `max_prepared_transactions=0`; o banco interno do Keycloak usa XA.

```bash
cd spi-keyclok
./setup.sh
cp local.env.example local.env
# Ajuste URL, usuário e senha do PostgreSQL, senha do administrador e BPM_CLIENT_SECRET.
./start.sh
```

Com o servidor iniciado, em outro terminal:

```bash
cd spi-keyclok
./configure.sh
```

O realm fica em `http://localhost:8081/realms/bpm`. Defina em
`bpm-app/.env` o mesmo segredo de `BPM_CLIENT_SECRET` como
`OIDC_CLIENT_SECRET`. O callback cadastrado é
`http://localhost:9080/oidcclient/redirect/bpm`.

Para a conexão externa, use um usuário PostgreSQL com `USAGE` nos schemas
`auth` e `sig` e `SELECT` em `auth.auth_user`, `auth.pessoas` e
`sig.user_sig`. Não conceda escrita. A configuração local fica em
`local.env`, ignorado pelo Git. O ZIP e os dados do Keycloak ficam em
`.local/`, também ignorado.

Após alterar o código da extensão, execute `./setup.sh` novamente e reinicie
o Keycloak. O script confere o SHA-256 fixado para o ZIP antes de
extrair. A configuração do realm é repetível via `./configure.sh`.

Para compilar e executar os testes autorizados posteriormente:

```bash
mvn -f spi-keyclok/pom.xml test
```

O cache da federação é desativado para que bloqueios no SIG sejam refletidos
na próxima tentativa de login. O Keycloak não oferece troca de senha para
essas contas. O uso de MD5 reproduz o formato legado existente; a migração
de senha deve ocorrer na fonte.
