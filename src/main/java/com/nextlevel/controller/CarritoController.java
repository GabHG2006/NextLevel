package com.nextlevel.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.nextlevel.model.Producto;
import com.nextlevel.service.CarritoService;
import com.nextlevel.service.ProductoService;

import jakarta.servlet.http.HttpSession;

@Controller
public class CarritoController {

    private final CarritoService carritoService;
    private final ProductoService productoService;

    public CarritoController(CarritoService carritoService, ProductoService productoService) {
        this.carritoService = carritoService;
        this.productoService = productoService;
    }

    @GetMapping("/carrito")
    public String verCarrito(Model model, HttpSession session) {
        model.addAttribute("carrito", carritoService.obtenerCarrito(session));
        model.addAttribute("total", carritoService.calcularTotal(session));
        model.addAttribute("envioEstimado", carritoService.calcularEnvioEstimado(session));
        model.addAttribute("totalVisual", carritoService.calcularTotalVisual(session, false));
        return "carrito";
    }

    @GetMapping("/carrito/agregar/{id}")
    public String agregarRapido(@PathVariable String id, HttpSession session, RedirectAttributes redirectAttributes) {
        return productoService.buscarPorId(id)
                .map(producto -> agregarProductoConValidacion(producto, 1, session, redirectAttributes))
                .orElse("redirect:/catalogo");
    }

    @PostMapping("/carrito/agregar/{id}")
    public String agregar(@PathVariable String id,
            @RequestParam(defaultValue = "1") Integer cantidad,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        return productoService.buscarPorId(id)
                .map(producto -> agregarProductoConValidacion(producto, cantidad, session, redirectAttributes))
                .orElse("redirect:/catalogo");
    }

    @PostMapping("/carrito/actualizar/{id}")
    public String actualizarCantidad(@PathVariable String id,
            @RequestParam(required = false) Integer cantidad,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        int cantidadFinal = cantidad == null ? 1 : cantidad;
        productoService.buscarPorId(id).ifPresent(producto -> {
            if (cantidadFinal > producto.getStock()) {
                carritoService.actualizarCantidad(session, id, producto.getStock());
                redirectAttributes.addFlashAttribute("errorCarrito", "La cantidad se ajustó al stock disponible.");
            } else {
                carritoService.actualizarCantidad(session, id, cantidadFinal);
            }
        });
        return "redirect:/carrito";
    }

    @GetMapping("/carrito/eliminar/{id}")
    public String eliminar(@PathVariable String id, HttpSession session, RedirectAttributes redirectAttributes) {
        carritoService.eliminarProducto(session, id);
        redirectAttributes.addFlashAttribute("mensaje", "Producto eliminado del carrito.");
        return "redirect:/carrito";
    }

    @GetMapping("/carrito/limpiar")
    public String limpiar(HttpSession session, RedirectAttributes redirectAttributes) {
        carritoService.limpiar(session);
        redirectAttributes.addFlashAttribute("mensaje", "Se vació el carrito correctamente.");
        return "redirect:/carrito";
    }

    private String agregarProductoConValidacion(Producto producto, Integer cantidad, HttpSession session,
            RedirectAttributes redirectAttributes) {
        int qty = cantidad == null ? 1 : Math.max(1, cantidad);
        if (producto.getStock() <= 0) {
            redirectAttributes.addFlashAttribute("errorCarrito", "Este producto no tiene stock disponible.");
            return "redirect:/producto/" + producto.getId();
        }
        if (qty > producto.getStock()) {
            qty = producto.getStock();
            redirectAttributes.addFlashAttribute("errorCarrito", "Se agregó la cantidad disponible en stock.");
        } else {
            redirectAttributes.addFlashAttribute("mensaje", "Producto agregado al carrito.");
        }
        carritoService.agregarProducto(session, producto, qty);
        return "redirect:/carrito";
    }
}
