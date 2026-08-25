package com.devsv.pixelworks_api.repository;

import com.devsv.pixelworks_api.entities.OfertaProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OfertaProductoRepository extends JpaRepository<OfertaProducto, Integer> {
    boolean existsByOfertaIdAndProductoId(Integer ofertaId, Integer productoId);
    List<OfertaProducto> findByOfertaId(Integer ofertaId);
}