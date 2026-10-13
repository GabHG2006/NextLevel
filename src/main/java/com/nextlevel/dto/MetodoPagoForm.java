package com.nextlevel.dto;

import java.math.BigDecimal;

import org.springframework.format.annotation.NumberFormat;

import com.nextlevel.model.TipoMetodoPago;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class MetodoPagoForm {

    @NotNull(message = "Debes seleccionar un tipo de método de pago.")
    private TipoMetodoPago tipo;

    @NotBlank(message = "Debes indicar la entidad o banco.")
    @Size(max = 60, message = "La entidad no debe superar 60 caracteres.")
    private String entidad;

    @NotBlank(message = "Debes indicar el titular del método de pago.")
    @Size(max = 80, message = "El titular no debe superar 80 caracteres.")
    private String titular;

    @NotBlank(message = "Debes indicar una referencia del medio de pago.")
    @Size(max = 80, message = "La referencia no debe superar 80 caracteres.")
    private String referencia;

    @NotNull(message = "Debes indicar el saldo disponible para la simulación.")
    @DecimalMin(value = "0.0", inclusive = true, message = "El saldo disponible no puede ser negativo.")
    @NumberFormat(style = NumberFormat.Style.NUMBER)
    private BigDecimal saldoDisponible;

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
        this.entidad = entidad;
    }

    public String getTitular() {
        return titular;
    }

    public void setTitular(String titular) {
        this.titular = titular;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public BigDecimal getSaldoDisponible() {
        return saldoDisponible;
    }

    public void setSaldoDisponible(BigDecimal saldoDisponible) {
        this.saldoDisponible = saldoDisponible;
    }
}
