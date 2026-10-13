package com.nextlevel.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ErrorPageController {

    @GetMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object statusObject = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int status = statusObject == null ? 500 : Integer.parseInt(statusObject.toString());
        prepararModeloError(status, model);
        return "error/inteligente";
    }

    @GetMapping({"/403", "/error/403"})
    public String forbidden(Model model) {
        prepararModeloError(403, model);
        return "error/inteligente";
    }

    @GetMapping({"/404", "/error/404"})
    public String notFound(Model model) {
        prepararModeloError(404, model);
        return "error/inteligente";
    }

    @GetMapping("/ups")
    public String ups(@RequestParam(defaultValue = "500") int status, Model model) {
        prepararModeloError(status, model);
        return "error/inteligente";
    }

    private void prepararModeloError(int status, Model model) {
        model.addAttribute("status", status);
        model.addAttribute("titulo", "Ups, lo siento");

        if (status == 403) {
            model.addAttribute("mensaje", "No tienes permisos para entrar a esta sección.");
            model.addAttribute("accion", "Volver a la tienda");
            model.addAttribute("url", "/catalogo");
        } else if (status == 404) {
            model.addAttribute("mensaje", "La página que buscas no existe o fue movida.");
            model.addAttribute("accion", "Ir al catálogo");
            model.addAttribute("url", "/catalogo");
        } else {
            model.addAttribute("mensaje", "No pudimos completar la solicitud. Revisa los datos e inténtalo nuevamente.");
            model.addAttribute("accion", "Volver al inicio");
            model.addAttribute("url", "/catalogo");
        }
    }
}
