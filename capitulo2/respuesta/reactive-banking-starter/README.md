# LAB02 — STARTER para alumnos (WebFlux / WebClient)

**Duración de clase:** 95 minutos. **Base:** solución del Laboratorio 1 (Java 21, Spring Boot 4.1.2, Maven 3.9.x).

Este repositorio Maven multimódulo contiene:

- `account-service` — puerto **8081**, datos de cuentas en memoria, `POST /api/accounts` y `GET /api/accounts/{id}`.
- `transaction-service` — puerto **8082**, consulta de movimientos *de demostración* y llamada WebClient a Account Service.
- `docs/GUIA_ALUMNO.md` — procedimiento completo de 95 min.
- `docs/GUIA_INSTRUCTOR.md` — guía de ejecución y fallos controlados.
- `docs/CONTRATOS_REST.md` — contratos aprobados (incluye endpoints pendientes).
- `scripts/validate-lab02.sh` y `.ps1` — comprobación HTTP una vez levantados ambos servicios.

## Requisitos

- JDK 21 (`java -version`, `javac -version`).
- Maven 3.9.x (`mvn -version`).
- `curl` (Ubuntu) o `curl.exe` (Windows) y dos terminales.
- Dependencias Maven descargadas previamente si hay restricciones de red.

## Compilar y ejecutar

Desde **la raíz del proyecto**:

```bash
mvn clean package
```

Terminal 1:

```bash
java -jar account-service/target/account-service-0.1.0.jar --spring.profiles.active=dev
```

Terminal 2:

```bash
java -jar transaction-service/target/transaction-service-0.1.0.jar --spring.profiles.active=dev
```

Terminal 3:

```bash
bash scripts/validate-lab02.sh
# Windows PowerShell:
# .\scripts\validate-lab02.ps1
```

**Semilla:** cuenta `11111111-1111-4111-8111-111111111111`, `ACTIVE`, saldo ficticio `150000.00` COP, y dos movimientos históricos ficticios.

**Alcance:** NO implementar `POST /api/transactions`, NO modificar saldos, NO persistir información ni procesar dinero real. El endpoint `/internal/verify-account/{accountId}` es solamente un ejercicio técnico para WebClient; no es un contrato bancario externo.

**Precaución:** el código es didáctico. Sin seguridad, base de datos, trazabilidad distribuida ni consistencia financiera; no es apto para uso bancario real.

## IMPORTANTE: ejercicios incompletos

Este ZIP es el **starter**, no la solución final. Las pruebas de `AccountCatalogTest` y
`AccountLookupClientTest` **fallarán de manera intencional** hasta resolver las marcas
`TODO LAB02-A` y `TODO LAB02-B`. Consulta `docs/GUIA_ALUMNO.md` para instrucciones
paso a paso.
