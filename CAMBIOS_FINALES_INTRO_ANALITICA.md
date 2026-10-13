# Cambios finales aplicados

## 1. Página de introducción
- La ruta `/` ya no redirige automáticamente al catálogo.
- Ahora muestra una pantalla de bienvenida de NextLevel Store.
- Se agregó fondo personalizado con sombra oscura.
- Se agregó logo de NextLevel.
- Se agregó botón `Continuar` que redirige a `/catalogo`.

Archivos modificados:
- `src/main/java/com/nextlevel/controller/HomeController.java`
- `src/main/resources/templates/index.html`
- `src/main/resources/static/css/style.css`
- `src/main/resources/static/images/store/intro-nextlevel-bg.jpg`

## 2. Panel de indicadores integrado
- Se cambió la presentación para que no aparezca como un módulo externo evidente.
- El menú ahora muestra `Panel de indicadores`.
- El dashboard administrativo muestra `Ver panel de indicadores`.
- La vista `/admin/analitica` usa lenguaje propio del sistema: reporte de gestión, tablero de visualización y vista interactiva.
- Se agregó la imagen del panel de visualización como vista previa institucional.
- Se dejó configurado el enlace embebido del reporte:

```properties
app.powerbi.embed-url=${POWERBI_EMBED_URL:https://app.powerbi.com/reportEmbed?reportId=a11881bc-8eea-49a4-8e4f-8500ed3954e2&autoAuth=true&ctid=9d12bf3f-e4f6-47ab-912f-1a2f0fc48aa4}
```

Archivos modificados:
- `src/main/resources/templates/admin/analitica_powerbi.html`
- `src/main/resources/templates/fragments/admin-header.html`
- `src/main/resources/templates/admin/dashboard.html`
- `src/main/resources/application.properties`
- `src/main/resources/application-prod.properties`
- `src/main/resources/static/css/style.css`
- `src/main/resources/static/images/store/panel-visualizacion-nextlevel.png`

## 3. Explicación breve para exposición
La aplicación cuenta con una página de introducción personalizada que dirige al usuario hacia la tienda. En el módulo administrativo se integró un panel de indicadores construido a partir de los registros del proyecto. El reporte se presenta dentro de NextLevel como una vista de analítica interna, evitando que se perciba como un archivo externo. La vista interactiva se carga mediante el enlace embebido configurado en la propiedad `POWERBI_EMBED_URL`.
