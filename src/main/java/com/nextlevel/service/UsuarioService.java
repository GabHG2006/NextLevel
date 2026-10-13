package com.nextlevel.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.nextlevel.dto.RegistroUsuarioForm;
import com.nextlevel.model.Rol;
import com.nextlevel.model.Usuario;
import com.nextlevel.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.security.max-failed-attempts:5}")
    private int maxFailedAttempts;

    @Value("${app.security.lock-duration-minutes:15}")
    private int lockDurationMinutes;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario registrarCliente(RegistroUsuarioForm form) {
        String correoNormalizado = form.getCorreo().trim().toLowerCase();
        if (usuarioRepository.existsByCorreoIgnoreCase(correoNormalizado)) {
            throw new IllegalArgumentException("Ya existe un usuario registrado con ese correo.");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(form.getNombre().trim());
        usuario.setApellido(form.getApellido().trim());
        usuario.setCorreo(correoNormalizado);
        usuario.setTelefono(form.getTelefono());
        usuario.setDireccion(form.getDireccion());
        usuario.setPassword(passwordEncoder.encode(form.getPassword()));
        usuario.setRol(Rol.ROLE_CLIENTE);
        usuario.setEnabled(true);
        usuario.setAccountNonLocked(true);

        return usuarioRepository.save(usuario);
    }

    public Optional<Usuario> buscarPorCorreo(String correo) {
        return usuarioRepository.findByCorreoIgnoreCase(correo == null ? null : correo.trim().toLowerCase());
    }

    public Optional<Usuario> buscarPorId(String id) {
        return usuarioRepository.findById(id);
    }

    public Usuario guardar(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    public void registrarIntentoFallido(String correo) {
        if (correo == null || correo.isBlank()) {
            return;
        }

        usuarioRepository.findByCorreoIgnoreCase(correo.trim().toLowerCase()).ifPresent(usuario -> {
            if (!usuario.isAccountNonLocked() && !desbloquearSiAplica(usuario)) {
                return;
            }

            usuario.setFailedAttempts(usuario.getFailedAttempts() + 1);
            if (usuario.getFailedAttempts() >= maxFailedAttempts) {
                usuario.setAccountNonLocked(false);
                usuario.setLockTime(LocalDateTime.now());
            }
            usuarioRepository.save(usuario);
        });
    }

    public void resetearIntentos(String correo) {
        if (correo == null || correo.isBlank()) {
            return;
        }

        usuarioRepository.findByCorreoIgnoreCase(correo.trim().toLowerCase()).ifPresent(usuario -> {
            usuario.setFailedAttempts(0);
            usuario.setAccountNonLocked(true);
            usuario.setLockTime(null);
            usuarioRepository.save(usuario);
        });
    }

    public boolean desbloquearSiAplica(Usuario usuario) {
        if (usuario == null || usuario.isAccountNonLocked() || usuario.getLockTime() == null) {
            return false;
        }

        LocalDateTime desbloqueo = usuario.getLockTime().plusMinutes(lockDurationMinutes);
        if (LocalDateTime.now().isAfter(desbloqueo)) {
            usuario.setAccountNonLocked(true);
            usuario.setFailedAttempts(0);
            usuario.setLockTime(null);
            usuarioRepository.save(usuario);
            return true;
        }
        return false;
    }

    public boolean existeCorreo(String correo) {
        if (correo == null || correo.isBlank()) {
            return false;
        }
        return usuarioRepository.existsByCorreoIgnoreCase(correo.trim().toLowerCase());
    }

    public Usuario obtenerOCrearUsuarioOAuth(String correo, String nombreCompleto) {
        String correoNormalizado = correo == null ? null : correo.trim().toLowerCase();
        if (correoNormalizado == null || correoNormalizado.isBlank()) {
            throw new IllegalArgumentException("El correo de OAuth no puede estar vacío.");
        }

        return usuarioRepository.findByCorreoIgnoreCase(correoNormalizado)
                .map(usuario -> actualizarDatosOAuthSiAplica(usuario, nombreCompleto))
                .orElseGet(() -> crearUsuarioOAuth(correoNormalizado, nombreCompleto));
    }

    private Usuario crearUsuarioOAuth(String correo, String nombreCompleto) {
        Usuario usuario = new Usuario();
        aplicarNombreOAuth(usuario, nombreCompleto, correo);
        usuario.setCorreo(correo);
        usuario.setPassword(passwordEncoder.encode("OAUTH2_USER_" + System.nanoTime()));
        usuario.setRol(Rol.ROLE_CLIENTE);
        usuario.setEnabled(true);
        usuario.setAccountNonLocked(true);
        return usuarioRepository.save(usuario);
    }

    private Usuario actualizarDatosOAuthSiAplica(Usuario usuario, String nombreCompleto) {
        boolean incompleto = estaVacio(usuario.getNombre()) || estaVacio(usuario.getApellido());
        if (incompleto && !estaVacio(nombreCompleto)) {
            aplicarNombreOAuth(usuario, nombreCompleto, usuario.getCorreo());
            return usuarioRepository.save(usuario);
        }
        return usuario;
    }

    private void aplicarNombreOAuth(Usuario usuario, String nombreCompleto, String correo) {
        String nombreBase = estaVacio(nombreCompleto) ? correo.split("@")[0] : nombreCompleto.trim();
        String[] partes = nombreBase.split("\\s+", 2);
        usuario.setNombre(partes.length > 0 && !partes[0].isBlank() ? partes[0] : "Cliente");
        usuario.setApellido(partes.length > 1 && !partes[1].isBlank() ? partes[1] : "Google");
    }

    private boolean estaVacio(String valor) {
        return valor == null || valor.isBlank();
    }

    public int getMaxFailedAttempts() {
        return maxFailedAttempts;
    }

    public int getLockDurationMinutes() {
        return lockDurationMinutes;
    }
}
