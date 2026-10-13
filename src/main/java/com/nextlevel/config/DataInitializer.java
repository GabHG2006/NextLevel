package com.nextlevel.config;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextlevel.model.Categoria;
import com.nextlevel.model.EstadoPago;
import com.nextlevel.model.EstadoPedido;
import com.nextlevel.model.ItemPedido;
import com.nextlevel.model.MetodoPago;
import com.nextlevel.model.Pedido;
import com.nextlevel.model.Producto;
import com.nextlevel.model.Rol;
import com.nextlevel.model.TipoMetodoPago;
import com.nextlevel.model.Usuario;
import com.nextlevel.repository.CategoriaRepository;
import com.nextlevel.repository.PedidoRepository;
import com.nextlevel.repository.ProductoRepository;
import com.nextlevel.repository.UsuarioRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private static final String LEGACY_DEFAULT_PASSWORD = "NextLevel123*";

    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;
    private final PedidoRepository pedidoRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    public DataInitializer(UsuarioRepository usuarioRepository,
            CategoriaRepository categoriaRepository,
            ProductoRepository productoRepository,
            PedidoRepository pedidoRepository,
            PasswordEncoder passwordEncoder,
            ObjectMapper objectMapper) {
        this.usuarioRepository = usuarioRepository;
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
        this.pedidoRepository = pedidoRepository;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(String... args) {
        try {
            crearUsuariosBase();
            migrarClientesLegacy();
            asegurarMetodosPagoSimulados();
            cargarCategoriasDesdeJson();
            cargarProductosDesdeJson();
            normalizarPreciosProductosExistentes();
            crearPedidosDemo();
            normalizarPedidosExistentes();

            log.info("Inicialización completada -> usuarios: {}, categorias: {}, productos: {}, pedidos: {}",
                    usuarioRepository.count(), categoriaRepository.count(), productoRepository.count(), pedidoRepository.count());
        } catch (Exception ex) {
            log.warn("La aplicación inició sin inicializar MongoDB. "
                    + "Configura MONGODB_URI en Render para usar base de datos real. Detalle: {}", ex.getMessage());
        }
    }

    private void crearUsuariosBase() {
        if (!usuarioRepository.existsByCorreoIgnoreCase("admin@nextlevel.com")) {
            Usuario admin = new Usuario();
            admin.setNombre("Admin");
            admin.setApellido("NextLevel");
            admin.setCorreo("admin@nextlevel.com");
            admin.setDireccion("Centro logístico NextLevel - Cartagena");
            admin.setPassword(passwordEncoder.encode("Admin123*"));
            admin.setRol(Rol.ROLE_ADMIN);
            admin.setEnabled(true);
            admin.setAccountNonLocked(true);
            usuarioRepository.save(admin);
        }

        if (!usuarioRepository.existsByCorreoIgnoreCase("cliente@nextlevel.com")) {
            Usuario cliente = new Usuario();
            cliente.setNombre("Cliente");
            cliente.setApellido("Demo");
            cliente.setCorreo("cliente@nextlevel.com");
            cliente.setTelefono("3000000000");
            cliente.setDireccion("Cartagena, Bolívar");
            cliente.setPassword(passwordEncoder.encode("Cliente123*"));
            cliente.setRol(Rol.ROLE_CLIENTE);
            cliente.setEnabled(true);
            cliente.setAccountNonLocked(true);
            usuarioRepository.save(cliente);
        }
    }

    private void migrarClientesLegacy() {
        List<LegacyClientSeed> clientes = leerLista("seed/clientes_legacy.json",
                new TypeReference<List<LegacyClientSeed>>() {
                });

        int creados = 0;
        for (LegacyClientSeed seed : clientes) {
            if (usuarioRepository.existsByCorreoIgnoreCase(seed.correo())) {
                continue;
            }

            Usuario usuario = new Usuario();
            usuario.setNombre(seed.nombre());
            usuario.setApellido(seed.apellido());
            usuario.setCorreo(seed.correo());
            usuario.setTelefono(seed.telefono());
            usuario.setDireccion(seed.direccion());
            usuario.setPassword(passwordEncoder.encode(LEGACY_DEFAULT_PASSWORD));
            usuario.setRol(Rol.ROLE_CLIENTE);
            usuario.setEnabled(true);
            usuario.setAccountNonLocked(true);
            usuarioRepository.save(usuario);
            creados++;
        }

        if (creados > 0) {
            log.info("Clientes legacy migrados desde SQL a MongoDB: {}", creados);
            log.info("Contraseña temporal de clientes migrados: {}", LEGACY_DEFAULT_PASSWORD);
        }
    }

    private void asegurarMetodosPagoSimulados() {
        int actualizados = 0;
        for (Usuario usuario : usuarioRepository.findAll()) {
            if (usuario.getMetodosPago() != null && !usuario.getMetodosPago().isEmpty()) {
                continue;
            }
            usuario.setMetodosPago(crearMetodosBase(usuario));
            usuarioRepository.save(usuario);
            actualizados++;
        }

        if (actualizados > 0) {
            log.info("Métodos de pago simulados asignados a usuarios existentes: {}", actualizados);
        }
    }

    private List<MetodoPago> crearMetodosBase(Usuario usuario) {
        String titular = usuario.getNombreCompleto().trim().isBlank() ? usuario.getCorreo() : usuario.getNombreCompleto().trim();
        List<MetodoPago> metodos = new ArrayList<>();

        metodos.add(crearMetodo(TipoMetodoPago.TARJETA, "Bancolombia", titular,
                "**** " + Math.abs(usuario.getCorreo().hashCode() % 9000 + 1000), new BigDecimal("950000")));
        metodos.add(crearMetodo(TipoMetodoPago.NEQUI, "Nequi", titular,
                usuario.getTelefono() == null || usuario.getTelefono().isBlank() ? "3000000000" : usuario.getTelefono(),
                new BigDecimal("250000")));
        metodos.add(crearMetodo(TipoMetodoPago.PAYPAL, "PayPal", titular,
                usuario.getCorreo(), new BigDecimal("650000")));

        return metodos;
    }

    private MetodoPago crearMetodo(TipoMetodoPago tipo, String entidad, String titular, String referencia,
            BigDecimal saldo) {
        MetodoPago metodoPago = new MetodoPago();
        metodoPago.setId(UUID.randomUUID().toString());
        metodoPago.setTipo(tipo);
        metodoPago.setEntidad(entidad);
        metodoPago.setTitular(titular);
        metodoPago.setReferencia(referencia);
        metodoPago.setSaldoDisponible(saldo);
        metodoPago.setActivo(true);
        metodoPago.setCreatedAt(LocalDateTime.now());
        return metodoPago;
    }

    private void cargarCategoriasDesdeJson() {
        List<Categoria> categorias = leerLista("seed/categorias.json", new TypeReference<List<Categoria>>() {
        });

        if (categoriaRepository.count() >= categorias.size()) {
            return;
        }

        categoriaRepository.saveAll(categorias);
        log.info("Categorías sembradas/actualizadas en MongoDB: {}", categorias.size());
    }

    private void cargarProductosDesdeJson() {
        List<Producto> productos = leerLista("seed/productos.json", new TypeReference<List<Producto>>() {
        });

        if (productoRepository.count() >= productos.size()) {
            log.info("La colección productos ya tiene {} documentos. No se reemplaza la carga masiva.", productoRepository.count());
            return;
        }

        productos.forEach(this::normalizarProducto);
        productoRepository.saveAll(productos);
        log.info("Colección productos sembrada/actualizada en MongoDB con {} documentos.", productos.size());
    }


    private void normalizarPreciosProductosExistentes() {
        int actualizados = 0;
        for (Producto producto : productoRepository.findAll()) {
            if (normalizarPrecioProducto(producto)) {
                producto.setUpdatedAt(LocalDateTime.now());
                productoRepository.save(producto);
                actualizados++;
            }
        }
        if (actualizados > 0) {
            log.info("Precios normalizados a pesos colombianos en productos existentes: {}", actualizados);
        }
    }

    private boolean normalizarPrecioProducto(Producto producto) {
        if (producto == null || producto.getPrecio() == null) {
            return false;
        }
        if (producto.getPrecio().compareTo(new BigDecimal("1000")) < 0) {
            producto.setPrecio(producto.getPrecio().multiply(new BigDecimal("1000")).setScale(0, RoundingMode.HALF_UP));
            return true;
        }
        return false;
    }

    private void crearPedidosDemo() {
        if (pedidoRepository.count() > 0 || productoRepository.count() < 3) {
            return;
        }

        Usuario clienteDemo = usuarioRepository.findByCorreoIgnoreCase("cliente@nextlevel.com").orElse(null);
        if (clienteDemo == null) {
            return;
        }

        Producto producto1 = productoRepository.findById("1").orElse(null);
        Producto producto2 = productoRepository.findById("2").orElse(null);
        Producto producto3 = productoRepository.findById("3").orElse(null);

        if (producto1 == null || producto2 == null || producto3 == null) {
            return;
        }

        Pedido pedido1 = new Pedido();
        pedido1.setUsuarioId(clienteDemo.getId());
        pedido1.setCorreoCliente(clienteDemo.getCorreo());
        pedido1.setNombreCliente(clienteDemo.getNombreCompleto().trim());
        pedido1.setDireccionEnvio(clienteDemo.getDireccion());
        pedido1.setEstado(EstadoPedido.EN_PREPARACION);
        pedido1.setEstadoPago(EstadoPago.APROBADO);
        pedido1.setMetodoPagoTipo(TipoMetodoPago.NEQUI.name());
        pedido1.setMetodoPagoEntidad("Nequi");
        pedido1.setReferenciaPago("PAY-DEMO-1001");
        pedido1.setFechaPago(LocalDateTime.now().minusDays(2));
        pedido1.setTrackingCode("NXTL-1001");
        pedido1.setItems(List.of(
                crearItem(producto1, 1),
                crearItem(producto2, 2)));
        pedido1.setTotal(calcularTotal(pedido1.getItems()));
        pedido1.setFechaCreacion(LocalDateTime.now().minusDays(2));
        pedido1.setFechaActualizacion(LocalDateTime.now().minusDays(1));

        Pedido pedido2 = new Pedido();
        pedido2.setUsuarioId(clienteDemo.getId());
        pedido2.setCorreoCliente(clienteDemo.getCorreo());
        pedido2.setNombreCliente(clienteDemo.getNombreCompleto().trim());
        pedido2.setDireccionEnvio(clienteDemo.getDireccion());
        pedido2.setEstado(EstadoPedido.ENVIADO);
        pedido2.setEstadoPago(EstadoPago.APROBADO);
        pedido2.setMetodoPagoTipo(TipoMetodoPago.PAYPAL.name());
        pedido2.setMetodoPagoEntidad("PayPal");
        pedido2.setReferenciaPago("PAY-DEMO-1002");
        pedido2.setFechaPago(LocalDateTime.now().minusDays(5));
        pedido2.setTrackingCode("NXTL-1002");
        pedido2.setItems(List.of(
                crearItem(producto3, 1)));
        pedido2.setTotal(calcularTotal(pedido2.getItems()));
        pedido2.setFechaCreacion(LocalDateTime.now().minusDays(5));
        pedido2.setFechaActualizacion(LocalDateTime.now().minusDays(3));

        pedidoRepository.saveAll(List.of(pedido1, pedido2));
        log.info("Pedidos demo creados para validar el módulo administrativo.");
    }

    private void normalizarPedidosExistentes() {
        int actualizados = 0;
        for (Pedido pedido : pedidoRepository.findAll()) {
            boolean cambiado = false;

            if (pedido.getEstadoPago() == null) {
                pedido.setEstadoPago(EstadoPago.APROBADO);
                cambiado = true;
            }
            if (pedido.getMetodoPagoTipo() == null || pedido.getMetodoPagoTipo().isBlank()) {
                pedido.setMetodoPagoTipo(TipoMetodoPago.TARJETA.name());
                cambiado = true;
            }
            if (pedido.getMetodoPagoEntidad() == null || pedido.getMetodoPagoEntidad().isBlank()) {
                pedido.setMetodoPagoEntidad("Pago simulado");
                cambiado = true;
            }
            if (pedido.getReferenciaPago() == null || pedido.getReferenciaPago().isBlank()) {
                pedido.setReferenciaPago("MIGRADO-" + pedido.getId());
                cambiado = true;
            }
            if (pedido.getFechaPago() == null) {
                pedido.setFechaPago(pedido.getFechaCreacion());
                cambiado = true;
            }
            if (cambiado) {
                pedido.setFechaActualizacion(LocalDateTime.now());
                pedidoRepository.save(pedido);
                actualizados++;
            }
        }

        if (actualizados > 0) {
            log.info("Pedidos existentes normalizados con datos de pago simulados: {}", actualizados);
        }
    }

    private ItemPedido crearItem(Producto producto, int cantidad) {
        ItemPedido item = new ItemPedido();
        item.setProductoId(producto.getId());
        item.setNombreProducto(producto.getNombre());
        item.setCategoriaNombre(producto.getCategoriaNombre());
        item.setPrecioUnitario(producto.getPrecio());
        item.setCantidad(cantidad);
        item.setSubtotal(producto.getPrecio().multiply(BigDecimal.valueOf(cantidad)));
        item.setImagen(producto.getImagen());
        return item;
    }

    private BigDecimal calcularTotal(List<ItemPedido> items) {
        return items.stream()
                .map(ItemPedido::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void normalizarProducto(Producto producto) {
        if (producto.getActivo() == null) {
            producto.setActivo(true);
        }
        if (producto.getStock() == null) {
            producto.setStock(0);
        }
        if (producto.getImagen() == null || producto.getImagen().isBlank()) {
            producto.setImagen("/images/default.png");
        }
        normalizarPrecioProducto(producto);
        if (producto.getUpdatedAt() == null) {
            producto.setUpdatedAt(LocalDateTime.now());
        }
    }

    private <T> List<T> leerLista(String path, TypeReference<List<T>> typeReference) {
        try (InputStream inputStream = new ClassPathResource(path).getInputStream()) {
            return objectMapper.readValue(inputStream, typeReference);
        } catch (IOException e) {
            throw new IllegalStateException("No fue posible cargar el recurso " + path, e);
        }
    }

    private record LegacyClientSeed(String nombre, String apellido, String correo, String telefono, String direccion) {
    }
}
