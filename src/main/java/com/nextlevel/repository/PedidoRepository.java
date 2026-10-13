package com.nextlevel.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.nextlevel.model.Pedido;

public interface PedidoRepository extends MongoRepository<Pedido, String> {
    List<Pedido> findByUsuarioIdOrderByFechaCreacionDesc(String usuarioId);
    List<Pedido> findAllByOrderByFechaCreacionDesc();
}
