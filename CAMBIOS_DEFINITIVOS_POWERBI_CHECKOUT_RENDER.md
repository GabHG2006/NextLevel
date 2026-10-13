# Cambios definitivos: Power BI público, checkout y Render

## 1. Dashboard público sin login

Se integró directamente el iframe público generado desde Power BI con la opción:

`Archivo > Insertar informe > Publicar en la web`

La URL pública configurada es:

`https://app.powerbi.com/view?r=eyJrIjoiZjI0Mzk1NGEtYTEyOC00M2JiLWFhYzQtOTc0ZDY3ZjMyZjg5IiwidCI6IjlkMTJiZjNmLWU0ZjYtNDdhYi05MTJmLTFhMmYwZmM0OGFhNCIsImMiOjR9`

La vista del sistema sigue apareciendo como **Panel de indicadores** y **Vista interactiva**, para que no se perciba como un módulo externo.

Archivos ajustados:

- `src/main/resources/application.properties`
- `src/main/resources/application-prod.properties`
- `src/main/resources/templates/admin/analitica_powerbi.html`
- `render.yaml`
- `archivo.env`

## 2. Lógica de checkout corregida

Se mantiene la lógica completa:

- Subtotal = suma de productos del carrito.
- Envío a domicilio = $14.900.
- Recoger en tienda = $0 de envío.
- Total final = subtotal + envío aplicable.
- Al pagar, se descuenta el total final del método de pago seleccionado.
- Al confirmar el pedido, se descuenta el stock de los productos comprados.
- Si ocurre un error al guardar el pedido, se restaura el stock y se devuelve el saldo al método de pago.

## 3. Precios corregidos a pesos colombianos

Los 1000 productos del seed quedaron normalizados a valores de tienda en pesos colombianos. Por ejemplo, un valor que estaba como `116.57` ahora queda como `116570`, para que la interfaz muestre valores como `$116.570` y no cifras irreales.

También se agregó normalización automática en la inicialización para corregir productos existentes que hayan quedado con precios menores a 1000.

Archivos ajustados:

- `src/main/resources/seed/productos.json`
- `mongo-seed/productos.json`
- `src/main/java/com/nextlevel/config/DataInitializer.java`
- `src/main/java/com/nextlevel/service/ProductoService.java`

## 4. Despliegue en Render

El proyecto queda listo para subir a GitHub y desplegar en Render con Docker.

Variables mínimas recomendadas en Render:

```env
SPRING_PROFILES_ACTIVE=prod
JWT_SECRET=nextlevel-store-demo-jwt-secret-change-in-render-2026-very-long-key
JWT_EXPIRATION_MS=86400000
APP_COOKIE_SECURE=true
SESSION_COOKIE_SAME_SITE=Lax
JWT_COOKIE_SAME_SITE=Lax
POWERBI_PUBLIC_EMBED_URL=https://app.powerbi.com/view?r=eyJrIjoiZjI0Mzk1NGEtYTEyOC00M2JiLWFhYzQtOTc0ZDY3ZjMyZjg5IiwidCI6IjlkMTJiZjNmLWU0ZjYtNDdhYi05MTJmLTFhMmYwZmM0OGFhNCIsImMiOjR9
```

Para base de datos real:

```env
MONGODB_URI=tu_uri_de_mongodb_atlas
```

Para OAuth con Google:

```env
GOOGLE_CLIENT_ID=tu_client_id_real
GOOGLE_CLIENT_SECRET=tu_client_secret_real
```
