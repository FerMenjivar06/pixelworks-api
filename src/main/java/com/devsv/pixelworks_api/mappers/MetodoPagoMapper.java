package com.devsv.pixelworks_api.mappers;

import com.devsv.pixelworks_api.dto.MetodoPagoDTO;
import com.devsv.pixelworks_api.entities.MetodoPago;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MetodoPagoMapper {
    MetodoPagoDTO toDTO(MetodoPago metodoPago);
    MetodoPago toEntity(MetodoPagoDTO dto);
}