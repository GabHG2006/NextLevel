package com.nextlevel.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.nextlevel.service.CategoriaService;
import com.nextlevel.service.ProductoService;

@Controller
public class CatalogoController {

    private final ProductoService productoService;
    private final CategoriaService categoriaService;

    public CatalogoController(ProductoService productoService, CategoriaService categoriaService) {
        this.productoService = productoService;
        this.categoriaService = categoriaService;
    }

    @GetMapping("/catalogo")
    public String catalogo(@RequestParam(required = false) String categoriaId,
            @RequestParam(required = false) String q,
            Model model) {
        model.addAttribute("productos", productoService.buscar(categoriaId, q));
        model.addAttribute("categorias", categoriaService.listar());
        model.addAttribute("categoriaId", categoriaId);
        model.addAttribute("categoriaActualNombre", (categoriaId == null || categoriaId.isBlank()) ? "Todas"
                : categoriaService.buscarPorId(categoriaId).map(c -> c.getNombre()).orElse("Todas"));
        model.addAttribute("q", q);
        return "catalogo";
    }

    @GetMapping("/producto/{id}")
    public String detalleProducto(@PathVariable String id, Model model) {
        return productoService.buscarPorId(id)
                .map(producto -> { model.addAttribute("producto", producto); return "producto_detalle"; })
                .orElse("redirect:/catalogo");
    }
}
