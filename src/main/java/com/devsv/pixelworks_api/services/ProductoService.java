package com.devsv.pixelworks_api.services;

import com.devsv.pixelworks_api.dto.ProductoDTO;
import com.devsv.pixelworks_api.entities.Categoria;
import com.devsv.pixelworks_api.entities.Desarrollador;
import com.devsv.pixelworks_api.entities.Producto;
import com.devsv.pixelworks_api.exceptions.ResourceNotFoundException;
import com.devsv.pixelworks_api.interfaces.IProductoService;
import com.devsv.pixelworks_api.mappers.ProductoMapper;
import com.devsv.pixelworks_api.repository.CategoriaRepository;
import com.devsv.pixelworks_api.repository.DesarrolladorRepository;
import com.devsv.pixelworks_api.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductoService implements IProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final DesarrolladorRepository desarrolladorRepository;
    private final ProductoMapper productoMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ProductoDTO> listarTodos() {
        return productoRepository.findAll().stream()
                .map(productoMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoDTO obtenerPorId(Integer id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con el ID: " + id));
        return productoMapper.toDTO(producto);
    }

    @Override
    @Transactional
    public ProductoDTO guardar(ProductoDTO dto) {
        if (productoRepository.existsByNombre(dto.getNombre())) {
            throw new IllegalArgumentException("Ya existe un producto registrado con ese nombre.");
        }

        Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + dto.getCategoriaId()));

        Desarrollador desarrollador = desarrolladorRepository.findById(dto.getDesarrolladorId())
                .orElseThrow(() -> new ResourceNotFoundException("Desarrollador no encontrado con ID: " + dto.getDesarrolladorId()));

        Producto producto = productoMapper.toEntity(dto, categoria, desarrollador);

        Producto productoGuardado = productoRepository.save(producto);
        return productoMapper.toDTO(productoGuardado);
    }
    @Override
    @Transactional
    public ProductoDTO actualizar(Integer id, ProductoDTO dto) {
        Producto existente = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con el ID: " + id));

        existente.setNombre(dto.getNombre());
        existente.setDescripcion(dto.getDescripcion());
        existente.setAnioLanzamiento(dto.getAnioLanzamiento());
        existente.setPrecio(dto.getPrecio());
        existente.setImagen(dto.getImagen());

        if (dto.getCategoriaId() != null && !existente.getCategoria().getId().equals(dto.getCategoriaId())) {
            Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + dto.getCategoriaId()));
            existente.setCategoria(categoria);
        }

        if (dto.getDesarrolladorId() != null && !existente.getDesarrollador().getId().equals(dto.getDesarrolladorId())) {
            Desarrollador desarrollador = desarrolladorRepository.findById(dto.getDesarrolladorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Desarrollador no encontrado con ID: " + dto.getDesarrolladorId()));
            existente.setDesarrollador(desarrollador);
        }

        Producto actualizado = productoRepository.save(existente);
        return productoMapper.toDTO(actualizado);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con el ID: " + id));

        producto.setActivo(false);
        productoRepository.save(producto);
    }
}