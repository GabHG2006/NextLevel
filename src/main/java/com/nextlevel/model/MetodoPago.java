package com.nextlevel.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MetodoPago {

    private String id;
    private TipoMetodoPago tipo;
    private String entidad;
    private String titular;
    private String referencia;
    private BigDecimal saldoDisponible = BigDecimal.ZERO;
    private boolean activo = true;
    private LocalDateTime createdAt = LocalDateTime.now();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public TipoMetodoPago getTipo() {
        return tipo;
    }

    public void setTipo(TipoMetodoPago tipo) {
        this.tipo = tipo;
    }

    public String getEntidad() {
        return entidad;
    }

    public void setEntidad(String entidad) {
        this.entidad = entidad == null ? null : entidad.trim();
    }

    public String getTitular() {
        return titular;
    }

    public void setTitular(String titular) {
        this.titular = titular == null ? null : titular.trim();
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia == null ? null : referencia.trim();
    }

    public BigDecimal getSaldoDisponible() {
        return saldoDisponible == null ? BigDecimal.ZERO : saldoDisponible;
    }

    public void setSaldoDisponible(BigDecimal saldoDisponible) {
        this.saldoDisponible = saldoDisponible == null ? BigDecimal.ZERO : saldoDisponible;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getEtiqueta() {
        String tipoLabel = tipo == null ? "METODO" : tipo.name();
        String entidadLabel = entidad == null ? "Sin entidad" : entidad;
        String referenciaLabel = referencia == null || referencia.isBlank() ? "Sin referencia" : referencia;
        return tipoLabel + " - " + entidadLabel + " (" + referenciaLabel + ")";
    }
}
