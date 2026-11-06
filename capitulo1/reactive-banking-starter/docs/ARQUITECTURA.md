# Arquitectura base — Laboratorio 1

```text
           +------------------------------+
           | Cliente: curl / Postman      |
           +---------------+--------------+
                           |
                  (llamadas directas)
            +--------------+--------------+
            |                             |
    +-------v---------+           +-------v-------------+
    | account-service |           | transaction-service |
    | Spring WebFlux  |           | Spring WebFlux      |
    | :8081           |           | :8082               |
    | /internal/status|           | /internal/status    |
    +-----------------+           +---------------------+
```

El **API Gateway** es un límite de borde que se discutirá conceptualmente; no se despliega aquí. Los servicios no se llaman entre sí en esta fase.

## Límites del dominio

**Cuentas:** posee datos de la cuenta, estado y saldo; toma las decisiones que deben conservar la integridad del saldo.

**Transacciones:** identifica la cuenta involucrada, registra movimiento y resultado. En una fase posterior se comunicará con cuentas por API, sin acceso a tablas compartidas.

**Contrato sobre implementación:** los endpoints de negocio están definidos en `CONTRATOS_REST.md`, pero NO se exponen en Lab 1. Una respuesta 404 al llamarlos en esta etapa es esperada.

## Decisiones de diseño

1. Los módulos se compilan conjuntamente mediante reactor Maven, pero cada uno produce y ejecuta su JAR independiente.
2. No se comparte librería de dominio ni persistencia; las estructuras JSON son contratos públicos, no acceso a entidades de otro servicio.
3. Configuración externalizada en `application.yml` y `application-dev.yml`.
4. `GET /internal/status` es didáctico. No mide conectividad ni salud real.
5. La implementación monetaria exige reglas consistentes e idempotencia que se diseñarán y probarán antes de aceptar operaciones reales.

## Actividad de análisis

Identifica, en la siguiente lista, el servicio dueño: `balance`, `holderName`, `transactionId`, `type`, `status de la cuenta`, `registro del movimiento`.

**Respuesta orientativa:** Account = balance, holderName, estado de la cuenta; Transaction = transactionId, type, registro del movimiento.
