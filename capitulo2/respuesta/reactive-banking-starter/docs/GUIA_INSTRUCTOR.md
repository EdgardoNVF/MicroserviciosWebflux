# LAB02 — Guía del instructor

**Contrato de tiempo: 95 minutos.** Conservar la estructura Maven y los cuatro contratos del Lab 1. No mezclar con temas de capítulos 3 y 4.

## Antes de impartir

1. Ejecutar en Ubuntu/Windows una compilación de referencia (`mvn clean package`). Confirmar JDK 21, Maven 3.9.x, puertos 8081/8082 libres y dependencias Maven descargadas.
2. Probar ambos servicios en perfil `dev`; correr scripts en Linux y Windows si se dispone de ambos.
3. Entregar solo `LAB02_WebFlux_starter.zip` y la guía del alumno. Conservar `LAB02_WebFlux_referencia.zip` como recuperación.
4. Iniciar Lab 2 partiendo de la **solución del Lab 1**; no pedirle al alumno que vuelva a crear el proyecto.

## Guion y tiempos

| Bloque | Min | Acción instructor | Evidencia alumno |
|---|---:|---|---|
| Recuperación Lab 1 | 10 | Revisar carpetas, contratos y propietarios | Distingue cuentas de movimientos |
| Implementar Cuentas | 25 | Explicar `Mono.fromSupplier`, estado y moneda | `POST 201`, `GET 200` |
| Consulta movimientos | 10 | Explicar `Flux.fromIterable` y `filter` | `GET` con 2 movimientos, sin persistencia |
| WebClient | 20 | Dar URI, header, `retrieve`, `onStatus`, timeout | Verificación de cuenta 200 |
| Fallos | 15 | Simular cuenta ausente y latencia con `delayMs` | 404 y 503 con códigos JSON |
| Correlación | 15 | Revisar headers/logs; caída controlada de dependencia | ID visible en dos servicios |
| **TOTAL** | **95** | | |

## Dos TODO deliberados

- **LAB02-A** `account-service/.../service/AccountCatalog.java` -> reemplazar método `create` por `Mono.fromSupplier` con validaciones y escritura en `ConcurrentHashMap`.
- **LAB02-B** `transaction-service/.../service/AccountLookupClient.java` -> reemplazar `Mono.error(...)` por pipeline WebClient, mapping de 404, timeout y wrapping de errores.

Los controladores HTTP, DTOs, filtros de correlación y handlers de error se entregan completos para respetar el tiempo de clase. La ruta de movimiento demuestra un `Flux` con datos históricos precargados; NO produce una transacción real.

## Secuencia de validación recomendada

1. `mvn -pl account-service -am test`: las pruebas del starter fallan en creación hasta completar A.
2. `mvn -pl transaction-service -am test`: las pruebas del starter fallan en WebClient hasta completar B.
3. `mvn clean package`: las pruebas de la solución deben pasar.
4. Ejecutar JAR con `--spring.profiles.active=dev` en dos terminales.
5. `bash scripts/validate-lab02.sh` o PowerShell.
6. Inspeccionar 404; demo `?delayMs=1500` con timeout de 500 ms -> 503.
7. Detener Account Service: verificación -> 503; `GET /api/transactions/account/...` sigue activo -> 200.
8. Reiniciar y comprobar restauración del servicio.

## Diagnóstico docente

- Si la compilación falla por import de Reactor, comprobar `spring-boot-starter-webflux`, no agregar bloqueos.
- Si JSON inválido da 500, revisar `AccountErrorHandler`/`TransactionErrorHandler`.
- Si error 404 se traduce erróneamente a 503, revisar el predicado de `.onErrorMap`.
- Si timeout no se reproduce, confirmar `ACCOUNT_TIMEOUT_MS=500`, `delayMs=1500`, perfil `dev` en Account Service y Transaction Service.
- Si correlación no coincide, revisar header de solicitud y `.header("X-Correlation-Id", correlationId)` en WebClient.
- Si Maven no alcanza Central, detener el ejercicio y entregar entorno con dependencias precargadas; no perder el bloque de clase instalando software.

## Rúbrica sugerida (10 puntos)

- GET/POST de cuenta + reglas: 3 puntos.
- GET movimientos con Flux: 1 punto.
- WebClient: 2 puntos.
- 404 y timeout 503: 2 puntos.
- Correlación entre servicios: 1 punto.
- Evidencia de tests/arranque: 1 punto.

## Limitación funcional explícita

Las transacciones son fixtures históricos. No hay POST `/api/transactions`, transferencias, idempotencia, seguridad, autorización ni garantías de consistencia de saldo. No debe presentarse como core bancario real. El `correlationId` no autentica usuarios. El switch `demo-delay-enabled` solo habilita latencia deliberada en `dev`.

## Recuperación

Conservar ambos ZIP (starter y referencia). Una persona que se quede atrás puede recuperar solo los archivos de los dos TODO desde la referencia, sin deshacer sus ejercicios del Lab 1. **Los scripts no ejecutan limpieza destructiva ni detienen procesos.**

## Estado de verificación de esta entrega

Se inspeccionaron estructura, XML, YAML, archivos fuente y scripts de manera estática. En el entorno de generación no están disponibles Maven ni los artefactos remotos: **las pruebas Java y la ejecución de ambos JAR requieren validación real en una máquina de clase**.
