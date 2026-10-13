# NextLevel Store Secure

Proyecto actualizado de la tienda **NextLevel** para V semestre usando:

- Java 25
- Spring Boot 3.2.5
- Spring Security
- Spring Data MongoDB
- Thymeleaf
- MongoDB

## Qué se actualizó

- Migración de **JPA/MySQL** a **MongoDB**
- Unificación de usuarios en una sola colección `usuarios`
- Seguridad con **Spring Security**
- Contraseñas cifradas con **BCrypt**
- Roles `ADMIN` y `CLIENTE`
- Bloqueo temporal de cuenta tras **5 intentos fallidos**
- Expiración de sesión por inactividad
- Restricción de una sola sesión por usuario
- Carrito en sesión
- Gestión de productos, pedidos y seguimiento
- Simulación de checkout con métodos de pago (Tarjeta, Nequi y PayPal)
- Validación de fondos insuficientes antes de aprobar la compra
- Búsqueda por categoría o palabra clave
- Páginas personalizadas **403**, **404** y **500** con pantalla “Ups, lo siento”
- Reporte básico de stock bajo

## Credenciales de prueba

Se crean automáticamente al iniciar si la base de datos está vacía:

### Administrador
- Correo: `admin@nextlevel.com`
- Contraseña: `Admin123*`

### Cliente
- Correo: `cliente@nextlevel.com`
- Contraseña: `Cliente123*`

## Configuración de MongoDB

Por defecto usa:

```properties
spring.data.mongodb.uri=mongodb://localhost:27017/nextlevel_db
```

Puedes cambiarla con variable de entorno:

```bash
MONGODB_URI=mongodb://localhost:27017/nextlevel_db
```

## Ejecución rápida

### Opción 1: MongoDB local instalado
1. Inicia MongoDB
2. Abre el proyecto en VS Code
3. Ejecuta:

```bash
mvn spring-boot:run
```

### Opción 2: Docker
En la raíz del proyecto hay un `docker-compose.yml` para levantar MongoDB:

```bash
docker compose up -d
mvn spring-boot:run
```

## URL base
- Aplicación: `http://localhost:8080`

## Rutas principales

### Públicas
- `/`
- `/login`
- `/registro`
- `/catalogo`
- `/producto/{id}`

### Cliente autenticado
- `/carrito`
- `/carrito/agregar/{id}`
- `/pedido/detalle`
- `/pedido/realizar`
- `/pedido/mis-pedidos`
- `/perfil/pagos`

### Administrador
- `/admin/productos`
- `/admin/productos/nuevo`
- `/admin/pedidos`
- `/admin/reportes/stock`

## Notas de seguridad

En `application.properties` la cookie queda lista para trabajo local.  
En `application-prod.properties` se activa `Secure=true` para despliegue HTTPS.

## Requisitos no funcionales cubiertos

- Sesión con `HttpOnly` y `SameSite=Lax` para permitir el retorno de Google OAuth
- Límite de sesión por usuario
- Tiempo de expiración por inactividad
- BCrypt para contraseñas
- Cuenta bloqueada por intentos fallidos
- Páginas 403 y 404 personalizadas

## Estructura base

- `com.nextlevel.model`
- `com.nextlevel.repository`
- `com.nextlevel.service`
- `com.nextlevel.controller`
- `com.nextlevel.security`
- `com.nextlevel.config`


## Migración SQL -> MongoDB incluida

Esta versión ya trae una migración inicial hacia MongoDB usando los datos de `nextleveltienda.sql`:

- **5 categorías** migradas a la colección `categorias`
- **1000 registros/documentos** migrados a la colección `productos`
- **5 clientes legacy** migrados a la colección `usuarios` con contraseña cifrada en BCrypt
- Más los usuarios demo de seguridad (`admin@nextlevel.com` y `cliente@nextlevel.com`)

### Contraseña temporal de clientes migrados
- Contraseña inicial: `NextLevel123*`

> Importante: si ya habías ejecutado una versión anterior con otra estructura, usa una base de datos nueva o elimina la base `nextlevel_db` antes de correr esta versión para que se vuelva a sembrar completa.

## Colecciones MongoDB resultantes

- `usuarios`
- `categorias`
- `productos`
- `pedidos`


## Datos demo adicionales

Al iniciar por primera vez también se crean **2 pedidos demo** para que el administrador pueda validar:
- listado de pedidos
- actualización de estado
- tracking de envío


## Flujo de pago simulado

- El cliente administra sus medios de pago desde `\/perfil\/pagos`.
- Puede registrar **Tarjeta**, **Nequi** o **PayPal** con entidad, referencia y saldo disponible.
- En `\/pedido\/detalle` el sistema solicita dirección y medio de pago.
- Si el saldo no alcanza, la compra se rechaza y el carrito se conserva.
- Si el saldo alcanza, el pedido queda **PAGADO** y luego el administrador puede marcarlo como **ENVIADO** con tracking.


## Seguridad actualizada: JWT + OAuth2 Google

Esta entrega agrega lo solicitado para la guía de seguridad:

- Dependencia `spring-boot-starter-oauth2-client`.
- Dependencias `jjwt` para JSON Web Token.
- JWT separado de la lógica de negocio en `com.nextlevel.security`.
- Login tradicional que ahora también genera cookie `JWT_TOKEN`.
- Login con Google OAuth2 en `/oauth2/authorization/google`.
- `OAuth2AuthenticationSuccessHandler` para buscar o crear el usuario de Google en MongoDB, asignar rol `ROLE_CLIENTE`, generar JWT y redirigir al catálogo.
- Endpoint para validar si un correo existe: `GET /api/auth/email-exists?correo=...`.
- Endpoints API para JWT:
  - `POST /api/auth/login`
  - `POST /api/auth/registro`
  - `GET /api/auth/me`
- Manejo de errores JSON para API y pantallas visuales para 403, 404, 500 y `/ups`.
- Archivos de despliegue: `Dockerfile`, `.dockerignore`, `render.yaml` y `README_SEGURIDAD_DESPLIEGUE.md`.

### Variables para OAuth y JWT

```bash
JWT_SECRET=una_clave_larga_de_minimo_32_caracteres
GOOGLE_CLIENT_ID=tu_client_id
GOOGLE_CLIENT_SECRET=tu_client_secret
MONGODB_URI=mongodb://localhost:27017/nextlevel_db
```

Para desarrollo local con el puerto actual, registra en Google esta URL de redirección:

```text
http://localhost:8080/login/oauth2/code/google
```

Para producción, registra la URL con el dominio real:

```text
https://TU-DOMINIO/login/oauth2/code/google
```


## Entrega final añadida

Esta versión también incluye:

- Docker completo del aplicativo (`Dockerfile`, `docker-compose.yml`, `.dockerignore`).
- Archivo `.env.example` para configurar OAuth Google y Power BI en Docker.
- Carga masiva de **1000 registros/documentos** en la colección `productos` y 5 categorías en `categorias`.
- Página `/admin/base-datos` con el diagrama MongoDB integrado al aplicativo.
- Dashboard de Power BI integrado en `/admin/analitica` como **Panel de indicadores**.
- Guía `README_ENTREGA_FINAL_DOCKER_DESPLIEGUE.md` para explicar despliegue, Docker, MongoDB y dashboard.
