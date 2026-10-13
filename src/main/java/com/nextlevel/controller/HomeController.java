package com.nextlevel.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String inicio() { return "index"; }

    @GetMapping("/403")
    public String accesoDenegado() { return "error/403"; }

    @GetMapping("/404")
    public String noEncontrado() { return "error/404"; }

    @GetMapping("/productos")
    public String productosLegacy() { return "redirect:/catalogo"; }

    @GetMapping("/admin/login")
    public String adminLoginLegacy() { return "redirect:/login"; }

    @GetMapping("/auth/login")
    public String loginLegacy() { return "redirect:/login"; }

    @GetMapping("/auth/logout")
    public String logoutLegacy() { return "redirect:/logout"; }
}
