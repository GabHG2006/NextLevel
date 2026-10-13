package com.nextlevel.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.nextlevel.model.Usuario;

public interface UsuarioRepository extends MongoRepository<Usuario, String> {
    Optional<Usuario> findByCorreoIgnoreCase(String correo);
    boolean existsByCorreoIgnoreCase(String correo);
}
