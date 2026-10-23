#!/usr/bin/env bash
set -eu

cd "$(dirname "$0")"

if [ ! -f .env ]; then
    echo "Arquivo bpm-app/.env não encontrado. Copie .env.example e configure as credenciais." >&2
    exit 1
fi

set -a
. ./.env
set +a

for nome in APP_DB_USER APP_DB_PASSWORD OIDC_CLIENT_ID OIDC_CLIENT_SECRET OIDC_PROVIDER_BASE_URL OIDC_ISSUER OIDC_REDIRECT_BASE_URL; do
    if [[ -z ${!nome:-} ]]; then
        echo "Variável $nome não configurada em bpm-app/.env." >&2
        exit 1
    fi
done

exec mvn "$@" -o liberty:dev
