package com.nextlevel.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.nextlevel.model.CarritoItem;
import com.nextlevel.model.Producto;

import jakarta.servlet.http.HttpSession;

@Service
public class CarritoService {

    private static final String CARRITO_KEY = "CARRITO_NEXTLEVEL";
    private static final BigDecimal ENVIO_BASE = new BigDecimal("14900");

    @SuppressWarnings("unchecked")
    public List<CarritoItem> obtenerCarrito(HttpSession session) {
        Object carrito = session.getAttribute(CARRITO_KEY);
        if (carrito == null) {
            List<CarritoItem> nuevoCarrito = new ArrayList<>();
            session.setAttribute(CARRITO_KEY, nuevoCarrito);
            return nuevoCarrito;
        }
        return (List<CarritoItem>) carrito;
    }

    public void agregarProducto(HttpSession session, Producto producto, int cantidad) {
        List<CarritoItem> carrito = obtenerCarrito(session);
        int cantidadFinal = Math.max(1, cantidad);

        for (CarritoItem item : carrito) {
            if (item.getProductoId().equals(producto.getId())) {
                item.setCantidad(item.getCantidad() + cantidadFinal);
                session.setAttribute(CARRITO_KEY, carrito);
                return;
            }
        }

        carrito.add(new CarritoItem(producto.getId(), producto.getNombre(), producto.getCategoriaNombre(),
                producto.getPrecio(), cantidadFinal, producto.getImagen()));
        session.setAttribute(CARRITO_KEY, carrito);
    }

    public void actualizarCantidad(HttpSession session, String productoId, int cantidad) {
        List<CarritoItem> carrito = obtenerCarrito(session);
        carrito.removeIf(item -> item.getProductoId().equals(productoId) && cantidad <= 0);
        for (CarritoItem item : carrito) {
            if (item.getProductoId().equals(productoId) && cantidad > 0) {
                item.setCantidad(cantidad);
            }
        }
        session.setAttribute(CARRITO_KEY, carrito);
    }

    public void eliminarProducto(HttpSession session, String productoId) {
        List<CarritoItem> carrito = obtenerCarrito(session);
        carrito.removeIf(item -> item.getProductoId().equals(productoId));
        session.setAttribute(CARRITO_KEY, carrito);
    }

    public void limpiar(HttpSession session) {
        session.removeAttribute(CARRITO_KEY);
    }

    public BigDecimal calcularTotal(HttpSession session) {
        return obtenerCarrito(session).stream().map(CarritoItem::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int contarItems(HttpSession session) {
        return obtenerCarrito(session).stream().mapToInt(CarritoItem::getCantidad).sum();
    }

    public BigDecimal calcularEnvioEstimado(HttpSession session) {
        return estaVacio(session) ? BigDecimal.ZERO : ENVIO_BASE;
    }

    public BigDecimal calcularTotalVisual(HttpSession session, boolean retiroEnTienda) {
        return calcularTotal(session).add(retiroEnTienda ? BigDecimal.ZERO : calcularEnvioEstimado(session));
    }

    public boolean estaVacio(HttpSession session) {
        return obtenerCarrito(session).isEmpty();
    }
}
