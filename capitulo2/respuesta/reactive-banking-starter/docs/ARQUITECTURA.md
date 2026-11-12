# Arquitectura — Laboratorio 2

```text
                     curl/Postman
                       /       \
                      v         v
                 :8081            :8082
              Account Service   Transaction Service
                 |                 | GET /api/transactions/account/{id}
                 |                 | GET /internal/verify-account/{id}
                 |                 |
      POST /api/accounts           | WebClient HTTP no bloqueante
      GET /api/accounts/{id} <-----+
                 |
         ConcurrentHashMap
      (cuentas de laboratorio)

      Transaction Service dispone de una LISTA LOCAL con 2 movimientos históricos simulados.
```

- **Responsabilidad de Cuentas:** existencia, estado, saldo. El catálogo usa un `ConcurrentHashMap` aislado.
- **Responsabilidad de Transacciones:** representar movimientos; aquí solo consulta datos de demostración, no los crea.
- **Relación:** Transaction Service consulta Account Service mediante WebClient. La URI depende de `ACCOUNT_SERVICE_URL`.
- **Fallos:** 404 de la cuenta -> 404 `ACCOUNT_NOT_FOUND`; timeout / conexión rechazada -> 503 `ACCOUNT_SERVICE_UNAVAILABLE`.
- **Correlación:** el filtro acepta `X-Correlation-Id` alfanumérico controlado o genera UUID, agrega el header de respuesta y lo propaga por WebClient. No equivale a tracing distribuido ni a autenticación.
- **Simulación:** el endpoint de cuentas acepta `delayMs` (solo con perfil `dev`) para inducir latencia no bloqueante. El timeout del cliente se externaliza con `ACCOUNT_TIMEOUT_MS`.
- **No hay** `POST /api/transactions`, saldos mutables, API Gateway, JWT, base de datos, Actuator, circuit breaker ni retry.

> Datos semilla: cuenta `11111111-1111-4111-8111-111111111111`. Saldo hipotético inicial histórico 145000 COP; depósito de 10000 y retiro de 5000 explican la fotografía de demostración de 150000 COP, pero **no son movimientos aplicados por este sistema**.
