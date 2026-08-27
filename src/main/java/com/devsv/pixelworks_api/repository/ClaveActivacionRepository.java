package com.devsv.pixelworks_api.repository;

import com.devsv.pixelworks_api.entities.ClaveActivacion;
import com.devsv.pixelworks_api.enums.EstadoClave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClaveActivacionRepository extends JpaRepository<ClaveActivacion, Integer> {

    // Evita que el administrador suba la misma clave dos veces
    boolean existsByCodigo(String codigo);

    // Cuenta cuántas claves están "DISPONIBLES" para un juego específico (Este es tu Stock)
    long countByProductoIdAndEstado(Integer productoId, EstadoClave estado);
}