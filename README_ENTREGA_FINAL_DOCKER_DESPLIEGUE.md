# Entrega final: Docker, MongoDB, Dashboard y despliegue

## 1. Software Web Frontend - Backend

El aplicativo web está construido con Spring Boot, Thymeleaf, Spring Security, JWT, OAuth2 Google y MongoDB. El frontend se renderiza desde las plantillas `src/main/resources/templates` y el backend queda en `src/main/java/com/nextlevel`.

Rutas principales:

- `/` pantalla de introducción de NextLevel.
- `/catalogo` catálogo público.
- `/login` inicio de sesión tradicional y Google.
- `/registro` registro tradicional.
- `/admin/dashboard` módulo administrativo.
- `/admin/base-datos` diagrama y explicación de MongoDB.
- `/admin/analitica` panel de indicadores integrado con Power BI.

## 2. Docker del aplicativo

El proyecto incluye:

- `Dockerfile`: construye el `.jar` con Maven y ejecuta la aplicación con Java 25.
- `docker-compose.yml`: levanta MongoDB y la aplicación completa.
- `.dockerignore`: evita copiar archivos innecesarios al contenedor.
- `.env.example`: variables necesarias para OAuth y Power BI.

Ejecución local con Docker:

```bash
docker compose up --build
```

Después de levantar el contenedor, el aplicativo queda disponible en:

```text
http://localhost:8080
```

## 3. Base de Datos II: MongoDB

La base de datos es NoSQL y queda conectada mediante la variable:

```text
MONGODB_URI=mongodb://mongodb:27017/nextlevel_db
```

Colecciones principales:

- `usuarios`: usuarios, roles, bloqueo de cuenta y métodos de pago simulados.
- `categorias`: 5 categorías de la tienda.
- `productos`: 1000 registros/documentos del catálogo.
- `pedidos`: compras, pagos y seguimiento.

Los datos están incluidos en:

```text
src/main/resources/seed/categorias.json
src/main/resources/seed/productos.json
mongo-seed/categorias.json
mongo-seed/productos.json
```

La aplicación carga automáticamente los datos desde `DataInitializer.java` cuando la base está vacía o incompleta.

## 4. Diagrama de base de datos

El diagrama se integró visualmente en:

```text
/admin/base-datos
```

Archivo usado:

```text
src/main/resources/static/images/database/diagrama-mongodb-nextlevel.png
```

## 5. Visualización de datos I: Dashboard Power BI

El dashboard se muestra dentro del aplicativo en:

```text
/admin/analitica
```

No aparece como una página externa, sino como un módulo interno llamado **Panel de indicadores**.

El enlace de Power BI se configura con:

```text
POWERBI_EMBED_URL=https://app.powerbi.com/reportEmbed?reportId=...
```

## 6. Despliegue en la nube

El despliegue significa que el sistema pueda abrirse desde una URL pública, no solo desde `localhost`.

Este proyecto queda preparado para desplegarse en Render, Railway, Azure, AWS u otro servicio que soporte Docker.

Para Render se incluye:

```text
render.yaml
```

Variables mínimas en la nube:

```text
SPRING_PROFILES_ACTIVE=prod
MONGODB_URI=uri_de_mongodb_en_la_nube
JWT_SECRET=clave_larga_y_segura
GOOGLE_CLIENT_ID=client_id_de_google
GOOGLE_CLIENT_SECRET=client_secret_de_google
POWERBI_EMBED_URL=url_embed_de_power_bi
APP_COOKIE_SECURE=true
```

Cuando tengas la URL pública, en Google Cloud Console debes agregar este redirect URI:

```text
https://TU_URL_PUBLICA/login/oauth2/code/google
```

## 7. Errores personalizados

Se corrigió el manejo de errores para evitar pantallas genéricas. Ahora se muestra una pantalla tipo:

```text
Ups, lo siento
```

con una imagen propia de Oops y botones para regresar al sistema.

Archivos principales:

```text
src/main/java/com/nextlevel/controller/ErrorPageController.java
src/main/resources/templates/error/inteligente.html
src/main/resources/static/images/errors/oops-robot.svg
```
