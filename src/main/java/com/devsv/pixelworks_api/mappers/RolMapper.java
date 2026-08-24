package com.devsv.pixelworks_api.mappers;

import com.devsv.pixelworks_api.dto.RolDTO;
import com.devsv.pixelworks_api.entities.Rol;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RolMapper {
    RolDTO toDTO(Rol rol);
    Rol toEntity(RolDTO dto);
}