package com.devsv.pixelworks_api.mappers;

import com.devsv.pixelworks_api.dto.CategoriaDTO;
import com.devsv.pixelworks_api.entities.Categoria;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoriaMapper {
    CategoriaDTO toDTO(Categoria categoria);
    Categoria toEntity(CategoriaDTO dto);
}