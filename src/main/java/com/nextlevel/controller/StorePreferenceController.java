package com.nextlevel.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;

@Controller
public class StorePreferenceController {

    private static final String SESSION_LOCATION = "TIENDA_UBICACION";
    private static final String SESSION_CITY = "TIENDA_CIUDAD";
    private static final String SESSION_DEPARTMENT = "TIENDA_DEPARTAMENTO";

    @PostMapping("/ubicacion")
    public String guardarUbicacion(
            @RequestParam(required = false) String ciudad,
            @RequestParam(required = false) String departamento,
            @RequestParam(required = false, defaultValue = "/catalogo") String redirect,
            HttpSession session) {
        String ciudadLimpia = limpiar(ciudad);
        String departamentoLimpio = limpiar(departamento);

        if (ciudadLimpia == null && departamentoLimpio == null) {
            session.removeAttribute(SESSION_LOCATION);
            session.removeAttribute(SESSION_CITY);
            session.removeAttribute(SESSION_DEPARTMENT);
        } else {
            String valorUbicacion = ciudadLimpia != null && departamentoLimpio != null
                    ? ciudadLimpia + ", " + departamentoLimpio
                    : (ciudadLimpia != null ? ciudadLimpia : departamentoLimpio);
            session.setAttribute(SESSION_LOCATION, valorUbicacion);
            session.setAttribute(SESSION_CITY, ciudadLimpia);
            session.setAttribute(SESSION_DEPARTMENT, departamentoLimpio);
        }

        if (redirect == null || redirect.isBlank() || !redirect.startsWith("/")) {
            redirect = "/catalogo";
        }
        return "redirect:" + redirect;
    }

    private String limpiar(String value) {
        if (value == null) return null;
        String salida = value.trim();
        return salida.isBlank() ? null : salida;
    }
}
