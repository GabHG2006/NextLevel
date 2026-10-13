package com.nextlevel.service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextlevel.model.Categoria;
import com.nextlevel.repository.CategoriaRepository;

@Service
public class CategoriaService {

    private static final Logger log = LoggerFactory.getLogger(CategoriaService.class);

    private final CategoriaRepository categoriaRepository;
    private final ObjectMapper objectMapper;

    public CategoriaService(CategoriaRepository categoriaRepository, ObjectMapper objectMapper) {
        this.categoriaRepository = categoriaRepository;
        this.objectMapper = objectMapper;
    }

    public List<Categoria> listar() {
        try {
            return ordenar(categoriaRepository.findAll());
        } catch (RuntimeException ex) {
            log.warn("No fue posible consultar categorias en MongoDB. Se usan datos demo desde JSON. Detalle: {}",
                    ex.getMessage());
            return ordenar(categoriasDesdeJson());
        }
    }

    public Optional<Categoria> buscarPorId(String id) {
        try {
            return categoriaRepository.findById(id);
        } catch (RuntimeException ex) {
            return categoriasDesdeJson().stream()
                    .filter(categoria -> id != null && id.equals(categoria.getId()))
                    .findFirst();
        }
    }

    public Categoria guardar(Categoria categoria) {
        return categoriaRepository.save(categoria);
    }

    private List<Categoria> ordenar(List<Categoria> categorias) {
        return categorias.stream()
                .sorted(Comparator.comparing(Categoria::getNombre, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private List<Categoria> categoriasDesdeJson() {
        try {
            ClassPathResource resource = new ClassPathResource("seed/categorias.json");
            return objectMapper.readValue(resource.getInputStream(), new TypeReference<List<Categoria>>() {});
        } catch (Exception ex) {
            log.warn("No fue posible leer seed/categorias.json. Detalle: {}", ex.getMessage());
            return List.of();
        }
    }
}
