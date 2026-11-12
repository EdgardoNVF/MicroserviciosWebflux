#!/usr/bin/env bash
set -euo pipefail

check() {
  local name="$1" port="$2" result
  result="$(curl --fail --silent --show-error --max-time 6 "http://localhost:${port}/internal/status")"
  echo "${name}: ${result}"
  echo "$result" | grep -Eq '"service"[[:space:]]*:[[:space:]]*"'"${name}"'"' || { echo "ERROR: nombre inesperado" >&2; return 1; }
  echo "$result" | grep -Eq '"status"[[:space:]]*:[[:space:]]*"UP"' || { echo "ERROR: status != UP" >&2; return 1; }
  echo "$result" | grep -Eq '"environment"[[:space:]]*:[[:space:]]*"dev"' || { echo "ERROR: environment != dev" >&2; return 1; }
}
check account-service 8081
check transaction-service 8082
echo "OK: ambos servicios responden correctamente"
