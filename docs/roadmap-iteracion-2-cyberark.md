# Iteración 2 (propuesta): sacar los secretos de la plataforma a CyberArk

> **Estado: propuesta.** No se ha empezado y **no se sabe si es viable**. La decisión se toma **al terminar la iteración 1** (Keycloak y métricas). Hasta entonces, este documento solo guarda el razonamiento para no repetirlo.
> **Contexto:** es un proyecto **personal**, para aprender y para reutilizarlo después. La motivación es que en el trabajo actual se usa CyberArk y se quiere entender cómo se integra.

## 1. Objetivo

Que ninguna credencial de la plataforma esté escrita en un `docker-compose.yml`, un yml o el repositorio, y que se entreguen a los servicios desde CyberArk.

## 2. Keycloak y CyberArk no son alternativas

Se dudó si Keycloak seguiría valiendo con CyberArk. **Sí: resuelven problemas distintos.**

| | Keycloak | CyberArk (ahora Idira) |
|---|---|---|
| Pregunta | ¿Quién eres? | ¿Dónde guardo y cómo entrego esta credencial? |
| Qué hace | Autentica y emite tokens OAuth2/OIDC que los servicios validan | Almacena, rota, audita y entrega secretos |

El token de acceso de Secrets Manager (la familia Conjur) **dura 8 minutos y sirve para llamar a la API de CyberArk**, no a las APIs de tus servicios (según su documentación); por eso no reemplaza a un emisor de tokens como Keycloak. CyberArk tiene también productos de identidad; **no se ha verificado** cómo funcionarían como emisor de tokens para estos servicios.

## 3. Inventario de secretos

| Secreto | Dónde está hoy | Destino en la iteración 2 |
|---|---|---|
| Usuario y contraseña de PostgreSQL (`admin`) | `docker-compose.yml` y `config-server/*.yml` | CyberArk |
| Contraseña de administrador de Keycloak y de su base de datos | `docker-compose.yml` | CyberArk |
| `client-secret` de cada *client* de Keycloak (`client_credentials`) | No existe aún (se crea en la iteración 1) | **CyberArk**, entregado a cada servicio |
| Claves de firma de los tokens | Dentro de Keycloak | Se quedan en Keycloak |
| Contraseña de administrador de Grafana (iteración 1, métricas) | No existe aún | CyberArk |

El `client-secret` es el caso importante: es el **"secreto cero"** de `client_credentials`. Cada servicio necesita uno para pedir su token y no puede estar en el yml.

## 4. Lo que se sabe de CyberArk (verificado en su documentación el 25 de noviembre de 2026)

- **Producto renombrado:** CyberArk es ahora **Idira** (Palo Alto Networks lo presentó el 12 de mayo de 2026). La documentación sigue en `docs.cyberark.com`, con "Idira Docs" en la cabecera. Conjur Enterprise → *Secrets Manager Self-Hosted*; Conjur Cloud → *Secrets Manager SaaS*; existe también **Conjur Open Source**.
- **Credential Provider (CP):** agente instalado en el servidor de la aplicación. **No funciona en contenedores.**
- **Central Credential Provider (CCP):** servicio REST sobre IIS. `GET /AIMWebService/api/Accounts` con `AppID` y al menos otro parámetro (`Safe`, `Object`…); autenticación habitual por **certificado de cliente (mTLS)**; devuelve `Content` (la contraseña), `UserName`, `Address`, `PasswordChangeInProcess`. Requiere un **Vault de PAM y un servidor Windows**.
- **Secrets Manager (Conjur):** pensado para contenedores y CI/CD. Se autentica con la identidad de la plataforma (cuenta de servicio de Kubernetes, rol IAM, JWT) y entrega los secretos con un **Secrets Provider** (init container o sidecar; en fichero o en un `Secret` de Kubernetes) o con una llamada directa a la API.
- **Plugin de Spring Boot:** `com.cyberark:conjur-sdk-springboot`, con propiedades `conjur.appliance-url`, `conjur.account`, `conjur.authn-login`, `conjur.authn-api-key`, `conjur.cert-file` y las anotaciones `@ConjurValue`, `@ConjurValues`, `@ConjurPropertySource`. **No verificado** con Spring Boot 3.3.

## 5. Opciones de integración para esta plataforma

Esta plataforma corre en **Docker Compose**, no en Kubernetes.

| Opción | Qué es | Encaja | Pega |
|---|---|---|---|
| **A. Conjur Open Source en el compose + plugin o cliente REST** | Un servicio más en el compose; los servicios piden sus secretos al arrancar | Es lo único del ecosistema que se puede **ejecutar en local**; se aprende de verdad la mecánica | Hay que montar y mantener Conjur y su política. Comprobar diferencias con la versión empresarial |
| **B. Paso previo que genera el entorno** | Un script (o contenedor de arranque) pide los secretos y rellena un `.env` o ficheros que el compose pasa a los servicios | La **aplicación no cambia**: solo cambia quién rellena los valores. Es el patrón de "secrets como variables de entorno" | El `.env` generado contiene secretos en claro en disco: hay que protegerlo e ignorarlo en git |
| **C. Secrets Provider de Kubernetes** | Init container o sidecar | Es lo que se usaría en OpenShift/Kubernetes | **No aplica** a un compose; solo si algún día se pasa a Kubernetes |
| **D. CCP / CP** | Vault de PAM + servidor | Es lo habitual en entornos corporativos con servidores | **Inviable en un proyecto personal**: requiere una infraestructura que no se puede levantar en local |

**Inclinación actual (sin comprometerse):** A, y si resulta pesado, B. Se decide tras la iteración 1.

## 6. Criterios de viabilidad (evaluar al terminar la iteración 1)

- [ ] ¿Se puede ejecutar Conjur Open Source en Docker en este equipo sin una licencia?
- [ ] ¿Se tiene acceso a algún entorno de CyberArk de pruebas (del trabajo o propio), o hay que simularlo todo?
- [ ] ¿El esfuerzo (montar Conjur, su política y la entrega de secretos) compensa lo que se aprende?
- [ ] ¿Se puede probar la parte de Spring (leer credenciales de fuera) sin Conjur? **Sí**: es un fichero o variables de entorno, y se puede hacer ya en FastTrack. La parte que añade CyberArk es solo el origen del secreto.

## 7. Fases tentativas (si se decide seguir)

1. **Inventario y decisión** (opción A o B) y política de nombres de los secretos por entorno.
2. **Secretos de PostgreSQL:** servicios y compose dejan de llevar `admin`.
3. **Secretos de Keycloak:** administrador y base de datos.
4. **`client-secret` de los *clients*:** entrega a cada servicio. Decidir cómo se rotan (ver riesgos).
5. **Tests:** que el arranque falle con un mensaje claro si falta un secreto, y que no aparezcan secretos en logs ni en `/actuator/env`.
6. **Documentación** de "cómo añadir un secreto o un servicio nuevo".

## 8. Riesgos y preguntas abiertas

- **Rotación del `client-secret`:** quien rota las contraseñas en CyberArk (el CPM) sabe cambiarlas en sistemas como una base de datos; para un *client* de Keycloak habría que usar su API de administración, con un script o un conector propio. Alternativa más robusta: que el *client* se autentique con **JWT firmado o certificado** (Keycloak lo soporta); entonces el secreto es una clave privada.
- **El secreto de arranque** (cómo se autentica un servicio ante CyberArk) sigue siendo un secreto: hay que decidir dónde vive.
- **Nunca subir secretos a git**, ni siquiera los de prueba: `.env` ignorado y un `.env.example` versionado.
- **Un solo mecanismo por servicio:** si unos servicios leen del entorno y otros llaman a CyberArk directamente, se multiplica el mantenimiento.
- La documentación y los nombres de producto de CyberArk/Idira **están cambiando** (rebranding de 2026): revalidar antes de implementar.

## 9. Relación con otros proyectos

- **`fasttrack-delivery-microservicio`** (otro repositorio) es el sitio natural para probar la mecánica del lado de Spring: credenciales solo desde el entorno, sin valores por defecto, con fallo claro al arrancar si faltan.
- La entrega de secretos con un Secrets Provider en Kubernetes/OpenShift **no** se puede practicar con el compose de esta plataforma.
