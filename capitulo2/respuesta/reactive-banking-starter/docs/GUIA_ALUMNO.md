# LAB02 — Manual de construcción paso a paso (alumnos)

**Duración máxima en clase: 95 minutos.** Punto de partida: solución del Lab 1, con `account-service` y `transaction-service` arrancando y respondiendo `/internal/status`.

## Objetivos observables

1. Implementar `POST /api/accounts` y consultar `GET /api/accounts/{id}` usando `Mono`.
2. Consultar movimientos ficticios mediante un `Flux` sin persistencia.
3. Hacer una llamada HTTP **no bloqueante** entre servicios con `WebClient`.
4. Traducir un 404, un timeout y una dependencia caída a respuestas JSON consistentes.
5. Propagar `X-Correlation-Id` en llamada, respuesta y logs.

## Antes de clase (NO cuenta en los 95 min)

Instalar Java 21, Maven 3.9.x, editor e internet/repositorio para Maven Central. Prueba `java -version`, `javac -version`, `mvn -version`. Descarga el starter ZIP. No instalar Docker, PostgreSQL ni Kubernetes. Si estás en Windows, usa `curl.exe` en lugar del alias `curl` de PowerShell.

## PASO 1 — Recuperar el laboratorio anterior (10 min)

1. Extrae `LAB02_WebFlux_starter.zip` y abre `reactive-banking-starter`.
2. Inspecciona `pom.xml`, `account-service/` y `transaction-service/`.
3. Confirma que ambos conservan `StatusController.java` de Lab 1.
4. Abre `docs/ARQUITECTURA.md` y ubica la flecha de **Transacciones -> Cuentas**.
5. Revisa los contratos aprobados en `docs/CONTRATOS_REST.md` y su estado en `docs/ESTADO_CONTRATOS_LAB02.md`.

**Evidencia:** explica qué servicio es autoridad sobre saldo; no existe base de datos compartida.

## PASO 2 — Implementar la creación de cuentas (25 min)

Archivo: `account-service/src/main/java/com/bancolombia/training/accounts/service/AccountCatalog.java`.

- La consulta `find(...)` y el controlador ya están conectados.
- Ubica **`TODO LAB02-A`** dentro de `create(CreateAccountRequest request)`.
- Sustituye el retorno temporal por este comportamiento:
  1. Acepta `holderName` no vacío (máximo 120 caracteres) y `currency` igual a `COP`.
  2. Genera un UUID, saldo inicial `"0.00"`, estado `"ACTIVE"` y fecha UTC con `Instant.now()`.
  3. Guarda la nueva cuenta en el mapa local.
  4. Devuelve el registro dentro de `Mono.fromSupplier`.

**Guía para completar:**

```java
return Mono.fromSupplier(() -> {
    if (request == null || request.holderName() == null
        || request.holderName().isBlank() || request.holderName().length() > 120) {
        throw new InvalidRequestException("holderName debe contener entre 1 y 120 caracteres");
    }
    if (!"COP".equals(request.currency())) {
        throw new InvalidRequestException("Solo se admite currency=COP en este laboratorio");
    }
    UUID id = UUID.randomUUID();
    Account created = new Account(id, request.holderName().trim(), "0.00",
        "COP", "ACTIVE", Instant.now());
    accounts.put(id, created);
    return created;
});
```

El starter ya incluye los imports, el modelo `Account`, la clase `InvalidRequestException` y el mapa `accounts`.

**Ejecuta solo pruebas del módulo de cuentas** desde raíz:

```bash
mvn -pl account-service -am test
```

Esperado: `AccountCatalogTest` valida semilla, creación y moneda inválida.

## PASO 3 — Revisar consultas de movimientos con Flux (10 min)

Archivo: `transaction-service/src/main/java/com/bancolombia/training/transactions/service/TransactionCatalog.java`.

1. Revisa cómo `Flux.fromIterable(demo)` emite movimientos.
2. Explica cómo `.filter(...)` selecciona el `accountId` solicitado.
3. Reconoce que la lista es **una fotografía histórica ficticia**; no hay API para registrar ni aplicar dinero.
4. Más adelante consultarás `/api/transactions/account/{accountId}`; no cambies el contrato.

**Pregunta:** ¿por qué una cuenta sin movimientos produce `[]` y no `404`?

## PASO 4 — Construir comunicación WebClient (20 min)

Archivo: `transaction-service/src/main/java/com/bancolombia/training/transactions/service/AccountLookupClient.java`.

Localiza **`TODO LAB02-B`**. Construye la cadena reactiva de forma que:

- Haga `GET /api/accounts/{id}` al host `services.account.url` (configurado en `AccountClientConfig`).
- Propague el header `X-Correlation-Id`.
- En demo, agregue `delayMs` solo cuando sea mayor que cero.
- Convierta un `404` en `AccountNotFoundException`.
- Deserialice JSON en el DTO **local** `AccountView`.
- Aplique `.timeout(timeout)` y envuelva otras fallas en `AccountServiceUnavailableException`.

**Cadena de referencia para construir (completa el método):**

```java
return client.get()
    .uri(builder -> {
        builder.path("/api/accounts/{id}");
        if (demoDelayMs > 0) builder.queryParam("delayMs", demoDelayMs);
        return builder.build(id);
    })
    .header("X-Correlation-Id", correlationId)
    .retrieve()
    .onStatus(status -> status.value() == 404,
        response -> Mono.error(new AccountNotFoundException()))
    .bodyToMono(AccountView.class)
    .timeout(timeout)
    .onErrorMap(error -> !(error instanceof AccountNotFoundException)
        && !(error instanceof AccountServiceUnavailableException),
        AccountServiceUnavailableException::new);
```

> **No uses `.block()`, `.subscribe()` manual ni `Thread.sleep()`** dentro del pipeline HTTP. El framework realiza la suscripción y `Mono.delay` simula latencia de forma no bloqueante.

**Prueba aislada WebClient y Flux:**

```bash
mvn -pl transaction-service -am test
```

`AccountLookupClientTest` usa un `ExchangeFunction` de prueba y no requiere iniciar Account Service.

## PASO 5 — Errores y timeout (15 min)

1. Revisa `AccountErrorHandler` y `TransactionErrorHandler`: ambos usan la estructura `ApiError`.
2. Ejecuta desde raíz:

```bash
mvn clean package
```

3. Abre dos terminales desde la **raíz**:

Terminal A:

```bash
java -jar account-service/target/account-service-0.1.0.jar --spring.profiles.active=dev
```

Terminal B:

```bash
java -jar transaction-service/target/transaction-service-0.1.0.jar --spring.profiles.active=dev
```

En tercera terminal (Ubuntu):

```bash
ID=11111111-1111-4111-8111-111111111111
curl -i http://localhost:8081/api/accounts/$ID
curl -i http://localhost:8082/api/transactions/account/$ID
curl -i http://localhost:8082/internal/verify-account/$ID
# Cuenta ausente => 404
curl -i http://localhost:8082/internal/verify-account/99999999-9999-4999-8999-999999999999
# Simulación de demora: 1500 ms > timeout 500 ms => 503
curl -i "http://localhost:8082/internal/verify-account/$ID?delayMs=1500"
```

En Windows PowerShell, usa el ID literal y `curl.exe -i "http://localhost:8082/..."`. Puedes cambiar los parámetros con `ACCOUNT_TIMEOUT_MS`, `ACCOUNT_SERVICE_URL` y el perfil dev.

**Diagnóstico:** si ambas peticiones devuelven 503, verifica que Account Service esté activo y que la URL sea `http://localhost:8081`.

## PASO 6 — Correlación y validación (15 min)

```bash
curl -i -H "X-Correlation-Id: lab02-demo-01" \
  http://localhost:8082/internal/verify-account/11111111-1111-4111-8111-111111111111
bash scripts/validate-lab02.sh
# Windows: .\scripts\validate-lab02.ps1
```

Revisa los logs de **ambos procesos**: el mismo `correlationId` debe aparecer en las líneas de entrada/salida. Comprueba también el header de respuesta `X-Correlation-Id`.

**Fallos controlados:** detén SOLO `account-service` con `Ctrl+C`, repite la verificación y espera `503`, pero la consulta local de movimientos de Transaction Service todavía debe devolver `200`. Vuelve a levantar Account Service al finalizar. El script automático no mata procesos.

### Evidencias que debe entregar el alumno

| Criterio | Resultado a registrar |
|---|---|
| Cuentas GET y POST | Respuestas 200 y 201 |
| GET movimientos semilla | 200 con 2 registros |
| WebClient -> Cuentas | 200, `exists=true` |
| Cuenta inexistente | 404, `ACCOUNT_NOT_FOUND` |
| Timeout y dependencia caída | 503, `ACCOUNT_SERVICE_UNAVAILABLE` |
| Correlación | mismo `correlationId` en ambos logs |
| Tests de ambos módulos | BUILD SUCCESS |

## Recuperación / solución de errores

- `BUILD FAILURE` inicial en starter: esperado hasta completar `TODO LAB02-A` y `TODO LAB02-B`.
- `404` al invocar POST `/api/transactions`: correcto; NO está implementado.
- Puerto ocupado: revisa procesos anteriores en 8081/8082.
- 400 por `delayMs`: asegúrate de usar `--spring.profiles.active=dev` y de que el parámetro esté entre 0 y 3000.
- 503 inesperado: valida `ACCOUNT_SERVICE_URL`, disponibilidad del puerto 8081 y `ACCOUNT_TIMEOUT_MS`.
- Después de cambiar código, vuelve a compilar y reinicia procesos; no basta con recargar curl.
- Sin acceso a Maven Central: requiere precarga de dependencias por el instructor ANTES de clase.
- Un saldo `150000.00` no cambia: por diseño; el sistema todavía no ejecuta transferencias ni depósitos.

**Fin del Laboratorio 2:** no añadas seguridad, gateway, base de datos ni escritura de transacciones en esta práctica.
