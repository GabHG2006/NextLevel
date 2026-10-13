from fastapi import FastAPI
import joblib
import numpy as np
from pydantic import BaseModel

# Inicializar la aplicación FastAPI
app = FastAPI(title="API Modelo Predictivo NextLevel", version="1.0")

# Cargar el modelo entrenado
modelo = joblib.load("modelo_ventas_nextlevel.pkl")

# Definir la estructura de los datos de entrada usando Pydantic
class DatosProducto(BaseModel):
    precio: float
    descuento: float
    calificacion: float
    stock: int
    popularidad_franquicia: float

@app.get("/")
def home():
    return {"mensaje": "¡El modelo predictivo de NextLevel está activo y funcionando!"}

@app.post("/predecir")
def predecir_ventas(datos: DatosProducto):
    # Organizar los datos en el formato que espera scikit-learn
    entrada = np.array([[
        datos.precio,
        datos.descuento,
        datos.calificacion,
        datos.stock,
        datos.popularidad_franquicia
    ]])
    
    # Realizar la predicción
    prediccion = modelo.predict(entrada)
    
    return {
        "prediccion_ventas": float(prediccion[0]),
        "caracteristicas_recibidas": datos.dict()
    }