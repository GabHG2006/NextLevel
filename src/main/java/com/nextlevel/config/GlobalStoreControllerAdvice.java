package com.nextlevel.config;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.nextlevel.model.Categoria;
import com.nextlevel.service.CarritoService;
import com.nextlevel.service.CategoriaService;

import jakarta.servlet.http.HttpSession;

@ControllerAdvice(annotations = Controller.class)
public class GlobalStoreControllerAdvice {

    private static final String SESSION_LOCATION = "TIENDA_UBICACION";
    private static final String SESSION_CITY = "TIENDA_CIUDAD";
    private static final String SESSION_DEPARTMENT = "TIENDA_DEPARTAMENTO";
    private static final Set<String> STORE_CATEGORY_NAMES = Set.of("coleccionables", "gamer", "ropa");

    private final CategoriaService categoriaService;
    private final CarritoService carritoService;

    public GlobalStoreControllerAdvice(CategoriaService categoriaService, CarritoService carritoService) {
        this.categoriaService = categoriaService;
        this.carritoService = carritoService;
    }

    @ModelAttribute("storeCategorias")
    public List<Categoria> storeCategorias() {
        return categoriaService.listar().stream()
                .filter(categoria -> categoria.getNombre() != null
                        && STORE_CATEGORY_NAMES.contains(categoria.getNombre().trim().toLowerCase()))
                .toList();
    }

    @ModelAttribute("cartPreview")
    public Object cartPreview(HttpSession session) {
        return carritoService.obtenerCarrito(session);
    }

    @ModelAttribute("cartCount")
    public int cartCount(HttpSession session) {
        return carritoService.contarItems(session);
    }

    @ModelAttribute("cartPreviewTotal")
    public Object cartPreviewTotal(HttpSession session) {
        return carritoService.calcularTotal(session);
    }

    @ModelAttribute("cartPreviewShipping")
    public Object cartPreviewShipping(HttpSession session) {
        return carritoService.calcularEnvioEstimado(session);
    }

    @ModelAttribute("tiendaUbicacion")
    public String tiendaUbicacion(HttpSession session) {
        Object ubicacion = session.getAttribute(SESSION_LOCATION);
        return ubicacion == null || ubicacion.toString().isBlank() ? "Agregar ubicación" : ubicacion.toString();
    }

    @ModelAttribute("tiendaCiudad")
    public String tiendaCiudad(HttpSession session) {
        Object ciudad = session.getAttribute(SESSION_CITY);
        return ciudad == null ? "" : ciudad.toString();
    }

    @ModelAttribute("tiendaDepartamento")
    public String tiendaDepartamento(HttpSession session) {
        Object departamento = session.getAttribute(SESSION_DEPARTMENT);
        return departamento == null ? "" : departamento.toString();
    }
}
