package com.nextlevel.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.nextlevel.dto.MetodoPagoForm;
import com.nextlevel.model.MetodoPago;
import com.nextlevel.model.Usuario;
import com.nextlevel.repository.UsuarioRepository;

@Service
public class MetodoPagoService {

    private final UsuarioRepository usuarioRepository;

    public MetodoPagoService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public List<MetodoPago> listarActivos(Usuario usuario) {
        if (usuario == null) {
            return List.of();
        }
        return usuario.getMetodosPago().stream()
                .filter(MetodoPago::isActivo)
                .toList();
    }

    public Usuario agregarMetodoPago(Usuario usuario, MetodoPagoForm form) {
        if (usuario == null) {
            throw new IllegalArgumentException("Debes iniciar sesión para registrar un método de pago.");
        }
        if (form == null) {
            throw new IllegalArgumentException("Debes diligenciar la información del método de pago.");
        }

        MetodoPago metodoPago = new MetodoPago();
        metodoPago.setId(UUID.randomUUID().toString());
        metodoPago.setTipo(form.getTipo());
        metodoPago.setEntidad(form.getEntidad());
        metodoPago.setTitular(form.getTitular());
        metodoPago.setReferencia(form.getReferencia());
        metodoPago.setSaldoDisponible(form.getSaldoDisponible());
        metodoPago.setActivo(true);

        if (usuario.getMetodosPago() == null) {
            usuario.setMetodosPago(new ArrayList<>());
        }
        usuario.getMetodosPago().add(metodoPago);
        return usuarioRepository.save(usuario);
    }

    public void eliminarMetodoPago(Usuario usuario, String metodoPagoId) {
        if (usuario == null || metodoPagoId == null || metodoPagoId.isBlank()) {
            return;
        }
        usuario.getMetodosPago().removeIf(metodo -> Objects.equals(metodo.getId(), metodoPagoId));
        usuarioRepository.save(usuario);
    }

    public MetodoPago obtenerMetodoActivo(Usuario usuario, String metodoPagoId) {
        if (usuario == null) {
            throw new IllegalArgumentException("Debes iniciar sesión para usar un método de pago.");
        }
        if (metodoPagoId == null || metodoPagoId.isBlank()) {
            throw new IllegalArgumentException("Debes seleccionar un método de pago.");
        }

        return usuario.getMetodosPago().stream()
                .filter(MetodoPago::isActivo)
                .filter(metodo -> metodoPagoId.equals(metodo.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("El método de pago seleccionado no existe o no está disponible."));
    }

    public MetodoPago debitarSaldo(Usuario usuario, String metodoPagoId, BigDecimal valor) {
        MetodoPago metodoPago = obtenerMetodoActivo(usuario, metodoPagoId);
        BigDecimal total = valor == null ? BigDecimal.ZERO : valor;

        if (total.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El total a pagar no es válido.");
        }

        if (metodoPago.getSaldoDisponible().compareTo(total) < 0) {
            throw new IllegalArgumentException("Fondos insuficientes en el método de pago seleccionado.");
        }

        metodoPago.setSaldoDisponible(metodoPago.getSaldoDisponible().subtract(total));
        usuarioRepository.save(usuario);
        return metodoPago;
    }

    public void acreditarSaldo(Usuario usuario, String metodoPagoId, BigDecimal valor) {
        if (usuario == null || metodoPagoId == null || metodoPagoId.isBlank()) {
            return;
        }

        BigDecimal monto = valor == null ? BigDecimal.ZERO : valor;
        if (monto.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        for (MetodoPago metodoPago : usuario.getMetodosPago()) {
            if (metodoPagoId.equals(metodoPago.getId())) {
                metodoPago.setSaldoDisponible(metodoPago.getSaldoDisponible().add(monto));
                usuarioRepository.save(usuario);
                return;
            }
        }
    }
}
