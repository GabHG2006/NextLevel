package com.nextlevel.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.nextlevel.model.Producto;

public interface ProductoRepository extends MongoRepository<Producto, String> {
}
