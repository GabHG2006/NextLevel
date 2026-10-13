# Seguridad, OAuth2, JWT, Power BI y despliegue

## Cambios principales

Esta versión separa la seguridad JWT de la lógica de negocio. La generación, validación y almacenamiento del token quedaron en el paquete `com.nextlevel.security`:

- `JwtService`: genera y valida el token.
- `JwtAuthenticationFilter`: lee el token desde `Authorization: Bearer` o desde la cookie `JWT_TOKEN`.
- `JwtCookieService`: crea y limpia la cookie segura del token.
- `OAuth2AuthenticationSuccessHandler`: conecta Google OAuth2 con MongoDB, Spring Security y JWT.
- `OAuth2StartController`: permite iniciar OAuth desde login o desde registro sin mezclar esa lógica con los controladores principales.

## Dónde está el JSON Web Token

El JWT quedó ubicado aquí:

```text
src/main/java/com/nextlevel/security/JwtService.java
src/main/java/com/nextlevel/security/JwtAuthenticationFilter.java
src/main/java/com/nextlevel/security/JwtCookieService.java
```

Flujo implementado:

1. El usuario inicia sesión o se registra.
2. El backend valida o crea el usuario.
3. `JwtService` genera el token firmado.
4. `JwtCookieService` guarda el token en la cookie `JWT_TOKEN`.
5. `JwtAuthenticationFilter` revisa cada petición y autentica al usuario si el token es válido.

## Registro con JWT

El registro ya no solo crea el usuario. También genera JWT y deja al usuario autenticado.

### Registro web

- `GET /registro`: formulario de registro.
- `POST /registro`: crea usuario, genera JWT y redirige al catálogo o a la ruta pendiente.
- `GET /auth/oauth2/google/registro`: registro con Google.

### Registro API

- `POST /api/auth/registro`: registra el usuario, genera JWT, lo guarda en cookie y devuelve el token.

Respuesta esperada:

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "correo": "cliente@nextlevel.com",
  "roles": ["ROLE_CLIENTE"]
}
```

## Login con JWT

### Login web

- `GET /login`: pantalla de inicio de sesión NextLevel.
- `POST /login`: login tradicional con correo y contraseña.
- `GET /auth/oauth2/google/login`: login con Google.
- `GET /login/oauth2/code/google`: callback de Google OAuth2.
- `GET /logout`: cierre de sesión y limpieza de la cookie JWT.

### Login API

- `POST /api/auth/login`: recibe `correo` y `password`, valida credenciales y devuelve JWT.

Ejemplo:

```json
{
  "correo": "cliente@nextlevel.com",
  "password": "Cliente123*"
}
```

## Validación de correo existente

Endpoint disponible:

```text
GET /api/auth/email-exists?correo=correo@ejemplo.com
```

Respuesta:

```json
{
  "correo": "correo@ejemplo.com",
  "exists": true
}
```

El sistema diferencia entre:

- correo inexistente: muestra `Ups, lo siento. Ese correo no está registrado`.
- contraseña incorrecta: muestra error de credenciales.
- cuenta bloqueada: muestra mensaje de bloqueo temporal.

## OAuth2 con Google para login y registro

Se agregaron dos entradas separadas:

```text
/auth/oauth2/google/login
/auth/oauth2/google/registro
```

Ambas llegan al mismo callback de Google:

```text
/login/oauth2/code/google
```

La diferencia es que el sistema guarda temporalmente si el usuario venía desde login o desde registro y después ejecuta la lógica correcta.

Importante: para que Google OAuth funcione, la cookie de sesión debe usar `SameSite=Lax`. Por eso se cambió:

```properties
server.servlet.session.cookie.same-site=${SESSION_COOKIE_SAME_SITE:Lax}
```

Con `SameSite=Strict`, Google puede devolver el callback sin la cookie de sesión y Spring Security puede rechazar el flujo OAuth.

## Configuración de Google OAuth2

Variables de entorno:

```bash
GOOGLE_CLIENT_ID=tu_client_id
GOOGLE_CLIENT_SECRET=tu_client_secret
```

Redirect URI para desarrollo local:

```text
http://localhost:8080/login/oauth2/code/google
```

Redirect URI para despliegue:

```text
https://TU-DOMINIO/login/oauth2/code/google
```

En Google Cloud debes registrar exactamente esas URL en **Authorized redirect URIs**.

## Power BI integrado

Se agregó una vista protegida para el administrador:

```text
/admin/analitica
```

Archivos agregados:

```text
src/main/resources/templates/admin/analitica_powerbi.html
src/main/resources/static/powerbi/Taller.pbix
```

El archivo `.pbix` ya quedó dentro del proyecto. Para verlo embebido en la aplicación:

1. Abre Power BI Desktop.
2. Abre `Taller.pbix`.
3. Publícalo en Power BI Service.
4. Copia el enlace de inserción o iframe.
5. Configura la variable de entorno:

```bash
POWERBI_EMBED_URL=https://app.powerbi.com/reportEmbed?reportId=...
```

Si la variable está vacía, la página `/admin/analitica` muestra una pantalla con instrucciones y un botón para descargar el archivo PBIX.

## Variables de entorno recomendadas

```bash
MONGODB_URI=mongodb://localhost:27017/nextlevel_db
JWT_SECRET=una_clave_larga_de_minimo_32_caracteres
JWT_EXPIRATION_MS=86400000
GOOGLE_CLIENT_ID=tu_client_id
GOOGLE_CLIENT_SECRET=tu_client_secret
APP_COOKIE_SECURE=false
SESSION_COOKIE_SAME_SITE=Lax
JWT_COOKIE_SAME_SITE=Lax
POWERBI_EMBED_URL=
```

En producción usa `APP_COOKIE_SECURE=true` porque la app debe correr bajo HTTPS.

## Despliegue con Docker

Construir la imagen:

```bash
docker build -t nextlevel-store-secure .
```

Ejecutar la aplicación:

```bash
docker run -p 8080:8080 \
  -e MONGODB_URI="mongodb://host.docker.internal:27017/nextlevel_db" \
  -e JWT_SECRET="una_clave_larga_de_minimo_32_caracteres" \
  -e GOOGLE_CLIENT_ID="tu_client_id" \
  -e GOOGLE_CLIENT_SECRET="tu_client_secret" \
  -e APP_COOKIE_SECURE="false" \
  -e SESSION_COOKIE_SAME_SITE="Lax" \
  -e POWERBI_EMBED_URL="" \
  nextlevel-store-secure
```

También puedes levantar todo con:

```bash
docker compose up --build
```

## Despliegue en Render

Se agregó `render.yaml`. En Render debes crear las variables:

- `MONGODB_URI`
- `JWT_SECRET`
- `GOOGLE_CLIENT_ID`
- `GOOGLE_CLIENT_SECRET`
- `POWERBI_EMBED_URL`
- `APP_COOKIE_SECURE=true`

Render asigna automáticamente la variable `PORT`; el proyecto ya la usa con `server.port=${PORT:8080}`.

## Manejo de errores personalizado

Se agregaron pantallas inteligentes:

- `403`: acceso denegado.
- `404`: página no encontrada.
- `500`: error interno.
- `/ups`: pantalla genérica con el mensaje “Ups, lo siento”.

Para API, los errores se responden en JSON con `status`, `error`, `message`, `path` y `timestamp`.
