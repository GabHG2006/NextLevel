# Cambios finales: select de ubicación y despliegue Render

## Ubicación en checkout

Se reemplazaron los campos escritos de ciudad y departamento por listas desplegables.

- Ciudad: lista de ciudades disponibles para Colombia.
- Departamento: lista de departamentos asociados.
- Dirección: se mantiene como campo de texto porque depende del usuario.

La lista de ciudades se filtra automáticamente según el departamento seleccionado.

## Ubicación en modal superior

También se actualizó el modal de ubicación de la tienda para que use listas desplegables y mantenga coherencia con el checkout.

## Render

El proyecto conserva la configuración segura para Render:

- `application.properties` no contiene credenciales reales.
- `render.yaml` contiene variables demo o `sync: false` para datos sensibles.
- El dashboard usa iframe público de Power BI mediante `POWERBI_PUBLIC_EMBED_URL`.

## Subida a GitHub

Después de reemplazar los archivos del proyecto:

```powershell
git add .
git commit -m "Add Colombia location selectors and final Render UI fixes"
git push
```

Después, en Render:

```text
Manual Deploy > Deploy latest commit
```
