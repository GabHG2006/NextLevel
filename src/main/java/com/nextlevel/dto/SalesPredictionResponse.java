package com.nextlevel.dto;

import java.util.Map;

public class SalesPredictionResponse {
    private double prediccionVentas;
    private Map<String,Object> caracteristicasRecibidas;

    public double getPrediccionVentas() {
        return prediccionVentas;
    }

    public void setPrediccionVentas(double prediccionVentas) {
        this.prediccionVentas = prediccionVentas;
    }

    public Map<String,Object> getCaracteristicasRecibidas() {
        return caracteristicasRecibidas;
    }

    public void setCaracteristicasRecibidas(Map<String,Object> caracteristicasRecibidas) {
        this.caracteristicasRecibidas = caracteristicasRecibidas;
    }
}
