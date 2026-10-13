package com.nextlevel.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.nextlevel.model.CarritoItem;
import com.nextlevel.model.MetodoPago;
import com.nextlevel.model.Pedido;
import com.nextlevel.model.Usuario;

@Service
public class CheckoutService {

    private final MetodoPagoService metodoPagoService;
    private final PedidoService pedidoService;

    public CheckoutService(MetodoPagoService metodoPagoService, PedidoService pedidoService) {
        this.metodoPagoService = metodoPagoService;
        this.pedidoService = pedidoService;
    }

    public Pedido procesarCheckout(Usuario usuario, String direccionEnvio, String ciudadEnvio,
            String departamentoEnvio, String metodoPagoId, List<CarritoItem> carrito, BigDecimal costoEnvio) {
        if (usuario == null) {
            throw new IllegalArgumentException("Debes iniciar sesión para completar el pago.");
        }
        if (direccionEnvio == null || direccionEnvio.isBlank()) {
            throw new IllegalArgumentException("Debes indicar una dirección de envío.");
        }
        if (carrito == null || carrito.isEmpty()) {
            throw new IllegalArgumentException("El carrito está vacío.");
        }

        BigDecimal subtotal = carrito.stream()
                .map(CarritoItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal envio = costoEnvio == null ? BigDecimal.ZERO : costoEnvio;
        BigDecimal totalAPagar = subtotal.add(envio);

        MetodoPago metodoPago = metodoPagoService.debitarSaldo(usuario, metodoPagoId, totalAPagar);
        try {
            return pedidoService.crearPedidoPagado(usuario, direccionEnvio.trim(), ciudadEnvio, departamentoEnvio,
                    carrito, metodoPago, envio);
        } catch (RuntimeException ex) {
            metodoPagoService.acreditarSaldo(usuario, metodoPagoId, totalAPagar);
            throw ex;
        }
    }

    public Pedido procesarCheckout(Usuario usuario, String direccionEnvio, String metodoPagoId, List<CarritoItem> carrito) {
        return procesarCheckout(usuario, direccionEnvio, null, null, metodoPagoId, carrito, BigDecimal.ZERO);
    }
}
