package com.devsv.pixelworks_api.mappers;

import com.devsv.pixelworks_api.dto.OfertaDTO;
import com.devsv.pixelworks_api.entities.Oferta;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OfertaMapper {
    OfertaDTO toDTO(Oferta oferta);
    Oferta toEntity(OfertaDTO dto);
}