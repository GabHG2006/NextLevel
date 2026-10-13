package com.nextlevel.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class CarritoItem implements Serializable {

    private String productoId;
    private String nombre;
    private String categoriaNombre;
    private BigDecimal precio;
    private int cantidad;
    private String imagen;

    public CarritoItem() {
    }

    public CarritoItem(String productoId, String nombre, String categoriaNombre, BigDecimal precio, int cantidad, String imagen) {
        this.productoId = productoId;
        this.nombre = nombre;
        this.categoriaNombre = categoriaNombre;
        this.precio = precio;
        this.cantidad = cantidad;
        this.imagen = imagen;
    }

    public String getProductoId() {
        return productoId;
    }

    public void setProductoId(String productoId) {
        this.productoId = productoId;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCategoriaNombre() {
        return categoriaNombre;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public int getCantidad() {
        return cantidad;
    }

    public String getImagen() {
        return imagen;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setCategoriaNombre(String categoriaNombre) {
        this.categoriaNombre = categoriaNombre;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public BigDecimal getSubtotal() {
        return precio.multiply(BigDecimal.valueOf(cantidad));
    }
}
