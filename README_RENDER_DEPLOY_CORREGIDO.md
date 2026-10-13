# Despliegue corregido para Render

Este proyecto quedó ajustado para que Render no vuelva a fallar por `JWT_SECRET` ni por falta del archivo `application.properties`.

## Cambios aplicados

- Se corrigió `JwtService.java` para que no se caiga si `JWT_SECRET` está vacío, corto o mal escrito.
- Se agregó una clave JWT demo segura en `application.properties` y `application-prod.properties`.
- Se eliminaron credenciales reales de Google OAuth del código.
- Se dejó `application.properties` limpio y apto para subir a GitHub.
- Se quitó `src/main/resources/application.properties` del `.gitignore`, porque ahora no contiene secretos.
- Se agregó soporte demo para que el catálogo use los JSON de `src/main/resources/seed` si MongoDB no está disponible.
- Se protegió `DataInitializer` para que la aplicación no se caiga si MongoDB no conecta durante el arranque.
- Se dejó `render.yaml` actualizado con variables básicas.
- Se corrigió el checkout para incluir ciudad, departamento y dirección en la misma sección.
- Se corrigió el cálculo del total: subtotal + envío, y ese total es el que se descuenta del método de pago.
- Se agregó soporte para iframe público de Power BI con `POWERBI_PUBLIC_EMBED_URL`.

## Configuración recomendada en Render

En Render, usar:

- Language: `Docker`
- Branch: `main`
- Root Directory: vacío

Variables mínimas para que arranque:

```env
SPRING_PROFILES_ACTIVE=prod
JWT_SECRET=nextlevel-store-demo-jwt-secret-change-in-render-2026-very-long-key
JWT_EXPIRATION_MS=86400000
APP_COOKIE_SECURE=true
SESSION_COOKIE_SAME_SITE=Lax
JWT_COOKIE_SAME_SITE=Lax
# Para que el reporte NO pida login, usa el src publico generado en Power BI.
POWERBI_PUBLIC_EMBED_URL=https://app.powerbi.com/view?r=PEGA_AQUI_TU_CODIGO_PUBLICO
```

Variables recomendadas para que todo funcione completo:

```env
MONGODB_URI=mongodb+srv://usuario:password@cluster.mongodb.net/nextlevel_db?retryWrites=true&w=majority
GOOGLE_CLIENT_ID=tu_client_id_real_de_google
GOOGLE_CLIENT_SECRET=tu_client_secret_real_de_google
```

## Importante

Si en Render aparecen filas vacías de variables de entorno, bórralas con el ícono de basura. Render marca error si hay variables con nombre vacío.

Si no configuras `MONGODB_URI`, la aplicación puede iniciar y mostrar catálogo con datos demo desde JSON, pero la base de datos real no estará conectada. Para cumplir la entrega de Base de Datos II, configura MongoDB Atlas en `MONGODB_URI`.

Para Google OAuth, cuando tengas la URL de Render, agrégala en Google Cloud Console como redirect URI:

```text
https://TU-APP.onrender.com/login/oauth2/code/google
```


## Power BI sin inicio de sesión

El enlace anterior `reportEmbed` puede pedir login. Para que cualquier persona vea el tablero dentro del sistema, en Power BI debes usar:

```text
Archivo > Insertar informe > Publicar en la web
```

Power BI entrega un iframe. Copia solo el valor de `src`, que normalmente empieza por:

```text
https://app.powerbi.com/view?r=...
```

Ese valor se pega en Render como:

```env
POWERBI_PUBLIC_EMBED_URL=https://app.powerbi.com/view?r=...
```

Si esta variable queda vacía, el sistema muestra la imagen del panel como vista previa y evita mostrar un iframe que pida login.
