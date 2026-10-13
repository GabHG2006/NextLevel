# Datos MongoDB incluidos para la entrega final

La aplicación siembra automáticamente estos datos al iniciar con una base de datos vacía o incompleta:

- categorias.json: 5 documentos en la colección `categorias`.
- productos.json: 1000 documentos en la colección `productos`.

Forma correcta de sustentarlo:
"En nuestra base de datos MongoDB cargamos 1000 registros/documentos en la colección productos, junto con 5 categorías necesarias para relacionarlos y filtrarlos en el aplicativo."

Importación manual opcional con mongoimport:

mongoimport --db nextlevel_db --collection categorias --file mongo-seed/categorias.json --jsonArray
mongoimport --db nextlevel_db --collection productos --file mongo-seed/productos.json --jsonArray

Nota:
- La colección `usuarios` debe seguir creándose desde la aplicación para que las contraseñas queden cifradas con BCrypt.
- La colección `pedidos` se genera desde el flujo de compra o con los pedidos demo del inicializador.
