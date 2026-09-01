package com.devsv.pixelworks_api.repository;

import com.devsv.pixelworks_api.entities.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    boolean existsByNombre(String nombre);

    @Query("SELECT p FROM Producto p WHERE p.activo = true AND NOT EXISTS (SELECT c FROM ClaveActivacion c WHERE c.producto = p AND c.estado = 'DISPONIBLE')")
    java.util.List<Producto> findProductosSinStock();
}