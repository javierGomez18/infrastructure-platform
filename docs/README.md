# Infrastructure Platform: guía para retomar el proyecto

> **Última actualización:** 25 de noviembre de 2026 · **Estado de git en esa fecha:** rama `feature/infrastructure-platform`, commit `9d8f72d`.
> Si lees esto meses después, **empieza por este fichero**: te dice qué hay, qué está a medias y por dónde seguir.

## 1. Qué es este repositorio y por qué está a medias

Es la **capa común de una arquitectura de microservicios**: lo que comparten todos los servicios (auditoría, métricas, filtros, cliente de auditoría, configuración centralizada).

El objetivo original, que **no se llegó a terminar**, era:

1. Montar **Keycloak** y securizar **todos** los servicios con **OAuth 2 (`client_credentials`)** en las llamadas entre servicios.
2. **Estandarizar las métricas de Prometheus** de todos los servicios.
3. Dejarlo reutilizable para **proyectos futuros** (librerías/starters que se añaden con una dependencia).

El trabajo se interrumpió al encontrar empleo y tener que aprender **programación reactiva** (para eso se creó el proyecto `fasttrack-delivery-microservicio`). Lo que hay de seguridad y métricas es **poco más que un concepto**, a la espera de configurar Keycloak. Esto es intencionado: no es un trabajo abandonado por error.

## 2. Mapa del repositorio

| Módulo | Qué es | Estado |
|---|---|---|
| `audit-service` | Servicio que guarda eventos de auditoría (PostgreSQL). Puerto `8085`, context-path `/api/v1` | Funciona. Ya es *resource server* OAuth2 (ver §3) |
| `shared-libraries/audit-client` | Cliente Feign + DTOs + contrato OpenAPI para llamar a `audit-service` | Funciona, **pero no envía token** (ver §3) |
| `shared-libraries/shared-filters` | Filtros comunes: validación de cabecera `X-Client-Id`, MDC (correlationId), sanitización de entrada | Funciona, con autoconfiguración |
| `metrics-service` | **Librería** (no es un servicio: no tiene clase main) con eventos y contadores de métricas | Código y tests hechos, **pero inerte** (nadie la activa) |
| `config-server` | Los ficheros de configuración que sirve el config server (`*.yml`) | Funciona |
| `infrastructure-platform-config-server` | La aplicación Spring Cloud Config Server | Funciona |
| `shared-database` | Scripts SQL de esquema y datos (`audit`, `clientes-cuentas`, `init`) | Funciona |
| `docker/` | `docker-compose.yml` (config-server, audit-service, **Keycloak 24**, PostgreSQL) y los Dockerfile | Versionado (ver §4) |

Servicios en Docker y puertos del host: config-server `8889`, audit-service `8085` (+`5005` debug), Keycloak `8082` (→ `8080` dentro del contenedor), PostgreSQL `5432`.

## 3. Estado de los dos "evolutivos"

| Evolutivo | Hecho | Falta |
|---|---|---|
| **Seguridad (Keycloak + OAuth2)** | `audit-service` valida JWT y comprueba roles (`audit.write` / `audit.read`); conversor de roles de Keycloak corregido y con tests | Keycloak sin configurar de forma reproducible; **nadie envía token** a `audit-service`; sin `client_credentials`; sin tests de integración con Keycloak. Ver [`roadmap-seguridad-keycloak.md`](roadmap-seguridad-keycloak.md) |
| **Métricas (Prometheus)** | Librería con eventos y contadores de auditoría (`audit.events.*`), con tests; el config server ya expone `/actuator/prometheus` y la etiqueta `application` | La librería **no se activa en ningún servicio** y **nadie publica eventos**; sin estándar de nombres; sin Prometheus ni Grafana desplegados. Ver [`roadmap-metricas-prometheus.md`](roadmap-metricas-prometheus.md) |
| **Eureka** | `audit-service` tiene el cliente Eureka y la configuración para registrarse (el gateway lo enruta como `lb://audit-service`) | Comprobar el registro de extremo a extremo (ver hallazgos) |

## 4. Antes de nada: cuatro avisos

1. **Resuelto (25 de noviembre de 2026): `docker/docker-compose.yml` ya está en git.** Antes el `.gitignore` ignoraba todos los `*.yml` y `*.yaml`, y ese fichero (que contiene el servicio de Keycloak) solo existía en un ordenador. Ya no hay reglas de ese tipo. Si algún día añades un `.env` con secretos, ignóralo explícitamente.
2. **Contraseñas de desarrollo (`admin`)** en los yml del config server y en el compose. No son secretos de producción, pero no se deben reutilizar. Hay un plan para gestionarlas (ver [`hallazgos-y-decisiones.md`](hallazgos-y-decisiones.md)).
3. **Orden de arranque de Docker.** Este compose y el de `clientes-cuentas-microservicio` se declaran **mutuamente la red del otro como `external`** (`docker_backend-net` y `docker_platform-net`). Si arrancas uno sin que exista la red del otro, falla. Crea las redes primero o arranca en dos pasadas.
4. **El `README.md` de la raíz describe la visión, no el estado.** Dice, por ejemplo, que la auditoría usa SQLite (usa PostgreSQL) y muestra insignias de "observability: enabled" y "status: active". Fíate de esta carpeta `docs/`, no de él.

## 5. Cómo comprobar que todo sigue funcionando (primer día)

```bash
# Desde la raíz del repo. Debe terminar en BUILD SUCCESS
mvn clean package
```

- Usa `package` y no `test`: el módulo `audit-service` descomprime el contrato del `audit-client` y Maven exige que ese módulo esté empaquetado (limitación conocida, no un fallo).
- Tests existentes: `metrics-service` (4, la librería junto a la autoconfiguración real de Boot) y `audit-service` (4, conversor de roles). **El resto de módulos no tiene tests.**
- Versiones a revisar antes de seguir: Spring Boot **3.3.1**, Spring Cloud **2023.0.1** y Keycloak **24.0** son de 2024. Comprueba su soporte y actualiza si hace falta.

## 6. Orden de trabajo recomendado

1. ~~**Higiene**: versionar el `docker-compose.yml` y arreglar el `.gitignore`~~ **Hecho el 25 de noviembre de 2026.** Queda el resto de la fase 0 de seguridad (contraseñas de desarrollo y orden de arranque).
2. **Keycloak reproducible** (realm exportado + clientes + roles). → fases 1 de seguridad.
3. **Token en el cliente de auditoría** (`client_credentials`). → fase 3 de seguridad. Con esto la auditoría vuelve a funcionar de extremo a extremo.
4. **Métricas**: activar la librería, definir el estándar y desplegar Prometheus y Grafana. → roadmap de métricas.
5. **Reutilización**: convertir seguridad y métricas en *starters* para futuros proyectos.

El orden importa: las métricas son independientes de Keycloak, pero **hasta el paso 3 la auditoría está rota en silencio** en cuanto se usa la seguridad nueva (cada llamada recibe 401 y el *fallback* la oculta).

## 7. Índice de esta carpeta

| Documento | Para qué |
|---|---|
| [`roadmap-seguridad-keycloak.md`](roadmap-seguridad-keycloak.md) | Plan paso a paso de Keycloak + OAuth2 `client_credentials` |
| [`roadmap-metricas-prometheus.md`](roadmap-metricas-prometheus.md) | Plan paso a paso de métricas estandarizadas con Prometheus y Grafana |
| [`hallazgos-y-decisiones.md`](hallazgos-y-decisiones.md) | Qué se comprobó (y cómo), decisiones abiertas y trampas ya encontradas |

La parte del lado cliente (gateway, `clientes-cuentas-microservicio`) está en `docs/` del repositorio `clientes-cuentas-microservicio`.
