#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
VERSION=26.8.1
ZIP="$ROOT/.local/keycloak-$VERSION.zip"
KEYCLOAK="$ROOT/.local/keycloak-$VERSION"
SHA256=4cd312c889af358a819a74a6c6f068f9ea159a6959e49924607cec95cfd4c1a4

mkdir -p "$ROOT/.local"
if [[ ! -f "$ZIP" ]]; then
    curl --fail --location --show-error --silent --retry 3 \
        "https://github.com/keycloak/keycloak/releases/download/$VERSION/keycloak-$VERSION.zip" \
        --output "$ZIP"
fi
printf '%s  %s\n' "$SHA256" "$ZIP" | sha256sum --check --status
if [[ ! -x "$KEYCLOAK/bin/kc.sh" ]]; then
    unzip -q "$ZIP" -d "$ROOT/.local"
fi

mvn -q -f "$ROOT/pom.xml" -DskipTests package
cp "$ROOT/target/keycloak-sig-storage-1.0.0-SNAPSHOT.jar" "$KEYCLOAK/providers/"
KC_DB=dev-file KC_DB_KIND_SIGSTORE=postgres \
    KC_TRANSACTION_XA_ENABLED=true KC_TRANSACTION_XA_ENABLED_SIGSTORE=false \
    "$KEYCLOAK/bin/kc.sh" build

printf 'Keycloak instalado em %s\n' "$KEYCLOAK"
printf 'Copie local.env.example para local.env, ajuste os valores e execute ./start.sh\n'
