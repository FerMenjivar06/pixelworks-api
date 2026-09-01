package com.devsv.pixelworks_api.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class ResumenFinancieroDTO {
    private BigDecimal ingresosTotales;
    private long cantidadVentas;
}