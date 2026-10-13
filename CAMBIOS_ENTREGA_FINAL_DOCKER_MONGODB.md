# Cambios aplicados para la entrega final

## Docker
- Se mantiene y actualiza `Dockerfile` para construir el proyecto con Maven y ejecutar el JAR con Java 25.
- Se actualiza `docker-compose.yml` para levantar MongoDB + aplicación.
- Se agrega `.env.example` para configurar Google OAuth y Power BI.
- Se agrega `README_ENTREGA_FINAL_DOCKER_DESPLIEGUE.md` con pasos de ejecución y despliegue.

## MongoDB y 1000 registros
- Se reemplazó/confirmó la carga de `src/main/resources/seed/productos.json` con 1000 documentos.
- Se confirmó `src/main/resources/seed/categorias.json` con 5 categorías.
- Se copiaron los mismos archivos a `mongo-seed/` para importación manual opcional.
- Se ajustó `DataInitializer.java` para sembrar/actualizar la colección `productos` cuando la base esté vacía o incompleta.
- Se integró el diagrama MongoDB en `/admin/base-datos`.

## Página de modelo MongoDB
- Nueva ruta: `/admin/base-datos`.
- Nueva plantilla: `templates/admin/base_datos.html`.
- Nuevo enlace en el menú administrativo: `Modelo MongoDB`.
- Se muestran conteos de productos, categorías, usuarios y pedidos.

## Power BI
- Se conserva el panel integrado en `/admin/analitica`.
- Se mantiene el nombre visual `Panel de indicadores` para que no se vea como un elemento externo.

## Errores personalizados
- Se ajustó el controlador de errores para responder en `/error`, `/403`, `/404` y `/ups`.
- Se agregó imagen propia `oops-robot.svg`.
- Se reforzó la pantalla “Ups, lo siento”.
