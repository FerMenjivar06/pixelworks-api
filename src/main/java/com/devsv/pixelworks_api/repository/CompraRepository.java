package com.devsv.pixelworks_api.repository;

import com.devsv.pixelworks_api.entities.Compra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CompraRepository extends JpaRepository<Compra, Integer> {

    List<Compra> findByUsuarioId(Integer usuarioId);

    @Query("SELECT SUM(c.total) FROM Compra c")
    java.math.BigDecimal calcularIngresosTotales();

    @Query("SELECT COUNT(c) FROM Compra c")
    long contarVentasTotales();

    @Query("SELECT SUM(c.total) FROM Compra c WHERE c.fechaVenta BETWEEN :inicio AND :fin")
    java.math.BigDecimal calcularIngresosPorFecha(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    @Query("SELECT COUNT(c) FROM Compra c WHERE c.fechaVenta BETWEEN :inicio AND :fin")
    long contarVentasPorFecha(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);
}