# Reactive Banking — Laboratorio 1 (Spring WebFlux)

**Curso:** Arquitectura de Microservicios Reactivos con WebFlux  
**Duración:** 90 minutos · **Modalidad:** construcción guiada

Esta es una práctica **didáctica**, no una aplicación financiera lista para producción.

## Qué contiene

- `account-service` (puerto **8081**) y `transaction-service` (puerto **8082**).
- `GET /internal/status` en cada aplicación.
- `docs/CONTRATOS_REST.md` con los **cuatro contratos bancarios aprobados**, aún **NO implementados**.
- `docs/GUIA_ALUMNO.md` con la construcción guiada y validaciones.
- `docs/GUIA_INSTRUCTOR.md` (en la referencia del instructor).
- `scripts/` con comprobaciones simples en Bash y PowerShell.

## Requisitos

Java **21**, Maven **3.9.x** (Spring Boot admite Maven >=3.6.3), conexión a Maven Central para la descarga inicial, y curl o Postman.

## Primeros pasos

1. Abre `docs/GUIA_ALUMNO.md`.
2. Completa las dos tareas `TODO LAB01` de los controladores.
3. Compila y ejecuta desde la raíz:

```bash
mvn clean package
```

En terminales diferentes:

```bash
java -jar account-service/target/account-service-0.1.0.jar --spring.profiles.active=dev
```

```bash
java -jar transaction-service/target/transaction-service-0.1.0.jar --spring.profiles.active=dev
```

Comprueba:

```bash
curl -i http://localhost:8081/internal/status
curl -i http://localhost:8082/internal/status
```

Tras completar el ejercicio, ambas respuestas deben contener `"status":"UP"` y `"environment":"dev"`.

> **Aviso:** en el **starter**, `mvn test` falla intencionadamente hasta completar los dos `TODO LAB01`. El proyecto de **referencia** incluye la solución y espera superar esas pruebas.

## Fuera del alcance del Lab 1

Los endpoints `/api/accounts` y `/api/transactions` están **solamente especificados**. No hay base de datos, lógica bancaria, WebClient, Gateway, JWT ni observabilidad Actuator. No llames `UP` del endpoint didáctico un health check real.
