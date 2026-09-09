package com.devsv.pixelworks_api.services;

import com.devsv.pixelworks_api.dto.ProductoDTO;
import com.devsv.pixelworks_api.entities.Categoria;
import com.devsv.pixelworks_api.entities.Desarrollador;
import com.devsv.pixelworks_api.entities.Producto;
import com.devsv.pixelworks_api.exceptions.ResourceNotFoundException;
import com.devsv.pixelworks_api.interfaces.IClaveActivacionService;
import com.devsv.pixelworks_api.interfaces.IOfertaService;
import com.devsv.pixelworks_api.interfaces.IProductoService;
import com.devsv.pixelworks_api.mappers.ProductoMapper;
import com.devsv.pixelworks_api.repository.CategoriaRepository;
import com.devsv.pixelworks_api.repository.DesarrolladorRepository;
import com.devsv.pixelworks_api.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductoService implements IProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final DesarrolladorRepository desarrolladorRepository;
    private final ProductoMapper productoMapper;

    private final IClaveActivacionService claveActivacionService;
    private final IOfertaService ofertaService;

    @Override
    @Transactional(readOnly = true)
    public List<ProductoDTO> listarTodos() {
        return productoRepository.findAll().stream()
                .map(this::completarDatosProducto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoDTO obtenerPorId(Integer id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con el ID: " + id));
        return completarDatosProducto(producto);
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
        return productoMapper.toDTO(productoRepository.save(producto));
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

        return productoMapper.toDTO(productoRepository.save(existente));
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con el ID: " + id));

        producto.setActivo(false);
        productoRepository.save(producto);
    }

    private ProductoDTO completarDatosProducto(Producto producto) {
        ProductoDTO dto = productoMapper.toDTO(producto);

        long stock = claveActivacionService.contarStockDisponible(producto.getId());
        dto.setStock((int) stock);

        BigDecimal descuento = ofertaService.obtenerDescuentoActivo(producto.getId());
        dto.setPorcentajeDescuento(descuento);

        if (descuento != null && descuento.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal factor = BigDecimal.ONE.subtract(descuento.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
            BigDecimal precioFinal = producto.getPrecio().multiply(factor).setScale(2, RoundingMode.HALF_UP);
            dto.setPrecioConDescuento(precioFinal);
        } else {
            dto.setPrecioConDescuento(null);
        }

        return dto;
    }
}