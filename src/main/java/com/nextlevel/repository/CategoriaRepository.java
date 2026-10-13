package com.nextlevel.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.nextlevel.model.Categoria;

public interface CategoriaRepository extends MongoRepository<Categoria, String> {
    Optional<Categoria> findByNombreIgnoreCase(String nombre);
}
