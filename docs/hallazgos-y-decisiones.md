# Hallazgos, decisiones abiertas y trampas ya encontradas

Aquí está lo que se **descubrió** revisando el proyecto (25 de noviembre de 2026), separado en dos grupos para saber cuánto fiarse de cada cosa:

- **Verificado:** se comprobó ejecutándolo.
- **Deducido leyendo:** se ve en los ficheros, pero **no se ejecutó**. Compruébalo antes de darlo por cierto.

## A. Verificado ejecutando

| # | Hallazgo | Cómo se comprobó | Estado |
|---|---|---|---|
| A1 | **El conversor estándar de roles de Spring no entiende la ruta anidada de Keycloak.** `JwtGrantedAuthoritiesConverter` con `setAuthoritiesClaimName("resource_access.<client>.roles")` devuelve **siempre una lista vacía**, así que `hasRole(...)` rechaza todo, incluso con un token válido | Se construyó un `Jwt` con ese claim y se aplicó el conversor: salió `[]` | **Corregido** en `audit-service` (conversor propio + `SecurityConfigTest`). **Sigue roto en `clientes-cuentas-microservicio`** |
| A2 | **Un `@Configuration` propio llamado `PrometheusConfig` impedía arrancar cualquier aplicación** que usara la librería de métricas: choca con el bean `prometheusConfig` de la autoconfiguración de Boot ("overriding is disabled") | Se arrancó un contexto mínimo con la librería | **Corregido** (se eliminó la clase) |
| A3 | Las etiquetas comunes estaban fijas a `application=metrics-service` / `environment=production`: todas las métricas de cualquier servicio habrían salido mal etiquetadas | Lectura + test | **Corregido** (salen del nombre de la aplicación y el perfil) |
| A4 | El cliente Feign de auditoría **solo añade `X-Client-Id`; no envía `Authorization`** | Lectura de `AuditFeignConfig` | **Pendiente** (fase 3 de seguridad) |
| A5 | **La librería de métricas está inerte**: ningún servicio escanea su paquete ni publica eventos | `grep` en los consumidores | **Pendiente** (fases 1 y 3 de métricas) |
| A6 | `mvn test` falla en `audit-service` con "Artifact has not been packaged yet" | Ejecución | Es una limitación de Maven (el plugin `unpack` necesita el módulo vecino empaquetado). Usa `mvn clean package` |

## B. Deducido leyendo (sin ejecutar)

| # | Sospecha | Por qué | Cómo comprobarlo |
|---|---|---|---|
| B1 | **`issuer-uri` inconsistente entre servicios** | `audit-service` y el gateway usan `localhost:8082`; `clientes-cuentas` usa `keycloak:8082`. Pero `8082` es el puerto **del host**: dentro de la red de Docker Keycloak escucha en `8080`. `keycloak:8082` no debería resolver | Pedir un token y llamar a cada servicio desde dentro y fuera de Docker |
| B2 | **La ruta del gateway a `audit-service` duplica `audit`** | `RewritePath=/api/(?<segment>.*), /api/v1/audit/${segment}` con `Path=/api/audit/**`: el `segment` ya contiene `audit/...`, así que saldría `/api/v1/audit/audit/...`. En las rutas de clientes-cuentas sí cuadra, porque su prefijo es distinto | `curl` por el gateway a `/api/audit` |
| B3 | **`clientes-cuentas` exige roles `*.read` para operaciones de escritura** | `POST`/`PUT` sobre `/cuentas/**` requieren `clientes.read` y `cuentas.read` | Revisar si es intencionado o falta `*.write` |
| B4 | **`initial-instance-info-replication-interval-seconds: 1000`** en `audit-service-dev.yml` | El valor por defecto de Eureka es 40. Es muy raro y solo está ahí. No sé con certeza su efecto en el registro | Observar cuánto tarda `audit-service` en aparecer en Eureka |
| B5 | **`TokenRelay` en el gateway** podría no hacer nada | Es un gateway que solo valida JWT (resource server): el relevo de token está pensado para un flujo con login OAuth2. El gateway ya reenvía la cabecera `Authorization` | Probar sin `TokenRelay` |
| B6 | **`X-Client-Id` es un mecanismo de identificación paralelo a OAuth2** | El cliente la envía y `shared-filters` la comprueba contra `security.allowed-clients` (en el yml compartido) | Leer `HeaderValidationFilter` |
| B7 | **`audit-service` en Eureka necesita red compartida** | Se registra en `eureka-server:8761`, que está en el otro compose | Ver el orden de arranque en el README |

## C. Decisiones abiertas

1. **¿`X-Client-Id` se mantiene o se elimina cuando haya OAuth2?** Con `client_credentials`, la identidad del llamante ya va en el token (`azp`/`clientId`). Mantener ambos duplica la fuente de verdad. Propuesta: eliminarla y usar el token.
2. **Estrategia de hostname de Keycloak** (B1). Es la decisión que más problemas evita: elegir una y documentarla.
3. **Gestión de secretos de los *clients*.** Opciones: variables de entorno inyectadas, un gestor de secretos (en el trabajo se usa CyberArk) o ficheros externos. En cualquier caso **no en el yml**.
4. **¿Starters reutilizables?** Hoy los módulos se consumen como dependencias del reactor o del `.m2`. Para proyectos futuros hace falta un mecanismo de publicación.
5. **Dónde viven las etiquetas comunes de las métricas** (config server o librería).

## D. Trampas ya encontradas (para no repetirlas)

- Una **librería no debe llevar `application.yml`** ni definir beans que Boot ya autoconfigura.
- **`Map.of(...)` lanza `NullPointerException` con valores `null`**: en un parámetro opcional usa `HashMap`. (Se encontró en `clientes-cuentas`, en la auditoría de `GET /movimientos`.)
- **`@EnabledIf` no se hereda** en JUnit de una clase base abstracta a sus subclases; hay que usar una extensión registrada con `@ExtendWith`.
- El **`.gitignore` con `*.yml`** oculta ficheros nuevos y es la razón de que el compose no esté en git.
- Con **Windows y rutas largas** (`core.longpaths`) puede fallar `git add` en proyectos con paquetes Java muy anidados.

## E. Comandos útiles

```bash
mvn clean package                         # construir todo (usar package, no test)
mvn -pl audit-service -am package         # solo audit-service y lo que necesita
git log --oneline -10                     # ver dónde se quedó
git status -sb                            # ¿hay algo sin commitear/subir?
```
