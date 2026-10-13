package com.nextlevel.security;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.stereotype.Component;

import com.nextlevel.model.Usuario;
import com.nextlevel.service.UsuarioService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final UsuarioService usuarioService;

    public CustomAuthenticationFailureHandler(UsuarioService usuarioService) { this.usuarioService = usuarioService; }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {
        String correo = request.getParameter("correo");
        boolean sesionMaxima = exception instanceof SessionAuthenticationException;
        Usuario usuario = usuarioService.buscarPorCorreo(correo).orElse(null);

        if (!(exception instanceof SessionAuthenticationException) && usuario != null) {
            usuarioService.registrarIntentoFallido(correo);
            usuario = usuarioService.buscarPorCorreo(correo).orElse(null);
        }

        boolean correoNoExiste = correo != null && !correo.isBlank() && usuario == null;
        boolean bloqueado = exception instanceof LockedException || (usuario != null && !usuario.isAccountNonLocked());
        String redirect = request.getParameter("redirect");
        String suffix = isSafeRedirect(redirect) ? "&redirect=" + URLEncoder.encode(redirect, StandardCharsets.UTF_8) : "";

        String failureUrl;
        if (sesionMaxima) {
            failureUrl = "/login?expired";
        } else if (correoNoExiste) {
            failureUrl = "/login?notfound";
        } else if (bloqueado) {
            failureUrl = "/login?blocked";
        } else {
            failureUrl = "/login?error";
        }

        setDefaultFailureUrl(failureUrl + suffix);
        super.onAuthenticationFailure(request, response, exception);
    }

    private boolean isSafeRedirect(String redirect) {
        return redirect != null && !redirect.isBlank() && redirect.startsWith("/") && !redirect.startsWith("//");
    }
}
