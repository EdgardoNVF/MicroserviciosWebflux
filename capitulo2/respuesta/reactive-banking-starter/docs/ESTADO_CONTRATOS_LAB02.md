# Estado de implementación — Laboratorio 2

**Los contratos aprobados en Lab 1 se conservan** en `docs/CONTRATOS_REST.md`.

| Servicio | HTTP | Ruta | Estado en Lab 2 |
|---|---|---|---|
| Cuentas | POST | `/api/accounts` | Implementado en memoria; crea saldo `"0.00"` |
| Cuentas | GET | `/api/accounts/{id}` | Implementado en memoria; 404 si no existe |
| Transacciones | GET | `/api/transactions/account/{accountId}` | Implementado con `Flux` de movimientos históricos ficticios |
| Transacciones | POST | `/api/transactions` | **NO IMPLEMENTADO**; reservado para etapa posterior |
| Transacciones | GET | `/internal/verify-account/{accountId}` | **Nuevo endpoint didáctico interno** (WebClient) |
| Ambos | GET | `/internal/status` | Conservado de Lab 1 |

## Ejemplos de contrato implementados

### Crear cuenta (201)

`POST http://localhost:8081/api/accounts`

```json
{"holderName":"Cliente de Prueba","currency":"COP"}
```

Respuesta (IDs y fecha variables):

```json
{
  "accountId":"123e4567-e89b-42d3-a456-426614174000",
  "holderName":"Cliente de Prueba",
  "balance":"0.00",
  "currency":"COP",
  "status":"ACTIVE",
  "createdAt":"2026-11-04T15:00:00Z"
}
```

La respuesta de ejemplo es ilustrativa: el UUID y `createdAt` se generan en cada solicitud.

### Consultar cuenta (200)

`GET http://localhost:8081/api/accounts/11111111-1111-4111-8111-111111111111`

Respuesta de la semilla: `accountId`, `holderName`, `balance: "150000.00"`, `currency: "COP"`, `status: "ACTIVE"`, `createdAt`.

### Consultar movimientos simulados (200)

`GET http://localhost:8082/api/transactions/account/11111111-1111-4111-8111-111111111111`

Lista de 2 movimientos **precargados**, uno `DEPOSIT` de `"10000.00"` y uno `WITHDRAWAL` de `"5000.00"`. Para una cuenta distinta retorna `[]`.

### Verificación interna de cuenta (200)

`GET http://localhost:8082/internal/verify-account/11111111-1111-4111-8111-111111111111`

```json
{
  "accountId":"11111111-1111-4111-8111-111111111111",
  "exists":true,
  "accountStatus":"ACTIVE",
  "currency":"COP",
  "correlationId":"demo-lab02-001"
}
```

El `correlationId` anterior depende del header HTTP de solicitud: `X-Correlation-Id: demo-lab02-001`. Si falta, se genera uno automáticamente. Se conserva también en el header de respuesta.

### Error unificado (404 o 503)

```json
{
  "code":"ACCOUNT_NOT_FOUND",
  "message":"La cuenta solicitada no existe",
  "status":404,
  "correlationId":"demo-lab02-001",
  "timestamp":"2026-11-04T15:12:00Z"
}
```

- Identificador inexistente => `404 ACCOUNT_NOT_FOUND`.
- JSON, parámetro o moneda inválidos => `400 INVALID_REQUEST`.
- Account Service caído o timeout => `503 ACCOUNT_SERVICE_UNAVAILABLE` desde `/internal/verify-account/...`.
- `GET /api/transactions/account/{accountId}` no consulta Account Service: con ID válido y sin movimientos responde `200 []` incluso si la cuenta no existe (contrato acordado en Lab 1).

## Timeouts y fallos de demostración

**Solo en perfil dev**: agrega `?delayMs=1500` al endpoint interno de transacciones. Este valor se envía a Account Service; `Mono.delay` retrasa la respuesta sin `Thread.sleep`. El timeout por defecto de WebClient es `500ms`, por lo que se espera `503`.

No se incluye seguridad ni persistencia, y **una respuesta 503 no debe interpretarse como rechazo de negocio**.
