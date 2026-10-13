package com.nextlevel.controller;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.nextlevel.dto.RegistroUsuarioForm;
import com.nextlevel.model.Usuario;
import com.nextlevel.security.JwtCookieService;
import com.nextlevel.security.JwtService;
import com.nextlevel.security.SecurityUser;
import com.nextlevel.service.UsuarioService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@Controller
public class AuthController {

    private final UsuarioService usuarioService;
    private final JwtService jwtService;
    private final JwtCookieService jwtCookieService;

    public AuthController(UsuarioService usuarioService,
            JwtService jwtService,
            JwtCookieService jwtCookieService) {
        this.usuarioService = usuarioService;
        this.jwtService = jwtService;
        this.jwtCookieService = jwtCookieService;
    }

    @GetMapping("/login")
    public String login() { return "login"; }

    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        model.addAttribute("registroUsuarioForm", new RegistroUsuarioForm());
        return "registro";
    }

    @PostMapping("/registro")
    public String registrar(@Valid @ModelAttribute("registroUsuarioForm") RegistroUsuarioForm form,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            HttpServletResponse response,
            @RequestParam(required = false) String redirect) {
        if (!result.hasFieldErrors("password") && !result.hasFieldErrors("confirmPassword")
                && !form.getPassword().equals(form.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "password.mismatch", "Las contraseñas no coinciden.");
        }
        if (result.hasErrors()) return "registro";
        try {
            Usuario usuario = usuarioService.registrarCliente(form);
            autenticarYCrearJwt(usuario, response);
        } catch (IllegalArgumentException e) {
            result.rejectValue("correo", "registro.error", e.getMessage());
            return "registro";
        }

        redirectAttributes.addFlashAttribute("mensaje", "Cuenta creada correctamente. Ya estás dentro de NextLevel.");
        if (isSafeRedirect(redirect)) {
            return "redirect:" + redirect;
        }
        return "redirect:/catalogo?registered";
    }

    private void autenticarYCrearJwt(Usuario usuario, HttpServletResponse response) {
        SecurityUser userDetails = new SecurityUser(usuario);
        String token = jwtService.generateToken(userDetails);
        jwtCookieService.addJwtCookie(response, token);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private boolean isSafeRedirect(String redirect) {
        return redirect != null && !redirect.isBlank() && redirect.startsWith("/") && !redirect.startsWith("//");
    }
}
