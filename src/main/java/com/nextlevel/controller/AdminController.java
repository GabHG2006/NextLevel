package com.nextlevel.controller;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.nextlevel.model.EstadoPago;
import com.nextlevel.model.EstadoPedido;
import com.nextlevel.model.Pedido;
import com.nextlevel.model.Producto;
import com.nextlevel.repository.UsuarioRepository;
import com.nextlevel.service.CategoriaService;
import com.nextlevel.service.PedidoService;
import com.nextlevel.service.ProductoService;

@Controller
public class AdminController {

    private final ProductoService productoService;
    private final CategoriaService categoriaService;
    private final PedidoService pedidoService;
    private final UsuarioRepository usuarioRepository;

    @Value("${app.powerbi.public-embed-url:}")
    private String powerBiPublicEmbedUrl;

    @Value("${app.powerbi.embed-url:}")
    private String powerBiEmbedUrl;

    @Value("${app.powerbi.report-file:/powerbi/Taller.pbix}")
    private String powerBiReportFile;

    public AdminController(ProductoService productoService, CategoriaService categoriaService,
            PedidoService pedidoService, UsuarioRepository usuarioRepository) {
        this.productoService = productoService;
        this.categoriaService = categoriaService;
        this.pedidoService = pedidoService;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping({"/admin", "/admin/dashboard"})
    public String dashboard(Model model) {
        List<Producto> productos = productoService.listar();
        List<Pedido> pedidos = pedidoService.listarTodos();
        List<Producto> stockBajo = productoService.listarStockBajo(5).stream().sorted(Comparator.comparing(Producto::getStock)).limit(8).toList();
        Map<String, Long> categoriasResumen = productos.stream().collect(Collectors.groupingBy(
                p -> p.getCategoriaNombre() == null ? "Sin categoría" : p.getCategoriaNombre(), Collectors.counting()));
        model.addAttribute("totalProductos", productos.size());
        model.addAttribute("totalPedidos", pedidos.size());
        model.addAttribute("stockCritico", stockBajo.size());
        model.addAttribute("ventasAprobadas", pedidos.stream().filter(p -> p.getEstadoPago() == EstadoPago.APROBADO)
                .map(Pedido::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add));
        model.addAttribute("pedidosRecientes", pedidos.stream().limit(10).toList());
        model.addAttribute("stockBajo", stockBajo);
        model.addAttribute("categoriasResumen", categoriasResumen.entrySet().stream().sorted(Map.Entry.<String, Long>comparingByValue().reversed()).toList());
        return "admin/dashboard";
    }

    @GetMapping("/admin/productos")
    public String listarProductos(Model model) {
        model.addAttribute("productos", productoService.listar());
        return "admin/productos-lista";
    }

    @GetMapping("/admin/productos/nuevo")
    public String nuevoProducto(Model model) {
        model.addAttribute("producto", new Producto());
        model.addAttribute("categorias", categoriaService.listar());
        return "admin/producto_form";
    }

    @GetMapping("/admin/productos/editar/{id}")
    public String editarProducto(@PathVariable String id, Model model) {
        Producto producto = productoService.buscarPorId(id).orElseThrow();
        model.addAttribute("producto", producto);
        model.addAttribute("categorias", categoriaService.listar());
        return "admin/producto_form";
    }

    @PostMapping("/admin/productos/guardar")
    public String guardarProducto(Producto producto, RedirectAttributes redirectAttributes) {
        try { productoService.guardar(producto); redirectAttributes.addFlashAttribute("mensaje", "Producto guardado correctamente."); }
        catch (IllegalArgumentException e) { redirectAttributes.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/admin/productos";
    }

    @GetMapping("/admin/productos/eliminar/{id}")
    public String eliminarProducto(@PathVariable String id, RedirectAttributes redirectAttributes) {
        productoService.eliminar(id);
        redirectAttributes.addFlashAttribute("mensaje", "Producto eliminado correctamente.");
        return "redirect:/admin/productos";
    }

    @GetMapping("/admin/pedidos")
    public String listarPedidos(Model model) {
        model.addAttribute("pedidos", pedidoService.listarTodos());
        model.addAttribute("estados", EstadoPedido.values());
        return "admin/pedidos-lista";
    }

    @PostMapping("/admin/pedidos/{id}/actualizar")
    public String actualizarPedido(@PathVariable String id, EstadoPedido estado, String trackingCode,
            RedirectAttributes redirectAttributes) {
        pedidoService.actualizarSeguimiento(id, estado, trackingCode);
        redirectAttributes.addFlashAttribute("mensaje", "Pedido actualizado correctamente.");
        return "redirect:/admin/pedidos";
    }

    @GetMapping("/admin/reportes/stock")
    public String reporteStock(Model model) {
        model.addAttribute("productos", productoService.listarStockBajo(5));
        return "admin/reporte_stock";
    }

    @GetMapping("/admin/analitica")
    public String analiticaPowerBi(Model model) {
        String publicUrl = powerBiPublicEmbedUrl == null ? "" : powerBiPublicEmbedUrl.trim();
        String privateUrl = powerBiEmbedUrl == null ? "" : powerBiEmbedUrl.trim();
        String finalUrl = !publicUrl.isBlank() ? publicUrl : privateUrl;
        boolean publicEmbed = !publicUrl.isBlank();
        model.addAttribute("powerBiEmbedUrl", finalUrl);
        model.addAttribute("powerBiPublicEmbed", publicEmbed);
        model.addAttribute("powerBiNeedsLogin", !publicEmbed && !privateUrl.isBlank());
        model.addAttribute("powerBiReportFile", powerBiReportFile);
        return "admin/analitica_powerbi";
    }

    @GetMapping("/admin/base-datos")
    public String baseDatos(Model model) {
        model.addAttribute("totalProductos", productoService.listar().size());
        model.addAttribute("totalCategorias", categoriaService.listar().size());
        model.addAttribute("totalPedidos", pedidoService.listarTodos().size());
        model.addAttribute("totalUsuarios", contarUsuariosSeguro());
        return "admin/base_datos";
    }

    private long contarUsuariosSeguro() {
        try {
            return usuarioRepository.count();
        } catch (RuntimeException ex) {
            return 0L;
        }
    }


    @GetMapping("/admin/prediccion")
    public String prediccionVentas() {
        return "admin/prediccion";
    }
}
