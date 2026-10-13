# Modelo predictivo NextLevel Store

## Arquitectura

Spring Boot funciona como backend principal y consume el servicio FastAPI donde está alojado el modelo de Machine Learning.

Flujo:

Usuario administrador -> NextLevel Spring Boot -> FastAPI -> modelo_ventas_nextlevel.pkl -> predicción.

## Ejecución

### Backend Java

```bash
mvn spring-boot:run
```

Abrir:
http://localhost:8080

### Modelo Python

Desde la carpeta ml-api:

```bash
uvicorn main:app --host 0.0.0.0 --port 8000
```

Ver documentación:
http://localhost:8000/docs

## Ver demostración

1. Iniciar sesión como administrador.
2. Entrar a:
http://localhost:8080/admin/prediccion
3. Ingresar datos del producto.
4. Presionar "Generar predicción".
5. El sistema enviará los datos al modelo y mostrará la estimación.

