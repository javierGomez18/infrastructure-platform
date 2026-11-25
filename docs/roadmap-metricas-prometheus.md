# Roadmap: métricas estandarizadas con Prometheus

**Objetivo:** que todos los servicios expongan **las mismas métricas con los mismos nombres y etiquetas**, que Prometheus las recoja y que haya paneles de Grafana reutilizables. Reutilizable en proyectos futuros.

**Pedido explícito:** no escribir más código de métricas por ahora. Este documento es el plan.

## Estado de partida (25 de noviembre de 2026)

| Pieza | Estado |
|---|---|
| `metrics-service` | **Librería** (jar sin clase main) con: `MetricEvent`, `MetricEventPublisher` (publica un evento de Spring), `MetricEventListener` (lo convierte en contadores y un temporizador), registros auxiliares y un `MetricsConfig` con etiquetas comunes. **4 tests** que la ejecutan junto a la autoconfiguración real de Actuator/Prometheus de Boot |
| Métricas definidas | `audit.events.success`, `.failure`, `.conflict`, `.unprocessable`, `.latency` (el evento de entrada usa el singular: `audit.event.success`…) |
| Quién la usa | `audit-service` y `clientes-cuentas-microservicio` la declaran como dependencia, pero **ninguno escanea su paquete** y **nadie publica eventos**: **está inerte** |
| Configuración del config server (`config-server/application.yml`) | Ya expone `health,info,prometheus,refresh,mappings`, activa el endpoint de Prometheus y pone la etiqueta `application` = nombre del servicio |
| Prometheus / Grafana | **No están desplegados.** Ni en el compose ni configurados |

## Decisiones previas (hay que tomarlas antes de escribir código)

1. **¿Quién pone las etiquetas comunes: el config server o la librería?** Hoy lo hacen los dos (`application` está en el yml compartido y en `MetricsConfig`). Dos sitios = confusión. Propuesta: **el config server** para lo que es configuración (`application`) y la librería solo para lo que necesita código.
2. **¿La librería es solo "eventos de auditoría" o el estándar general?** Hoy es lo primero. Propuesta: separar (a) el **estándar** (nombres, etiquetas, qué métricas son obligatorias) de (b) las **métricas de negocio** concretas, como las de auditoría.
3. **Nombres.** Hoy `audit.events.*` (con punto, estilo Micrometer). Prometheus las verá como `audit_events_success_total`. Decidir un prefijo por dominio y mantenerlo.

## Fase 1: activar la librería (hacerla autoconfigurable)

- [ ] Convertir `metrics-service` en una **autoconfiguración de Spring Boot** (fichero `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` con una clase `@AutoConfiguration`). Así basta con añadir la dependencia, sin escanear paquetes ajenos.
- [ ] Condicionarla a que exista un `MeterRegistry` (que no falle en servicios sin Actuator).
- [ ] **No** definir un `PrometheusMeterRegistry` propio: ya lo hizo Boot y definirlo chocó con su bean (`prometheusConfig`) y la aplicación no arrancaba. Ver `hallazgos-y-decisiones.md`.
- [ ] Añadir un test de autoconfiguración (patrón `ApplicationContextRunner`, como el que ya hay).

**Hecho cuando:** añadir la dependencia a un servicio nuevo hace que sus métricas de la librería aparezcan en `/actuator/prometheus` sin tocar nada más.

## Fase 2: el estándar

Rellenar esta tabla (es el entregable de la fase; el resto es aplicarla):

| Qué | A decidir |
|---|---|
| Etiquetas obligatorias | `application` y `environment` (perfil activo); valorar `instance` |
| Métricas HTTP del servidor | Las de Micrometer/Boot (latencia, peticiones por estado). Confirmar que están habilitadas y con histogramas |
| Métricas de JVM, pool de conexiones y Feign/Resilience4j | Las que ya da Boot/Resilience4j; listar cuáles son obligatorias |
| Métricas de negocio | `audit.events.*` y las que haya que añadir; convención de nombres |
| Cardinalidad | **Prohibido** etiquetar con ids de usuario, de cuenta, etc. (explota la cardinalidad) |

## Fase 3: publicar de verdad

- [ ] En `audit-client`, publicar `MetricEvent` al enviar eventos de auditoría: éxito, fallo, conflicto (409), no procesable (422) y latencia. El sitio natural son el `AuditFallbackFactory` y el `AuditFeignErrorDecoder` ya existentes.
- [ ] Que los fallos de comunicación **se midan** y no solo se registren en el log (hoy se pueden perder en silencio).
- [ ] Decidir si se publica con el evento de Spring (como está) o se usan directamente los contadores de Micrometer: el intermediario solo vale si desacopla algo.

**Hecho cuando:** hacer una operación en `clientes-cuentas-microservicio` incrementa `audit_events_success_total` y se ve en `/actuator/prometheus`.

## Fase 4: desplegar Prometheus y Grafana

- [ ] Añadir Prometheus y Grafana al `docker-compose.yml` (versionado, ver el roadmap de seguridad, fase 0).
- [ ] Configuración de *scrape*: por **Eureka** (descubrimiento dinámico) o por lista estática de objetivos. Para empezar, estática.
- [ ] Un panel de Grafana por tipo de servicio, **exportado a JSON y versionado** (si no, se pierde).
- [ ] Alertas básicas (servicio caído, tasa de errores de auditoría alta). Opcional al principio.

**Hecho cuando:** `docker compose up` deja un Grafana con los paneles ya cargados y datos reales.

## Fase 5: seguridad de los endpoints de métricas

- [ ] `audit-service` deja `/actuator/**` abierto a todos. Decidir: abrir solo `health` y `prometheus`, y restringir el resto por red o por rol.
- [ ] El `shared-filters` (validación de cabecera y sanitización) **no debe bloquear** el *scrape* de Prometheus. Comprobarlo.
- [ ] Prometheus no se autentica con `client_credentials` por defecto: decidir si el *scrape* va por red interna sin autenticación o con un mecanismo propio.

## Fase 6: reutilizable en futuros proyectos

- [ ] Publicar la librería (ver la fase 5 del roadmap de seguridad: cómo se distribuyen los módulos).
- [ ] Guía de "cómo instrumentar un servicio nuevo" y plantilla del panel de Grafana.

## Cosas ya aprendidas

- Una librería **no debe llevar `application.yml`** en su jar (compite con el de la aplicación que la use). Se eliminó el de `metrics-service`.
- Las etiquetas comunes se aplican con un `MeterRegistryCustomizer` de Boot, no con un `@PostConstruct` que toque el registro (los medidores creados antes no las reciben).
