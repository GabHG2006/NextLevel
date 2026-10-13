package com.nextlevel.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "pedidos")
public class Pedido {

    @Id
    private String id;

    private String usuarioId;
    private String correoCliente;
    private String nombreCliente;
    private String direccionEnvio;
    private String ciudadEnvio;
    private String departamentoEnvio;
    private BigDecimal subtotalProductos;
    private BigDecimal costoEnvio;
    private BigDecimal total;
    private EstadoPedido estado = EstadoPedido.PENDIENTE;
    private EstadoPago estadoPago = EstadoPago.PENDIENTE;
    private String metodoPagoTipo;
    private String metodoPagoEntidad;
    private String referenciaPago;
    private LocalDateTime fechaPago;
    private String trackingCode;
    private LocalDateTime fechaCreacion = LocalDateTime.now();
    private LocalDateTime fechaActualizacion = LocalDateTime.now();
    private List<ItemPedido> items = new ArrayList<>();

    public String getId() {
        return id;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public String getCorreoCliente() {
        return correoCliente;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public String getDireccionEnvio() {
        return direccionEnvio;
    }

    public String getCiudadEnvio() {
        return ciudadEnvio;
    }

    public String getDepartamentoEnvio() {
        return departamentoEnvio;
    }

    public BigDecimal getSubtotalProductos() {
        return subtotalProductos;
    }

    public BigDecimal getCostoEnvio() {
        return costoEnvio;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public EstadoPedido getEstado() {
        return estado;
    }

    public EstadoPago getEstadoPago() {
        return estadoPago;
    }

    public String getMetodoPagoTipo() {
        return metodoPagoTipo;
    }

    public String getMetodoPagoEntidad() {
        return metodoPagoEntidad;
    }

    public String getReferenciaPago() {
        return referenciaPago;
    }

    public LocalDateTime getFechaPago() {
        return fechaPago;
    }

    public String getTrackingCode() {
        return trackingCode;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public List<ItemPedido> getItems() {
        return items;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }

    public void setCorreoCliente(String correoCliente) {
        this.correoCliente = correoCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public void setDireccionEnvio(String direccionEnvio) {
        this.direccionEnvio = direccionEnvio;
    }

    public void setCiudadEnvio(String ciudadEnvio) {
        this.ciudadEnvio = ciudadEnvio;
    }

    public void setDepartamentoEnvio(String departamentoEnvio) {
        this.departamentoEnvio = departamentoEnvio;
    }

    public void setSubtotalProductos(BigDecimal subtotalProductos) {
        this.subtotalProductos = subtotalProductos;
    }

    public void setCostoEnvio(BigDecimal costoEnvio) {
        this.costoEnvio = costoEnvio;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public void setEstado(EstadoPedido estado) {
        this.estado = estado;
    }

    public void setEstadoPago(EstadoPago estadoPago) {
        this.estadoPago = estadoPago;
    }

    public void setMetodoPagoTipo(String metodoPagoTipo) {
        this.metodoPagoTipo = metodoPagoTipo;
    }

    public void setMetodoPagoEntidad(String metodoPagoEntidad) {
        this.metodoPagoEntidad = metodoPagoEntidad;
    }

    public void setReferenciaPago(String referenciaPago) {
        this.referenciaPago = referenciaPago;
    }

    public void setFechaPago(LocalDateTime fechaPago) {
        this.fechaPago = fechaPago;
    }

    public void setTrackingCode(String trackingCode) {
        this.trackingCode = trackingCode;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public void setItems(List<ItemPedido> items) {
        this.items = items;
    }
}
