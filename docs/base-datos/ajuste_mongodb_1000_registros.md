# Ajuste de MongoDB para la sustentación

## Cambio de redacción
Donde antes se decía **1000 productos**, ahora debe decirse:

**La colección `productos` contiene 1000 registros en MongoDB.**

## Forma correcta de explicarlo
- En MongoDB no hablamos de filas o tablas, sino de **documentos dentro de colecciones**.
- Por eso, técnicamente lo correcto es decir:
  - **1000 registros**
  - o mejor aún: **1000 documentos en la colección `productos`**

## Frase lista para decir mañana
**“En nuestra base de datos MongoDB cargamos 1000 registros en la colección `productos`, junto con las categorías necesarias para relacionarlos.”**

## Soporte con archivos cargados
- El archivo de categorías contiene 5 registros de categorías.
- El archivo principal corresponde a la carga masiva de 1000 registros de productos.

## Nota
Este ajuste es solo de redacción y presentación.
No cambia la lógica del proyecto, no afecta el ZIP, y sigue funcionando con el sistema actual.
