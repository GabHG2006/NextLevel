package com.nextlevel.controller;

import java.math.BigDecimal;
import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.nextlevel.dto.CheckoutDraft;
import com.nextlevel.model.Pedido;
import com.nextlevel.model.Usuario;
import com.nextlevel.service.CarritoService;
import com.nextlevel.service.CheckoutService;
import com.nextlevel.service.MetodoPagoService;
import com.nextlevel.service.PedidoService;
import com.nextlevel.service.UsuarioService;

import jakarta.servlet.http.HttpSession;

@Controller
public class PedidoController {

    private static final String CHECKOUT_DRAFT_KEY = "CHECKOUT_DRAFT";

    private final PedidoService pedidoService;
    private final CarritoService carritoService;
    private final UsuarioService usuarioService;
    private final MetodoPagoService metodoPagoService;
    private final CheckoutService checkoutService;

    public PedidoController(PedidoService pedidoService, CarritoService carritoService,
            UsuarioService usuarioService, MetodoPagoService metodoPagoService,
            CheckoutService checkoutService) {
        this.pedidoService = pedidoService;
        this.carritoService = carritoService;
        this.usuarioService = usuarioService;
        this.metodoPagoService = metodoPagoService;
        this.checkoutService = checkoutService;
    }

    @GetMapping("/pedido/detalle")
    public String confirmarPedido(Model model, HttpSession session, Principal principal) {
        if (carritoService.estaVacio(session)) return "redirect:/carrito";
        CheckoutDraft checkoutDraft = obtenerCheckoutDraft(session);
        completarConUbicacionSesion(checkoutDraft, session);
        if (principal != null) {
            Usuario usuario = usuarioService.buscarPorCorreo(principal.getName()).orElseThrow();
            completarConDatosUsuario(checkoutDraft, usuario);
            model.addAttribute("usuario", usuario);
            model.addAttribute("metodosPago", metodoPagoService.listarActivos(usuario));
        }
        cargarResumenCheckout(model, session, checkoutDraft, principal != null);
        return "pedido_detalle";
    }

    @PostMapping("/pedido/continuar")
    public String continuarCheckout(@ModelAttribute CheckoutDraft checkoutDraft,
            HttpSession session,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        if (carritoService.estaVacio(session)) return "redirect:/carrito";
        normalizarCheckout(checkoutDraft);
        session.setAttribute(CHECKOUT_DRAFT_KEY, checkoutDraft);
        if (!datosBasicosCompletos(checkoutDraft)) {
            redirectAttributes.addFlashAttribute("errorPedido", "Completa nombre, apellido, correo, celular, dirección y acepta las condiciones para continuar.");
            return "redirect:/pedido/detalle";
        }
        if (principal == null) return "redirect:/login?redirect=/pedido/detalle&checkout=1";
        redirectAttributes.addFlashAttribute("mensajePedido", "Tus datos quedaron guardados. Ahora elige el método de pago.");
        return "redirect:/pedido/detalle";
    }

    @PostMapping("/pedido/realizar")
    public String realizarPedido(@ModelAttribute CheckoutDraft checkoutDraft,
            @RequestParam(required = false) String metodoPagoId,
            HttpSession session,
            Principal principal,
            RedirectAttributes redirectAttributes,
            Model model) {
        normalizarCheckout(checkoutDraft);
        session.setAttribute(CHECKOUT_DRAFT_KEY, checkoutDraft);
        if (!checkoutDraft.isAceptaTerminos() || !checkoutDraft.isAceptaPrivacidad()) {
            redirectAttributes.addFlashAttribute("errorPedido", "Debes aceptar términos, privacidad y tratamiento de datos para pagar.");
            return "redirect:/pedido/detalle";
        }
        try {
            Usuario usuario = usuarioService.buscarPorCorreo(principal.getName()).orElseThrow();
            BigDecimal envioEstimado = calcularEnvio(checkoutDraft, session);
            Pedido pedido = checkoutService.procesarCheckout(usuario, resolverDireccionEnvio(checkoutDraft, usuario),
                    checkoutDraft.getCiudad(), checkoutDraft.getDepartamento(), metodoPagoId,
                    carritoService.obtenerCarrito(session), envioEstimado);
            carritoService.limpiar(session);
            session.removeAttribute(CHECKOUT_DRAFT_KEY);
            model.addAttribute("pedido", pedido);
            model.addAttribute("entregaSeleccionada", checkoutDraft.getTipoEntrega());
            return "pedido_confirmado";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorPedido", e.getMessage());
            return "redirect:/pedido/detalle";
        }
    }

    @GetMapping("/pedido/mis-pedidos")
    public String misPedidos(Model model, Principal principal) {
        Usuario usuario = usuarioService.buscarPorCorreo(principal.getName()).orElseThrow();
        model.addAttribute("pedidos", pedidoService.listarPorUsuario(usuario.getId()));
        return "mis_pedidos";
    }

    private void cargarResumenCheckout(Model model, HttpSession session, CheckoutDraft checkoutDraft, boolean autenticado) {
        BigDecimal subtotal = carritoService.calcularTotal(session);
        BigDecimal envio = calcularEnvio(checkoutDraft, session);
        model.addAttribute("carrito", carritoService.obtenerCarrito(session));
        model.addAttribute("total", subtotal);
        model.addAttribute("envioEstimado", envio);
        model.addAttribute("totalVisual", subtotal.add(envio));
        model.addAttribute("checkoutDraft", checkoutDraft);
        model.addAttribute("requiereLoginPago", !autenticado);
    }

    private CheckoutDraft obtenerCheckoutDraft(HttpSession session) {
        Object o = session.getAttribute(CHECKOUT_DRAFT_KEY);
        if (o instanceof CheckoutDraft draft) return draft;
        CheckoutDraft nuevo = new CheckoutDraft();
        session.setAttribute(CHECKOUT_DRAFT_KEY, nuevo);
        return nuevo;
    }

    private void completarConDatosUsuario(CheckoutDraft d, Usuario u) {
        if (u == null) return;
        if (estaVacio(d.getCorreo())) d.setCorreo(u.getCorreo());
        if (estaVacio(d.getNombre())) d.setNombre(u.getNombre());
        if (estaVacio(d.getApellido())) d.setApellido(u.getApellido());
        if (estaVacio(d.getCelular())) d.setCelular(u.getTelefono());
        if (estaVacio(d.getDireccion())) d.setDireccion(u.getDireccion());
    }

    private void completarConUbicacionSesion(CheckoutDraft d, HttpSession session) {
        Object ciudad = session.getAttribute("TIENDA_CIUDAD");
        if (estaVacio(d.getCiudad()) && ciudad != null && !ciudad.toString().isBlank()) {
            d.setCiudad(ciudad.toString());
        }
    }

    private boolean datosBasicosCompletos(CheckoutDraft d) {
        return !estaVacio(d.getNombre()) && !estaVacio(d.getApellido()) && !estaVacio(d.getCorreo())
                && !estaVacio(d.getCelular())
                && ("RETIRO".equalsIgnoreCase(d.getTipoEntrega()) || !estaVacio(d.getDireccion()))
                && d.isAceptaTerminos() && d.isAceptaPrivacidad();
    }

    private void normalizarCheckout(CheckoutDraft d) {
        d.setCorreo(limpiar(d.getCorreo()));
        d.setNombre(limpiar(d.getNombre()));
        d.setApellido(limpiar(d.getApellido()));
        d.setDocumento(limpiar(d.getDocumento()));
        d.setCelular(limpiar(d.getCelular()));
        d.setDireccion(limpiar(d.getDireccion()));
        d.setCiudad(limpiar(d.getCiudad()));
        d.setDepartamento(limpiar(d.getDepartamento()));
        d.setTipoEntrega(estaVacio(d.getTipoEntrega()) ? "DOMICILIO" : d.getTipoEntrega());
    }

    private String resolverDireccionEnvio(CheckoutDraft d, Usuario u) {
        String ubicacion = unirUbicacion(d.getCiudad(), d.getDepartamento());
        if ("RETIRO".equalsIgnoreCase(d.getTipoEntrega())) return "Recoger en tienda - " + ubicacion;
        if (!estaVacio(d.getDireccion())) return d.getDireccion() + ", " + ubicacion;
        if (u != null && !estaVacio(u.getDireccion())) return u.getDireccion().trim() + ", " + ubicacion;
        throw new IllegalArgumentException("Debes indicar la dirección de entrega.");
    }

    private BigDecimal calcularEnvio(CheckoutDraft checkoutDraft, HttpSession session) {
        return "RETIRO".equalsIgnoreCase(checkoutDraft.getTipoEntrega()) ? BigDecimal.ZERO : carritoService.calcularEnvioEstimado(session);
    }

    private String unirUbicacion(String ciudad, String departamento) {
        if (estaVacio(departamento)) return ciudad;
        if (estaVacio(ciudad)) return departamento;
        return ciudad + ", " + departamento;
    }

    private boolean estaVacio(String v) { return v == null || v.isBlank(); }
    private String limpiar(String v) { return v == null ? null : v.trim(); }
}
