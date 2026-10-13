package com.nextlevel.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.nextlevel.controller.OAuth2StartController;
import com.nextlevel.model.Usuario;
import com.nextlevel.service.UsuarioService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final JwtCookieService jwtCookieService;
    private final UsuarioService usuarioService;

    public OAuth2AuthenticationSuccessHandler(JwtService jwtService,
            JwtCookieService jwtCookieService,
            UsuarioService usuarioService) {
        this.jwtService = jwtService;
        this.jwtCookieService = jwtCookieService;
        this.usuarioService = usuarioService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String nombreCompleto = oAuth2User.getAttribute("name");

        if (email == null || email.isBlank()) {
            response.sendRedirect("/login?oauth_error=email");
            return;
        }

        String correoNormalizado = email.trim().toLowerCase();
        OAuth2FlowData flowData = leerYLimpiarFlujo(request);
        boolean usuarioExistia = usuarioService.existeCorreo(correoNormalizado);

        Usuario usuario = usuarioService.obtenerOCrearUsuarioOAuth(correoNormalizado, nombreCompleto);
        SecurityUser userDetails = new SecurityUser(usuario);
        String token = jwtService.generateToken(userDetails);
        jwtCookieService.addJwtCookie(response, token);

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        response.sendRedirect(resolverDestino(flowData, userDetails, usuarioExistia));
    }

    private OAuth2FlowData leerYLimpiarFlujo(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return new OAuth2FlowData("LOGIN", null);
        }

        Object flow = session.getAttribute(OAuth2StartController.OAUTH2_FLOW_ATTRIBUTE);
        Object redirect = session.getAttribute(OAuth2StartController.OAUTH2_REDIRECT_ATTRIBUTE);
        session.removeAttribute(OAuth2StartController.OAUTH2_FLOW_ATTRIBUTE);
        session.removeAttribute(OAuth2StartController.OAUTH2_REDIRECT_ATTRIBUTE);

        String flowValue = flow == null ? "LOGIN" : flow.toString();
        String redirectValue = redirect == null ? null : redirect.toString();
        return new OAuth2FlowData(flowValue, isSafeRedirect(redirectValue) ? redirectValue : null);
    }

    private String resolverDestino(OAuth2FlowData flowData, SecurityUser userDetails, boolean usuarioExistia) {
        if (flowData.redirect() != null) {
            return flowData.redirect();
        }

        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) {
            return "/admin/dashboard";
        }

        if ("REGISTRO".equalsIgnoreCase(flowData.flow())) {
            return usuarioExistia ? "/catalogo?oauth=existing" : "/catalogo?oauth=registered";
        }
        return usuarioExistia ? "/catalogo?oauth=login" : "/catalogo?oauth=registered";
    }

    private boolean isSafeRedirect(String redirect) {
        return redirect != null && !redirect.isBlank() && redirect.startsWith("/") && !redirect.startsWith("//");
    }

    private record OAuth2FlowData(String flow, String redirect) {}
}
