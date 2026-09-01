package com.devsv.pixelworks_api.repository;

import com.devsv.pixelworks_api.entities.ClaveActivacion;
import com.devsv.pixelworks_api.enums.EstadoClave;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClaveActivacionRepository extends JpaRepository<ClaveActivacion, Integer> {
    boolean existsByCodigo(String codigo);

    long countByProductoIdAndEstado(Integer productoId, EstadoClave estado);

    List<ClaveActivacion> findByProductoIdAndEstado(Integer productoId, EstadoClave estado, Pageable pageable);

    List<ClaveActivacion> findByDetalleCompraId(Integer detalleCompraId);
}