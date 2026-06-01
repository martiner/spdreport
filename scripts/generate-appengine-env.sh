#!/usr/bin/env bash
# Generates target/appengine/app.yaml from the src/main/appengine template, adding one
# env_variables entry per ${UPPER_SNAKE} placeholder found in application.properties — the
# single source of truth for required secrets. Values come from the environment; the deploy
# fails fast if any referenced env var is unset. Invoked from pom.xml during the deploy phase.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
PROPS="$ROOT/src/main/resources/application.properties"
SRC_DIR="$ROOT/src/main/appengine"
OUT_DIR="$ROOT/target/appengine"

mkdir -p "$OUT_DIR"
cp -R "$SRC_DIR"/. "$OUT_DIR"/

# Required secret env vars = ${NAME} placeholders (UPPER_SNAKE, no default) in the properties.
vars=$(grep -oE '\$\{[A-Z][A-Z0-9_]*\}' "$PROPS" | sed -E 's/^\$\{(.*)\}$/\1/' | sort -u)

missing=()
block="env_variables:"
while IFS= read -r v; do
    [ -z "$v" ] && continue
    val="${!v:-}"
    if [ -z "$val" ]; then
        missing+=("$v")
        continue
    fi
    esc=${val//\'/\'\'}
    block+=$'\n'"  $v: '$esc'"
done <<< "$vars"

if [ ${#missing[@]} -gt 0 ]; then
    echo "ERROR: required secret env var(s) not set for deploy: ${missing[*]}" >&2
    echo "They are referenced in application.properties; set them locally or add the matching GitHub secret." >&2
    exit 1
fi

if [ -n "$vars" ]; then
    printf '\n%s\n' "$block" >> "$OUT_DIR/app.yaml"
fi
echo "Generated env_variables for:" $vars
