# Contratos REST — Sistema bancario reactivo (v1 didáctica)

**ESTADO:** diseño aprobado; **no implementado en Laboratorio 1**.  
**Objetivo:** permitir construir las funcionalidades de los laboratorios 2–5 sin redefinir las rutas.  
**Base URLs locales:** `http://localhost:8081` (cuentas) y `http://localhost:8082` (transacciones).  
**Encabezados:** `Content-Type: application/json` para POST; el registro de movimientos requiere `Idempotency-Key` cuando se implemente.

## Propiedad del dominio

- **account-service** posee identificador, titular ficticio, estado y **saldo**. Solo este servicio puede autorizar y aplicar cambios de saldo.
- **transaction-service** registra movimientos, su tipo y estado. **Nunca** modifica una base de datos ajena ni replica datos sensibles de titulares.
- La coordinación transacción–saldo, la atomicidad, la consistencia y las garantías de idempotencia quedan **pendientes de diseño e implementación** para etapas siguientes. No se afirma que exista una transferencia bancaria real.
- Estos datos y nombres son **ficticios**. Los ejemplos se representan como cadenas decimales exactas y se tratarán internamente con `BigDecimal` cuando se implemente la lógica monetaria.

## Modelo de Account

| Campo | Tipo en contrato | Regla |
|---|---|---|
| `accountId` | UUID (string) | generado por el servicio |
| `holderName` | string | no vacío; ejemplo ficticio |
| `balance` | decimal (string) | no negativo, 2 decimales para COP |
| `currency` | string | exclusivamente `COP` en esta práctica |
| `status` | string | `ACTIVE` o `BLOCKED` |
| `createdAt` | ISO-8601 UTC string | fecha de creación |

### POST /api/accounts

**Solicitud** (`holderName` y `currency` obligatorios):

```json
{
  "holderName": "Cliente Demostracion",
  "currency": "COP"
}
```

**201 Created** (saldo inicial cero):

```json
{
  "accountId": "11111111-1111-4111-8111-111111111111",
  "holderName": "Cliente Demostracion",
  "balance": "0.00",
  "currency": "COP",
  "status": "ACTIVE",
  "createdAt": "2026-11-04T15:00:00Z"
}
```

La implementación futura añadirá `Location: /api/accounts/{id}`. Errores previstos: `400 INVALID_REQUEST`, `422 VALIDATION_ERROR`.

### GET /api/accounts/{id}

**200 OK**:

```json
{
  "accountId": "11111111-1111-4111-8111-111111111111",
  "holderName": "Cliente Demostracion",
  "balance": "150000.00",
  "currency": "COP",
  "status": "ACTIVE",
  "createdAt": "2026-11-04T15:00:00Z"
}
```

Errores previstos: `400 INVALID_REQUEST` para identificador mal formado; `404 ACCOUNT_NOT_FOUND` si no existe.

## Modelo de Transaction

| Campo | Tipo en contrato | Regla |
|---|---|---|
| `transactionId` | UUID (string) | generado por transacciones |
| `accountId` | UUID (string) | referencia por contrato, sin FK entre servicios |
| `type` | string | `DEPOSIT`, `WITHDRAWAL` |
| `amount` | decimal (string) | > 0, 2 decimales |
| `currency` | string | `COP` |
| `status` | string | `COMPLETED`, `REJECTED` (estados finales simplificados) |
| `createdAt` | ISO-8601 UTC string | fecha de registro |
| `correlationId` | string | identificador de correlación en la respuesta de registro |

### POST /api/transactions

**Encabezados**:

```
Content-Type: application/json
Idempotency-Key: movimiento-demo-001
```

**Solicitud**:

```json
{
  "accountId": "11111111-1111-4111-8111-111111111111",
  "type": "DEPOSIT",
  "amount": "50000.00",
  "currency": "COP"
}
```

**201 Created** — **únicamente** si el movimiento fue realmente aplicado y confirmado:

```json
{
  "transactionId": "22222222-2222-4222-8222-222222222222",
  "accountId": "11111111-1111-4111-8111-111111111111",
  "type": "DEPOSIT",
  "amount": "50000.00",
  "currency": "COP",
  "status": "COMPLETED",
  "createdAt": "2026-11-04T15:10:00Z",
  "correlationId": "demo-request-001"
}
```

**Pendiente para implementación:** mecanismo de actualización de saldo sin inconsistencias, deduplicación durable para `Idempotency-Key` y respuesta idempotente idéntica en reintentos. No aplicar `retry` a escrituras sin esas garantías.

**Errores previstos:** 400 `INVALID_REQUEST`, 404 `ACCOUNT_NOT_FOUND`, 409 `ACCOUNT_BLOCKED` o `IDEMPOTENCY_CONFLICT`, 422 `INSUFFICIENT_FUNDS` o `VALIDATION_ERROR`, 503 `ACCOUNT_SERVICE_UNAVAILABLE`.

### GET /api/transactions/account/{accountId}

**200 OK**:

```json
[
  {
    "transactionId": "22222222-2222-4222-8222-222222222222",
    "accountId": "11111111-1111-4111-8111-111111111111",
    "type": "DEPOSIT",
    "amount": "50000.00",
    "currency": "COP",
    "status": "COMPLETED",
    "createdAt": "2026-11-04T15:10:00Z"
  }
]
```

Si no hay movimientos se devuelve `[]` con HTTP 200; esto **no confirma** que la cuenta exista. Para un UUID mal formado: `400 INVALID_REQUEST`. La respuesta de listado no incluye `correlationId` por movimiento en esta versión del contrato.

## Contrato común de errores (para implementación posterior)

```json
{
  "code": "ACCOUNT_NOT_FOUND",
  "message": "La cuenta solicitada no existe",
  "status": 404,
  "correlationId": "demo-request-001",
  "timestamp": "2026-11-04T15:12:00Z"
}
```

| HTTP | `code` | Motivo |
|---|---|---|
| 400 | `INVALID_REQUEST` | JSON o UUID mal formados |
| 404 | `ACCOUNT_NOT_FOUND` | Cuenta ausente |
| 409 | `ACCOUNT_BLOCKED` | Cuenta bloqueada |
| 409 | `IDEMPOTENCY_CONFLICT` | Misma clave con payload diferente |
| 422 | `INSUFFICIENT_FUNDS` | Retiro que excede saldo |
| 422 | `VALIDATION_ERROR` | Importe, moneda o campos inválidos |
| 503 | `ACCOUNT_SERVICE_UNAVAILABLE` | Dependencia no disponible |

**Fuera de este laboratorio:** JWT, 401/403, verificación real de seguridad, persistencia, timeouts y trazabilidad distribuida. Los cuerpos, el estado HTTP y el registro de errores deben implementarse y probarse después.

## Endpoint técnico (el único implementado en Lab 1)

`GET /internal/status` en **cada** servicio:

```json
{
  "service": "account-service",
  "status": "UP",
  "environment": "dev"
}
```

En el servicio de transacciones cambia `service` a `transaction-service`. Este valor `UP` es solo demostrativo y **no es** una comprobación de salud/ready real de dependencias; Actuator se revisará más adelante.
