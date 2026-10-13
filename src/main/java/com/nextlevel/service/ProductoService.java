package com.nextlevel.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextlevel.model.Categoria;
import com.nextlevel.model.Producto;
import com.nextlevel.repository.ProductoRepository;

@Service
public class ProductoService {

    private static final Logger log = LoggerFactory.getLogger(ProductoService.class);

    private final ProductoRepository productoRepository;
    private final CategoriaService categoriaService;
    private final ObjectMapper objectMapper;

    public ProductoService(ProductoRepository productoRepository, CategoriaService categoriaService,
            ObjectMapper objectMapper) {
        this.productoRepository = productoRepository;
        this.categoriaService = categoriaService;
        this.objectMapper = objectMapper;
    }

    public List<Producto> listar() {
        try {
            return ordenarActivos(productoRepository.findAll());
        } catch (RuntimeException ex) {
            log.warn("No fue posible consultar productos en MongoDB. Se usan los 1000 registros demo desde JSON. Detalle: {}",
                    ex.getMessage());
            return ordenarActivos(productosDesdeJson());
        }
    }

    public List<Producto> buscar(String categoriaId, String query) {
        String filtro = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        boolean tieneCategoria = categoriaId != null && !categoriaId.isBlank();

        List<Producto> filtrados = listar().stream()
                .filter(p -> !tieneCategoria || categoriaId.equals(p.getCategoriaId()))
                .filter(p -> filtro.isBlank()
                        || contiene(p.getNombre(), filtro)
                        || contiene(p.getDescripcion(), filtro)
                        || contiene(p.getCategoriaNombre(), filtro))
                .toList();

        if (!tieneCategoria && filtro.isBlank()) {
            return diversificarCatalogo(filtrados);
        }
        return filtrados;
    }

    public Optional<Producto> buscarPorId(String id) {
        try {
            return productoRepository.findById(id);
        } catch (RuntimeException ex) {
            return productosDesdeJson().stream()
                    .filter(producto -> id != null && id.equals(producto.getId()))
                    .findFirst();
        }
    }

    public Producto guardar(Producto producto) {
        Categoria categoria = categoriaService.buscarPorId(producto.getCategoriaId())
                .orElseThrow(() -> new IllegalArgumentException("La categoría seleccionada no existe."));

        producto.setCategoriaNombre(categoria.getNombre());
        producto.setUpdatedAt(LocalDateTime.now());

        if (producto.getImagen() == null || producto.getImagen().isBlank()) {
            producto.setImagen("/images/default.png");
        }

        if (producto.getStock() == null || producto.getStock() < 0) {
            producto.setStock(0);
        }

        return productoRepository.save(producto);
    }

    public void eliminar(String id) {
        productoRepository.deleteById(id);
    }

    public List<Producto> listarStockBajo(int umbral) {
        return listar().stream()
                .filter(p -> p.getStock() != null && p.getStock() <= umbral)
                .toList();
    }

    public void descontarStock(String productoId, int cantidad) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado."));
        if (producto.getStock() < cantidad) {
            throw new IllegalArgumentException("Stock insuficiente para " + producto.getNombre());
        }
        producto.setStock(producto.getStock() - cantidad);
        producto.setUpdatedAt(LocalDateTime.now());
        productoRepository.save(producto);
    }

    private List<Producto> ordenarActivos(List<Producto> productos) {
        return productos.stream()
                .filter(producto -> producto.getActivo() == null || producto.getActivo())
                .sorted(comparadorCatalogo())
                .toList();
    }

    private Comparator<Producto> comparadorCatalogo() {
        return Comparator
                .comparing(this::updatedAtOrMin, Comparator.reverseOrder())
                .thenComparing((Producto producto) -> producto.getStock() > 0 ? 0 : 1)
                .thenComparing(producto -> textoSeguro(producto.getNombre()), String.CASE_INSENSITIVE_ORDER);
    }

    private List<Producto> diversificarCatalogo(List<Producto> productos) {
        List<Producto> pendientes = new ArrayList<>(productos);
        List<Producto> resultado = new ArrayList<>();
        String ultimaClave = "";
        String ultimaCategoria = "";

        while (!pendientes.isEmpty()) {
            int indiceSeleccionado = buscarIndiceDiversificado(pendientes, ultimaClave, ultimaCategoria);
            Producto siguiente = pendientes.remove(indiceSeleccionado);
            resultado.add(siguiente);
            ultimaClave = claveCatalogo(siguiente.getNombre());
            ultimaCategoria = textoSeguro(siguiente.getCategoriaNombre()).toLowerCase(Locale.ROOT);
        }

        return resultado;
    }

    private int buscarIndiceDiversificado(List<Producto> productos, String ultimaClave, String ultimaCategoria) {
        for (int i = 0; i < productos.size(); i++) {
            Producto producto = productos.get(i);
            String claveActual = claveCatalogo(producto.getNombre());
            String categoriaActual = textoSeguro(producto.getCategoriaNombre()).toLowerCase(Locale.ROOT);
            if (!claveActual.equals(ultimaClave) && !categoriaActual.equals(ultimaCategoria)) {
                return i;
            }
        }
        for (int i = 0; i < productos.size(); i++) {
            Producto producto = productos.get(i);
            String claveActual = claveCatalogo(producto.getNombre());
            if (!claveActual.equals(ultimaClave)) {
                return i;
            }
        }
        return 0;
    }

    private boolean contiene(String texto, String filtro) {
        return texto != null && texto.toLowerCase(Locale.ROOT).contains(filtro);
    }

    private String claveCatalogo(String nombre) {
        String base = textoSeguro(nombre).toLowerCase(Locale.ROOT);
        base = base.replaceAll("#\\s*\\d+", " ");
        base = base.replaceAll("\\b\\d+\\b", " ");
        base = base.replaceAll("[^a-z0-9áéíóúñü ]", " ");
        base = base.replaceAll("\\s+", " ").trim();
        return base.isBlank() ? "producto" : base;
    }


    private void normalizarProductoDemo(Producto producto) {
        if (producto == null) {
            return;
        }
        if (producto.getPrecio() != null && producto.getPrecio().compareTo(new BigDecimal("1000")) < 0) {
            producto.setPrecio(producto.getPrecio().multiply(new BigDecimal("1000")).setScale(0, RoundingMode.HALF_UP));
        }
        if (producto.getImagen() == null || producto.getImagen().isBlank()) {
            producto.setImagen("/images/default.png");
        }
        if (producto.getActivo() == null) {
            producto.setActivo(true);
        }
    }

    private String textoSeguro(String texto) {
        return texto == null ? "" : texto;
    }

    private LocalDateTime updatedAtOrMin(Producto producto) {
        return producto.getUpdatedAt() == null ? LocalDateTime.MIN : producto.getUpdatedAt();
    }

    private List<Producto> productosDesdeJson() {
        try {
            ClassPathResource resource = new ClassPathResource("seed/productos.json");
            List<Producto> productos = objectMapper.readValue(resource.getInputStream(), new TypeReference<List<Producto>>() {});
            productos.forEach(this::normalizarProductoDemo);
            return productos;
        } catch (Exception ex) {
            log.warn("No fue posible leer seed/productos.json. Detalle: {}", ex.getMessage());
            return List.of();
        }
    }
}
