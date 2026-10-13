# NextLevel Store - Modelo Predictivo Integrado

## Arquitectura

El modelo predictivo funciona como un microservicio independiente:

Usuario -> Spring Boot -> FastAPI -> modelo_ventas_nextlevel.pkl -> resultado

## Ejecución

### 1. Modelo Python

cd ml-api

uvicorn main:app --host 0.0.0.0 --port 8000

### 2. Backend

mvn spring-boot:run

El sistema web funciona en:

http://localhost:8081

## Demostración

1. Ingresar como administrador.
2. Abrir Modelo Predictivo AI.
3. Registrar características del producto.
4. Generar predicción.

El backend Spring Boot consume la API REST desarrollada en FastAPI y muestra el resultado dentro de NextLevel Store.
