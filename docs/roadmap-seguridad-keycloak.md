# Roadmap: Keycloak + OAuth 2 `client_credentials`

**Objetivo:** que todos los servicios se autentiquen entre sí con tokens emitidos por Keycloak, y que cada servicio compruebe quién llama y con qué permisos. Reutilizable en proyectos futuros.

**No hay código de Keycloak escrito** más allá de lo que ya hay en `audit-service` (ver estado). Este documento es el plan.

## Estado de partida (25 de noviembre de 2026)

| Pieza | Estado |
|---|---|
| Keycloak 24 en `docker/docker-compose.yml` (`start-dev`, base de datos PostgreSQL, hostname `localhost`, puerto `8082`) | Existe y **ya está versionado** (desde el 25 de noviembre de 2026), pero el realm `microservices-realm` se creó a mano: **no hay export** |
| `audit-service` como *resource server* (`SecurityConfig`) | Hecho. Roles `audit.write` (POST) y `audit.read` (GET) del claim `resource_access.audit-service.roles`; `/actuator/**` abierto |
| Conversor de roles | Corregido en `audit-service`. **El estándar de Spring no sirve** con el claim anidado de Keycloak (devuelve lista vacía): se usa uno propio. Hay tests (`SecurityConfigTest`) |
| Cliente de auditoría (`audit-client`, Feign) | **No envía `Authorization`**: solo la cabecera `X-Client-Id`. Con la seguridad actual, toda llamada recibe 401 y el *fallback* lo oculta |
| `clientes-cuentas-microservicio` | Es *resource server* pero **con el mismo conversor roto** (ver su `docs/`) |

## Convenciones propuestas (decídelo y no lo cambies después)

| Cosa | Propuesta |
|---|---|
| Realm | `microservices-realm` (ya existe) |
| Un *client* por servicio | Nombre = nombre del servicio (`audit-service`, `clientes-cuentas-microservicio`, `gateway-server`) |
| Roles | Por *client*, formato `recurso.accion` (`audit.read`, `audit.write`, `cuentas.read`, `cuentas.write`…). Revisar `clientes.read`/`cuentas.read` que hoy se exigen también para escribir |
| Quién llama a quién | Cada servicio que **llama** a otro tiene su propio *client* confidencial con **Service Accounts** activado, y a ese *service account* se le asignan los roles del servicio destino |
| Secretos de los *clients* | **Nunca en git ni en el yml.** Variables de entorno o un gestor de secretos (CyberArk, Vault…). Ver `hallazgos-y-decisiones.md`  Plan en `roadmap-iteracion-2-cyberark.md`. |

## Fase 0: higiene del repositorio (1 hora)

- [x] Quitar `*.yml` y `*.yaml` del `.gitignore` y versionar `docker/docker-compose.yml`. **Hecho el 25 de noviembre de 2026.**
- [ ] Decidir qué hacer con las contraseñas de desarrollo (`admin`): variables de entorno con un `.env.example` versionado y el `.env` real ignorado.
- [ ] Documentar el orden de arranque de Docker (las redes `external`, ver el README).

**Hecho cuando:** un `git clone` limpio contiene todo lo necesario para levantar la plataforma.

## Fase 1: Keycloak reproducible

El realm no debe depender de pasos manuales.

- [ ] Fijar la **estrategia de hostname** (ver el problema en `hallazgos-y-decisiones.md`): el `iss` del token debe ser **idéntico** para quien lo pide desde Docker y desde el host, y los servicios deben poder alcanzar Keycloak por su nombre de contenedor. Opciones: `KC_HOSTNAME` con un nombre resoluble en ambos sitios, o validar el `issuer` por una URL y descargar las claves (`jwk-set-uri`) por otra.
- [ ] Crear en el realm: los *clients* de cada servicio y los roles de cada *client* (convención de arriba).
- [ ] Activar *Service Accounts* en los *clients* que llaman a otros, y asignar a su *service account* los roles del destino.
- [ ] **Exportar el realm** a un JSON versionado e importarlo al arrancar (`--import-realm` con el fichero en `/opt/keycloak/data/import`). Los secretos de los *clients* **no** van en ese JSON: se regeneran o se inyectan.
- [ ] Pasar de `start-dev` a un arranque con base de datos persistente si va a ser algo más que un entorno de prueba.

**Hecho cuando:** `docker compose up` en una máquina limpia deja Keycloak con realm, clients y roles listos, y se puede pedir un token con `curl` (grant `client_credentials`) y ver en https://jwt.io los roles en `resource_access.<client>.roles`.

## Fase 2: todos los servicios como *resource servers*

- [ ] `audit-service`: ✅ hecho.
- [ ] `clientes-cuentas-microservicio`: corregir el conversor de roles (mismo fallo que tenía `audit-service`).
- [ ] `gateway-server`: validar el JWT (resource server reactivo) y decidir si reenvía el token o no (ver `hallazgos-y-decisiones.md`).
- [ ] **No copiar y pegar el conversor en cada servicio**: moverlo a una librería compartida (fase 5), parametrizado por el nombre del *client*.
- [ ] Alinear el `issuer-uri` de todos los servicios con la estrategia de hostname de la fase 1.

**Hecho cuando:** una petición sin token recibe 401, con token de un rol equivocado recibe 403 y con el rol correcto pasa, **en todos los servicios**, con tests que lo demuestren.

## Fase 3: el lado cliente (`client_credentials`) · **la que desbloquea la auditoría**

En `shared-libraries/audit-client` (afecta a todos los que auditan):

- [ ] Añadir el cliente OAuth2 de Spring Security y registrar un `ClientRegistration` con grant `client_credentials`, `token-uri` de Keycloak, `client-id` y `client-secret` (el secreto, de fuera del repo).
- [ ] Obtener el token con un gestor de clientes autorizados **que funcione fuera de una petición web** (las llamadas Feign salen de hilos de servicio, no de un contexto con sesión), con **caché del token** hasta cerca de su caducidad.
- [ ] Un `RequestInterceptor` de Feign que añada `Authorization: Bearer <token>`.
- [ ] Decidir el destino de `X-Client-Id` (ver `hallazgos-y-decisiones.md`: hoy es otro mecanismo de identificación paralelo).
- [ ] **Fallos de autenticación**: hoy el *fallback* de Feign los traga. Un 401/403 debe verse (log de error y métrica), no ser silencioso.

**Hecho cuando:** `clientes-cuentas-microservicio` hace una operación y el evento aparece en `audit-service` (`GET /audit`), con Keycloak levantado y seguridad activa.

## Fase 4: tests

- [ ] Unitarios de roles con `Jwt` construido a mano (ya hay un ejemplo en `audit-service`).
- [ ] De la cadena de filtros con `MockMvc` y el post-procesador `jwt()` de Spring Security Test (401/403/200).
- [ ] De integración con un **Keycloak real en Testcontainers** (existe un módulo de Testcontainers para Keycloak, valida el nombre actual al implementarlo): pide un token real y llama al servicio. Es el test que descubriría problemas de hostname/issuer.
- [ ] Si se usa Docker en CI, que los tests se salten limpiamente cuando no esté disponible.

## Fase 5: reutilizable en futuros proyectos

- [ ] Extraer a una librería (*starter*) con autoconfiguración: conversor de roles de Keycloak, cadena de seguridad base y configuración del cliente `client_credentials`.
- [ ] Decidir cómo se distribuye: `mvn install` local, GitHub Packages o un Nexus. Hoy los módulos se consumen como dependencia del reactor/`.m2`.
- [ ] Escribir una guía de "cómo añadir un servicio nuevo" (client en Keycloak, roles, propiedades, dependencia).

## Riesgos y cosas a vigilar

- **Tokens que caducan:** sin caché y renovación, cada llamada pide un token nuevo a Keycloak (lento y frágil).
- **Relojes:** los JWT validan `exp`/`nbf`; con relojes desajustados entre contenedores aparecen 401 intermitentes.
- **Rotación de secretos de los *clients*:** pensar cómo se actualiza sin reiniciar todo.
- **`/actuator/**` abierto** en `audit-service`: decidir qué expone y a quién (ver roadmap de métricas).
