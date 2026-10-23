#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
KEYCLOAK="$ROOT/.local/keycloak-26.8.1"
if [[ ! -f "$ROOT/local.env" || ! -x "$KEYCLOAK/bin/kcadm.sh" ]]; then
    printf 'Execute ./setup.sh e crie local.env antes de configurar o realm.\n' >&2
    exit 1
fi
set -a
source "$ROOT/local.env"
set +a
for name in KC_BOOTSTRAP_ADMIN_USERNAME KC_BOOTSTRAP_ADMIN_PASSWORD BPM_CLIENT_SECRET; do
    if [[ -z "${!name:-}" ]]; then
        printf 'Defina %s em local.env.\n' "$name" >&2
        exit 1
    fi
done

umask 077
KCADM="$KEYCLOAK/bin/kcadm.sh"
CONFIG="$ROOT/.local/kcadm.config"
BASE=http://localhost:8081
"$KCADM" config credentials --config "$CONFIG" --server "$BASE" --realm master \
    --user "$KC_BOOTSTRAP_ADMIN_USERNAME" --password "$KC_BOOTSTRAP_ADMIN_PASSWORD"

if ! "$KCADM" get realms/bpm --config "$CONFIG" >/dev/null 2>&1; then
    "$KCADM" create realms --config "$CONFIG" -s realm=bpm -s enabled=true \
        -s loginWithEmailAllowed=false -s registrationAllowed=false \
        -s resetPasswordAllowed=false
fi
"$KCADM" update realms/bpm --config "$CONFIG" -s enabled=true \
    -s loginWithEmailAllowed=false -s registrationAllowed=false \
    -s resetPasswordAllowed=false
REALM_ID="$("$KCADM" get realms/bpm --config "$CONFIG" | jq -r '.id')"

CLIENT_ID="$("$KCADM" get clients --config "$CONFIG" -r bpm -q clientId=bpm \
    | jq -r '.[] | select(.clientId == "bpm") | .id' | head -n 1)"
CLIENT_SETTINGS=(--config "$CONFIG" -r bpm -s clientId=bpm -s enabled=true
    -s publicClient=false -s standardFlowEnabled=true -s directAccessGrantsEnabled=false
    -s serviceAccountsEnabled=false -s clientAuthenticatorType=client-secret
    -s 'redirectUris=["http://localhost:9080/oidcclient/redirect/bpm"]'
    -s 'webOrigins=["http://localhost:9080"]'
    -s "secret=$BPM_CLIENT_SECRET")
if [[ -z "$CLIENT_ID" ]]; then
    "$KCADM" create clients "${CLIENT_SETTINGS[@]}"
else
    "$KCADM" update "clients/$CLIENT_ID" "${CLIENT_SETTINGS[@]}"
fi

PROVIDER_ID="$("$KCADM" get components --config "$CONFIG" -r bpm \
    -q "parent=$REALM_ID" -q type=org.keycloak.storage.UserStorageProvider \
    | jq -r '.[] | select(.providerId == "sig-postgres") | .id' | head -n 1)"
if [[ -z "$PROVIDER_ID" ]]; then
    "$KCADM" create components --config "$CONFIG" -r bpm -s name=SIG \
        -s providerId=sig-postgres -s providerType=org.keycloak.storage.UserStorageProvider \
        -s "parentId=$REALM_ID" -s 'config.cachePolicy=["NO_CACHE"]'
else
    "$KCADM" update "components/$PROVIDER_ID" --config "$CONFIG" -r bpm \
        -s 'config.cachePolicy=["NO_CACHE"]'
fi

printf 'Realm bpm, cliente bpm e federacao SIG configurados em %s\n' "$BASE"
