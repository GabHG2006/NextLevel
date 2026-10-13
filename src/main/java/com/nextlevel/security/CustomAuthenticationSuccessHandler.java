package com.nextlevel.security;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.nextlevel.service.UsuarioService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UsuarioService usuarioService;
    private final JwtService jwtService;
    private final JwtCookieService jwtCookieService;

    public CustomAuthenticationSuccessHandler(UsuarioService usuarioService,
            JwtService jwtService,
            JwtCookieService jwtCookieService) {
        this.usuarioService = usuarioService;
        this.jwtService = jwtService;
        this.jwtCookieService = jwtCookieService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        usuarioService.resetearIntentos(authentication.getName());

        if (authentication.getPrincipal() instanceof UserDetails userDetails) {
            String token = jwtService.generateToken(userDetails);
            jwtCookieService.addJwtCookie(response, token);
        }

        String redirect = request.getParameter("redirect");
        if (isSafeRedirect(redirect)) {
            getRedirectStrategy().sendRedirect(request, response, redirect);
            return;
        }
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        setDefaultTargetUrl(isAdmin ? "/admin/dashboard" : "/catalogo");
        super.onAuthenticationSuccess(request, response, authentication);
    }

    private boolean isSafeRedirect(String redirect) {
        return redirect != null && !redirect.isBlank() && redirect.startsWith("/") && !redirect.startsWith("//");
    }
}
