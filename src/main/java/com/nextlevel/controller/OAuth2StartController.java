package com.nextlevel.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class OAuth2StartController {

    public static final String OAUTH2_FLOW_ATTRIBUTE = "NEXTLEVEL_OAUTH2_FLOW";
    public static final String OAUTH2_REDIRECT_ATTRIBUTE = "NEXTLEVEL_OAUTH2_REDIRECT";

    @GetMapping("/auth/oauth2/google/{flow}")
    public String iniciarGoogleOAuth(@PathVariable String flow,
            @RequestParam(required = false) String redirect,
            HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        session.setAttribute(OAUTH2_FLOW_ATTRIBUTE, normalizarFlujo(flow));
        if (isSafeRedirect(redirect)) {
            session.setAttribute(OAUTH2_REDIRECT_ATTRIBUTE, redirect);
        } else {
            session.removeAttribute(OAUTH2_REDIRECT_ATTRIBUTE);
        }
        return "redirect:/oauth2/authorization/google";
    }

    private String normalizarFlujo(String flow) {
        return "registro".equalsIgnoreCase(flow) ? "REGISTRO" : "LOGIN";
    }

    private boolean isSafeRedirect(String redirect) {
        return redirect != null && !redirect.isBlank() && redirect.startsWith("/") && !redirect.startsWith("//");
    }
}
