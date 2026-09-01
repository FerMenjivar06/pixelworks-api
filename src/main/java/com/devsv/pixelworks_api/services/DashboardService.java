package com.devsv.pixelworks_api.services;

import com.devsv.pixelworks_api.dto.ResumenFinancieroDTO;
import com.devsv.pixelworks_api.dto.ProductoAlertaDTO;
import com.devsv.pixelworks_api.entities.Producto;
import com.devsv.pixelworks_api.interfaces.IDashboardService;
import com.devsv.pixelworks_api.repository.CompraRepository;
import com.devsv.pixelworks_api.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService implements IDashboardService {

    private final CompraRepository compraRepository;
    private final ProductoRepository productoRepository;

    @Override
    public ResumenFinancieroDTO obtenerFinanzas(LocalDateTime inicio, LocalDateTime fin) {
        ResumenFinancieroDTO dto = new ResumenFinancieroDTO();
        BigDecimal total;
        long cantidad;

        if (inicio != null && fin != null) {
            total = compraRepository.calcularIngresosPorFecha(inicio, fin);
            cantidad = compraRepository.contarVentasPorFecha(inicio, fin);
        } else {
            total = compraRepository.calcularIngresosTotales();
            cantidad = compraRepository.contarVentasTotales();
        }

        dto.setIngresosTotales(total != null ? total : BigDecimal.ZERO);
        dto.setCantidadVentas(cantidad);
        return dto;
    }

    @Override
    public List<ProductoAlertaDTO> obtenerJuegosSinStock() {
        List<Producto> agotados = productoRepository.findProductosSinStock();

        return agotados.stream().map(p -> {
            ProductoAlertaDTO dto = new ProductoAlertaDTO();
            dto.setId(p.getId());
            dto.setNombre(p.getNombre());
            return dto;
        }).toList();
    }
}