# Cambios de interfaz y flujo - NextLevel Store

## Objetivo
Ajustar la tienda para que tenga una interfaz más profesional, gamer y friki, tomando como referencia la lógica visual de una tienda comercial, sin cambiar la base de seguridad ni MongoDB.

## Cambios aplicados
- Nuevo estilo visual en tonos morados con mejor contraste.
- Logo de la tienda reemplazado por imagen fija.
- Banners principales tipo carrusel usando imágenes promocionales propias.
- Categorías superiores reducidas a: Coleccionables, Gamer y Ropa.
- Se eliminaron textos guía innecesarios para que la tienda se vea más natural.
- Cards de productos con un solo botón principal para abrir el producto y agregar al carrito.
- Carrito visible sin iniciar sesión.
- Modal para seleccionar ubicación por ciudad y departamento.
- Datos de ubicación reutilizados en el flujo de compra.
- Checkout mantenido con la lógica de iniciar sesión al final para pagar.
- Dashboard admin conservado con enfoque en tablas.

## Flujo esperado
1. Entrar al catálogo.
2. Seleccionar ubicación si el usuario lo desea.
3. Navegar por categorías o buscar productos.
4. Abrir un producto, elegir cantidad y agregar al carrito.
5. Revisar el carrito.
6. Completar datos de entrega.
7. Iniciar sesión al final para pagar.
8. Finalizar compra.

## Nota
En este entorno no se pudo ejecutar Maven para validar compilación final porque la herramienta no está instalada y no hay acceso a internet para descargarla. Se recomienda correr localmente:

```bash
mvn clean package
```

o

```bash
mvn spring-boot:run
```


## Ajustes finales
- Se corrigió el estilo del checkout para que radios y checkboxes no deformen la vista.
- Se ajustó el resumen de compra para que las imágenes queden centradas y con tamaño consistente.
- Se mejoró la visual del dashboard admin con fondo morado y texto blanco.
- El catálogo principal ahora prioriza productos nuevos o editados recientemente y evita repeticiones consecutivas del mismo nombre base.
- Se mejoró el tamaño de imágenes en cards y resumen para que no rompan la cuadrícula.
