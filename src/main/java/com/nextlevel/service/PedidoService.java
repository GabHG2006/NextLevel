package com.nextlevel.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.nextlevel.model.CarritoItem;
import com.nextlevel.model.EstadoPago;
import com.nextlevel.model.EstadoPedido;
import com.nextlevel.model.ItemPedido;
import com.nextlevel.model.MetodoPago;
import com.nextlevel.model.Pedido;
import com.nextlevel.model.Producto;
import com.nextlevel.model.Usuario;
import com.nextlevel.repository.PedidoRepository;

@Service
public class PedidoService {

    private static final Logger log = LoggerFactory.getLogger(PedidoService.class);
    private static final DateTimeFormatter PAYMENT_REF_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final PedidoRepository pedidoRepository;
    private final ProductoService productoService;

    public PedidoService(PedidoRepository pedidoRepository, ProductoService productoService) {
        this.pedidoRepository = pedidoRepository;
        this.productoService = productoService;
    }

    public Pedido crearPedido(Usuario usuario, String direccionEnvio, List<CarritoItem> carrito) {
        return crearPedidoPagado(usuario, direccionEnvio, null, null, carrito, null, BigDecimal.ZERO);
    }

    public Pedido crearPedidoPagado(Usuario usuario, String direccionEnvio, List<CarritoItem> carrito, MetodoPago metodoPago) {
        return crearPedidoPagado(usuario, direccionEnvio, null, null, carrito, metodoPago, BigDecimal.ZERO);
    }

    public Pedido crearPedidoPagado(Usuario usuario, String direccionEnvio, String ciudadEnvio,
            String departamentoEnvio, List<CarritoItem> carrito, MetodoPago metodoPago, BigDecimal costoEnvio) {
        if (usuario == null) {
            throw new IllegalArgumentException("Debes iniciar sesión para crear un pedido.");
        }

        if (carrito == null || carrito.isEmpty()) {
            throw new IllegalArgumentException("El carrito está vacío.");
        }

        if (direccionEnvio == null || direccionEnvio.isBlank()) {
            throw new IllegalArgumentException("Debes indicar la dirección de envío.");
        }

        List<ItemPedido> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        Map<String, Integer> stockOriginal = new LinkedHashMap<>();

        for (CarritoItem item : carrito) {
            Producto producto = productoService.buscarPorId(item.getProductoId())
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + item.getNombre()));
            if (producto.getStock() < item.getCantidad()) {
                throw new IllegalArgumentException("Stock insuficiente para " + producto.getNombre());
            }

            stockOriginal.put(producto.getId(), producto.getStock());

            ItemPedido itemPedido = new ItemPedido();
            itemPedido.setProductoId(item.getProductoId());
            itemPedido.setNombreProducto(item.getNombre());
            itemPedido.setCategoriaNombre(item.getCategoriaNombre());
            itemPedido.setPrecioUnitario(item.getPrecio());
            itemPedido.setCantidad(item.getCantidad());
            itemPedido.setSubtotal(item.getSubtotal());
            itemPedido.setImagen(item.getImagen());
            items.add(itemPedido);
            total = total.add(item.getSubtotal());
        }

        Pedido pedido = new Pedido();
        pedido.setUsuarioId(usuario.getId());
        pedido.setCorreoCliente(usuario.getCorreo());
        pedido.setNombreCliente(usuario.getNombreCompleto().trim());
        BigDecimal envioSeguro = costoEnvio == null ? BigDecimal.ZERO : costoEnvio;
        pedido.setDireccionEnvio(direccionEnvio.trim());
        pedido.setCiudadEnvio(ciudadEnvio == null ? null : ciudadEnvio.trim());
        pedido.setDepartamentoEnvio(departamentoEnvio == null ? null : departamentoEnvio.trim());
        pedido.setSubtotalProductos(total);
        pedido.setCostoEnvio(envioSeguro);
        pedido.setItems(items);
        pedido.setTotal(total.add(envioSeguro));
        pedido.setFechaCreacion(LocalDateTime.now());
        pedido.setFechaActualizacion(LocalDateTime.now());

        if (metodoPago != null) {
            pedido.setEstado(EstadoPedido.PAGADO);
            pedido.setEstadoPago(EstadoPago.APROBADO);
            pedido.setMetodoPagoTipo(metodoPago.getTipo() == null ? null : metodoPago.getTipo().name());
            pedido.setMetodoPagoEntidad(metodoPago.getEntidad());
            pedido.setReferenciaPago(generarReferenciaPago());
            pedido.setFechaPago(LocalDateTime.now());
        } else {
            pedido.setEstado(EstadoPedido.PENDIENTE);
            pedido.setEstadoPago(EstadoPago.PENDIENTE);
        }

        try {
            for (CarritoItem item : carrito) {
                productoService.descontarStock(item.getProductoId(), item.getCantidad());
            }
            return pedidoRepository.save(pedido);
        } catch (RuntimeException ex) {
            restaurarStock(stockOriginal);
            throw ex;
        }
    }

    public List<Pedido> listarPorUsuario(String usuarioId) {
        try {
            return pedidoRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId);
        } catch (RuntimeException ex) {
            log.warn("No fue posible consultar pedidos del usuario en MongoDB. Detalle: {}", ex.getMessage());
            return List.of();
        }
    }

    public List<Pedido> listarTodos() {
        try {
            return pedidoRepository.findAllByOrderByFechaCreacionDesc();
        } catch (RuntimeException ex) {
            log.warn("No fue posible consultar pedidos en MongoDB. Detalle: {}", ex.getMessage());
            return List.of();
        }
    }

    public Optional<Pedido> buscarPorId(String id) {
        try {
            return pedidoRepository.findById(id);
        } catch (RuntimeException ex) {
            log.warn("No fue posible consultar el pedido en MongoDB. Detalle: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    public Pedido actualizarSeguimiento(String id, EstadoPedido estado, String trackingCode) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado."));

        pedido.setEstado(estado);
        if (estado == EstadoPedido.ENVIADO && (trackingCode == null || trackingCode.isBlank())) {
            pedido.setTrackingCode(generarTrackingCode(pedido.getId()));
        } else {
            pedido.setTrackingCode((trackingCode == null || trackingCode.isBlank()) ? null : trackingCode.trim());
        }
        pedido.setFechaActualizacion(LocalDateTime.now());

        return pedidoRepository.save(pedido);
    }

    private void restaurarStock(Map<String, Integer> stockOriginal) {
        for (Map.Entry<String, Integer> entry : stockOriginal.entrySet()) {
            productoService.buscarPorId(entry.getKey()).ifPresent(producto -> {
                producto.setStock(entry.getValue());
                productoService.guardar(producto);
            });
        }
    }

    private String generarReferenciaPago() {
        return "PAY-" + LocalDateTime.now().format(PAYMENT_REF_FORMAT);
    }

    private String generarTrackingCode(String pedidoId) {
        String base = pedidoId == null ? Long.toString(System.currentTimeMillis()) : pedidoId;
        String suffix = base.length() > 6 ? base.substring(base.length() - 6) : base;
        return "NXL-" + suffix.toUpperCase();
    }
}
