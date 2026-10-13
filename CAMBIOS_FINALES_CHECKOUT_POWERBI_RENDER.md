# Cambios finales aplicados

## 1. Checkout y datos de entrega

Se actualizó la pantalla de checkout para mostrar, en la misma zona de datos:

- Ciudad
- Departamento
- Dirección

La dirección queda asociada a la ciudad y al departamento. Si el usuario selecciona **Recoger en tienda**, la dirección puede quedar vacía.

## 2. Lógica de total, envío y descuento

Se corrigió la lógica de pago:

- El subtotal corresponde a la suma de los productos del carrito.
- El envío ahora tiene valor de $14.900 COP cuando la entrega es a domicilio.
- Si el usuario selecciona recoger en tienda, el envío queda en $0.
- El total final es subtotal + envío.
- El valor que se descuenta del método de pago es el total final, no solo el subtotal.
- El pedido guarda subtotal, envío y total descontado.
- Al confirmar la compra, también se descuenta el stock del producto.

## 3. Power BI público

Se agregó soporte para un iframe público de Power BI.

Variable nueva recomendada en Render:

```env
POWERBI_PUBLIC_EMBED_URL=https://app.powerbi.com/view?r=...
```

Esta URL debe salir de Power BI con:

Archivo > Insertar informe > Publicar en la web

Luego se copia el valor del atributo `src` del iframe y se pega en Render como `POWERBI_PUBLIC_EMBED_URL`.

Si se usa `POWERBI_EMBED_URL` con `reportEmbed`, Power BI puede pedir inicio de sesión. Por eso, para la sustentación se recomienda usar `POWERBI_PUBLIC_EMBED_URL`.

## 4. Render

Después de subir estos cambios a GitHub, Render puede reconstruir el servicio desde la rama `main`.

Variables mínimas:

```env
SPRING_PROFILES_ACTIVE=prod
JWT_SECRET=nextlevel-store-demo-jwt-secret-change-in-render-2026-very-long-key
JWT_EXPIRATION_MS=86400000
APP_COOKIE_SECURE=true
SESSION_COOKIE_SAME_SITE=Lax
JWT_COOKIE_SAME_SITE=Lax
POWERBI_PUBLIC_EMBED_URL=https://app.powerbi.com/view?r=...
```

Variables opcionales pero recomendadas:

```env
MONGODB_URI=tu_uri_de_mongodb_atlas
GOOGLE_CLIENT_ID=tu_client_id_real
GOOGLE_CLIENT_SECRET=tu_client_secret_real
```
