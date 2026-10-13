package com.nextlevel.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.nextlevel.dto.MetodoPagoForm;
import com.nextlevel.model.TipoMetodoPago;
import com.nextlevel.model.Usuario;
import com.nextlevel.service.MetodoPagoService;
import com.nextlevel.service.UsuarioService;

import jakarta.validation.Valid;

@Controller
public class PagoController {

    private final UsuarioService usuarioService;
    private final MetodoPagoService metodoPagoService;

    public PagoController(UsuarioService usuarioService, MetodoPagoService metodoPagoService) {
        this.usuarioService = usuarioService;
        this.metodoPagoService = metodoPagoService;
    }

    @GetMapping("/perfil/pagos")
    public String verMetodosPago(Model model, Principal principal) {
        Usuario usuario = usuarioService.buscarPorCorreo(principal.getName()).orElseThrow();
        if (!model.containsAttribute("metodoPagoForm")) {
            model.addAttribute("metodoPagoForm", new MetodoPagoForm());
        }
        model.addAttribute("usuario", usuario);
        model.addAttribute("metodosPago", metodoPagoService.listarActivos(usuario));
        model.addAttribute("tiposMetodoPago", TipoMetodoPago.values());
        return "perfil_pagos";
    }

    @PostMapping("/perfil/pagos")
    public String registrarMetodoPago(@Valid @ModelAttribute("metodoPagoForm") MetodoPagoForm metodoPagoForm,
            BindingResult bindingResult,
            Principal principal,
            Model model,
            RedirectAttributes redirectAttributes) {

        Usuario usuario = usuarioService.buscarPorCorreo(principal.getName()).orElseThrow();

        if (bindingResult.hasErrors()) {
            model.addAttribute("usuario", usuario);
            model.addAttribute("metodosPago", metodoPagoService.listarActivos(usuario));
            model.addAttribute("tiposMetodoPago", TipoMetodoPago.values());
            return "perfil_pagos";
        }

        metodoPagoService.agregarMetodoPago(usuario, metodoPagoForm);
        redirectAttributes.addFlashAttribute("mensajePago", "Método de pago registrado correctamente.");
        return "redirect:/perfil/pagos";
    }

    @GetMapping("/perfil/pagos/eliminar/{id}")
    public String eliminarMetodo(@PathVariable String id, Principal principal, RedirectAttributes redirectAttributes) {
        Usuario usuario = usuarioService.buscarPorCorreo(principal.getName()).orElseThrow();
        metodoPagoService.eliminarMetodoPago(usuario, id);
        redirectAttributes.addFlashAttribute("mensajePago", "Método de pago eliminado correctamente.");
        return "redirect:/perfil/pagos";
    }
}
