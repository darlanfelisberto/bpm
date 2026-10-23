#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
KEYCLOAK="$ROOT/.local/keycloak-26.8.1"
if [[ ! -f "$ROOT/local.env" ]]; then
    printf 'Crie %s a partir de local.env.example.\n' "$ROOT/local.env" >&2
    exit 1
fi
if [[ ! -x "$KEYCLOAK/bin/kc.sh" ]]; then
    printf 'Execute ./setup.sh antes de iniciar o Keycloak.\n' >&2
    exit 1
fi

set -a
source "$ROOT/local.env"
set +a
for name in KC_BOOTSTRAP_ADMIN_USERNAME KC_BOOTSTRAP_ADMIN_PASSWORD KC_DB_URL_FULL_SIGSTORE KC_DB_USERNAME_SIGSTORE KC_DB_PASSWORD_SIGSTORE; do
    if [[ -z "${!name:-}" ]]; then
        printf 'Defina %s em local.env.\n' "$name" >&2
        exit 1
    fi
done
export KC_DB=dev-file KC_DB_KIND_SIGSTORE=postgres
export KC_TRANSACTION_XA_ENABLED=true KC_TRANSACTION_XA_ENABLED_SIGSTORE=false
export KC_HOSTNAME=http://localhost:8081
exec "$KEYCLOAK/bin/kc.sh" start-dev --http-port=8081
