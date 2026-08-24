package com.devsv.pixelworks_api.mappers;

import com.devsv.pixelworks_api.dto.DesarrolladorDTO;
import com.devsv.pixelworks_api.entities.Desarrollador;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DesarrolladorMapper {
    DesarrolladorDTO toDTO(Desarrollador desarrollador);
    Desarrollador toEntity(DesarrolladorDTO dto);
}