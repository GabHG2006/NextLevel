package com.nextlevel.dto;

public class SalesPredictionRequest {
    private double precio;
    private double descuento;
    private double calificacion;
    private int stock;
    private double popularidad_franquicia;

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }
    public double getDescuento() { return descuento; }
    public void setDescuento(double descuento) { this.descuento = descuento; }
    public double getCalificacion() { return calificacion; }
    public void setCalificacion(double calificacion) { this.calificacion = calificacion; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public double getPopularidad_franquicia() { return popularidad_franquicia; }
    public void setPopularidad_franquicia(double popularidad_franquicia) { this.popularidad_franquicia = popularidad_franquicia; }
}
