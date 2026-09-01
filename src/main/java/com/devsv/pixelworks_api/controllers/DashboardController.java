package com.devsv.pixelworks_api.controllers;

import com.devsv.pixelworks_api.dto.ResumenFinancieroDTO;
import com.devsv.pixelworks_api.dto.ProductoAlertaDTO;
import com.devsv.pixelworks_api.interfaces.IDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final IDashboardService dashboardService;

    @GetMapping("/finanzas")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResumenFinancieroDTO> verFinanzas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {

        LocalDateTime inicio = (fechaInicio != null) ? fechaInicio.atStartOfDay() : null;
        LocalDateTime fin = (fechaFin != null) ? fechaFin.atTime(LocalTime.MAX) : null;

        return ResponseEntity.ok(dashboardService.obtenerFinanzas(inicio, fin));
    }

    @GetMapping("/alertas/stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ProductoAlertaDTO>> verAlertasStock() {
        return ResponseEntity.ok(dashboardService.obtenerJuegosSinStock());
    }
}