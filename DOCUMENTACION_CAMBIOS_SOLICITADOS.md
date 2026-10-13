# Cambios solicitados aplicados

## 1. Registro con JWT

Ahora el registro también genera JWT, no solo el inicio de sesión.

Archivos modificados:

```text
src/main/java/com/nextlevel/controller/AuthController.java
src/main/java/com/nextlevel/controller/api/AuthApiController.java
```

Rutas:

```text
POST /registro
POST /api/auth/registro
```

Después de registrar, el sistema:

1. Guarda el usuario en MongoDB.
2. Crea `SecurityUser`.
3. Genera JWT con `JwtService`.
4. Guarda la cookie `JWT_TOKEN` con `JwtCookieService`.
5. Deja al usuario autenticado.

## 2. Google OAuth para login y registro

Se separaron las rutas de entrada:

```text
/auth/oauth2/google/login
/auth/oauth2/google/registro
```

Ambas usan el callback oficial:

```text
/login/oauth2/code/google
```

Archivos:

```text
src/main/java/com/nextlevel/controller/OAuth2StartController.java
src/main/java/com/nextlevel/security/OAuth2AuthenticationSuccessHandler.java
src/main/resources/templates/login.html
src/main/resources/templates/registro.html
```

## 3. Corrección para que Google OAuth funcione

Se cambió la cookie de sesión de `Strict` a `Lax`:

```properties
server.servlet.session.cookie.same-site=${SESSION_COOKIE_SAME_SITE:Lax}
```

Esto evita fallos cuando Google devuelve el callback OAuth.

## 4. Dónde está el JWT

```text
src/main/java/com/nextlevel/security/JwtService.java
src/main/java/com/nextlevel/security/JwtAuthenticationFilter.java
src/main/java/com/nextlevel/security/JwtCookieService.java
```

## 5. Validación de correo existente

```text
GET /api/auth/email-exists?correo=correo@ejemplo.com
```

## 6. Nombre NextLevel

Se ajustaron textos visibles del login y registro para que sean de NextLevel:

```text
Inicia sesión en NextLevel
Regístrate en NextLevel
Entrar a NextLevel
Cuenta NextLevel
```

## 7. Power BI

Se agregó la sección:

```text
/admin/analitica
```

Archivos:

```text
src/main/resources/templates/admin/analitica_powerbi.html
src/main/resources/static/powerbi/Taller.pbix
```

Variable para publicar el reporte:

```bash
POWERBI_EMBED_URL=https://app.powerbi.com/reportEmbed?reportId=...
```

## 8. Despliegue

Archivos actualizados:

```text
Dockerfile
docker-compose.yml
render.yaml
src/main/resources/application.properties
src/main/resources/application-prod.properties
```

Variables importantes:

```text
MONGODB_URI
JWT_SECRET
GOOGLE_CLIENT_ID
GOOGLE_CLIENT_SECRET
APP_COOKIE_SECURE
SESSION_COOKIE_SAME_SITE
POWERBI_EMBED_URL
```
