#!/usr/bin/env bash
set -euo pipefail
BASE_ACCOUNT="${ACCOUNT_BASE_URL:-http://localhost:8081}"
BASE_TX="${TX_BASE_URL:-http://localhost:8082}"
ID="11111111-1111-4111-8111-111111111111"
UNKNOWN="99999999-9999-4999-8999-999999999999"
OUT="$(mktemp)"; trap 'rm -f "$OUT"' EXIT

check() {
  local title="$1" expected="$2" pattern="$3"; shift 3
  local status
  status="$(curl -sS --max-time 8 -o "$OUT" -w '%{http_code}' "$@")"
  if [[ "$status" != "$expected" ]]; then
    printf 'ERROR  %-28s HTTP %s esperado=%s\n' "$title" "$status" "$expected"
    cat "$OUT"; printf '\n'; exit 1
  fi
  if ! grep -Fq -- "$pattern" "$OUT"; then
    printf 'ERROR  %-28s No aparece: %s\n' "$title" "$pattern"
    cat "$OUT"; printf '\n'; exit 1
  fi
  printf 'OK     %-28s HTTP %s\n' "$title" "$status"
}

echo '=== LAB02 Validación HTTP (perfil dev en ambos servicios) ==='
check 'Status cuentas' 200 '"status":"UP"' "$BASE_ACCOUNT/internal/status"
check 'Status movimientos' 200 '"status":"UP"' "$BASE_TX/internal/status"
check 'GET cuenta semilla' 200 '"accountId":"11111111-1111-4111-8111-111111111111"' "$BASE_ACCOUNT/api/accounts/$ID"
check 'POST cuenta' 201 '"balance":"0.00"' \
  -H 'Content-Type: application/json' -d '{"holderName":"Cliente Prueba LAB02","currency":"COP"}' "$BASE_ACCOUNT/api/accounts"
check 'POST cuenta moneda invalida' 400 '"code":"INVALID_REQUEST"' \
  -H 'Content-Type: application/json' -d '{"holderName":"Cliente","currency":"USD"}' "$BASE_ACCOUNT/api/accounts"
check 'GET movimientos ficticios' 200 '"type":"DEPOSIT"' "$BASE_TX/api/transactions/account/$ID"
check 'WebClient cuenta existente' 200 '"exists":true' \
  -H 'X-Correlation-Id: lab02-check-001' "$BASE_TX/internal/verify-account/$ID"
check 'WebClient cuenta ausente' 404 '"code":"ACCOUNT_NOT_FOUND"' "$BASE_TX/internal/verify-account/$UNKNOWN"
check 'Timeout simulado (dev)' 503 '"code":"ACCOUNT_SERVICE_UNAVAILABLE"' \
  "$BASE_TX/internal/verify-account/$ID?delayMs=1500"
echo '=== VALIDACION HTTP COMPLETADA ==='
echo 'Comprueba a mano: header X-Correlation-Id y coincidencia en ambos logs.'
echo 'Comprueba a mano: detener Account Service => verificación 503; GET movimientos 200.'
