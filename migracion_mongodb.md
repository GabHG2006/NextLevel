# Migración del modelo relacional a MongoDB

## Conversión usada en este proyecto

### SQL -> Colecciones MongoDB

- `categoria` -> `categorias`
- `cliente` -> `usuarios`
- `producto` -> `productos`
- `pedidos` -> `pedidos`

## Decisiones de modelado

### usuarios
Se unifican clientes y administradores en una sola colección y se maneja el acceso con el campo `rol`:
- `ROLE_ADMIN`
- `ROLE_CLIENTE`

Las contraseñas se guardan con BCrypt, nunca en texto plano.

### productos
Cada documento guarda:
- `id`
- `categoriaId`
- `categoriaNombre`
- `nombre`
- `descripcion`
- `precio`
- `stock`
- `imagen`
- `activo`

### pedidos
Cada pedido guarda los items embebidos para evitar joins:
- datos del cliente
- total
- estado
- tracking
- lista `items`

## Datos semilla
En `src/main/resources/seed` se incluyen:
- `categorias.json`
- `productos.json`
- `clientes_legacy.json`

La clase `DataInitializer` carga estos archivos automáticamente cuando la base está vacía.
