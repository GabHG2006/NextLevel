package com.nextlevel.controller.api;

import java.util.List;
import java.util.Map;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nextlevel.dto.LoginRequest;
import com.nextlevel.dto.LoginResponse;
import com.nextlevel.dto.RegistroUsuarioForm;
import com.nextlevel.model.Usuario;
import com.nextlevel.security.JwtCookieService;
import com.nextlevel.security.JwtService;
import com.nextlevel.security.SecurityUser;
import com.nextlevel.service.UsuarioService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtCookieService jwtCookieService;
    private final UsuarioService usuarioService;

    public AuthApiController(AuthenticationManager authenticationManager,
            JwtService jwtService,
            JwtCookieService jwtCookieService,
            UsuarioService usuarioService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.jwtCookieService = jwtCookieService;
        this.usuarioService = usuarioService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getCorreo(), request.getPassword()));

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(userDetails);
        jwtCookieService.addJwtCookie(response, token);

        return construirRespuesta(token, userDetails);
    }

    @PostMapping("/registro")
    public LoginResponse registrar(@Valid @RequestBody RegistroUsuarioForm form, HttpServletResponse response) {
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            throw new IllegalArgumentException("Las contraseñas no coinciden.");
        }
        Usuario usuario = usuarioService.registrarCliente(form);
        UserDetails userDetails = new SecurityUser(usuario);
        String token = jwtService.generateToken(userDetails);
        jwtCookieService.addJwtCookie(response, token);
        return construirRespuesta(token, userDetails);
    }

    @GetMapping("/email-exists")
    public Map<String, Object> existeCorreo(@RequestParam String correo) {
        return Map.of(
                "correo", correo == null ? "" : correo.trim().toLowerCase(),
                "exists", usuarioService.existeCorreo(correo));
    }

    @GetMapping("/me")
    public Map<String, Object> me(Authentication authentication) {
        return Map.of(
                "correo", authentication.getName(),
                "roles", authentication.getAuthorities());
    }

    private LoginResponse construirRespuesta(String token, UserDetails userDetails) {
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        return new LoginResponse(token, userDetails.getUsername(), roles);
    }
}
