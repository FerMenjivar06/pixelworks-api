
package com.devsv.pixelworks_api.services;

import com.devsv.pixelworks_api.dto.ProductoDTO;
import com.devsv.pixelworks_api.entities.Categoria;
import com.devsv.pixelworks_api.entities.Desarrollador;
import com.devsv.pixelworks_api.entities.Oferta;
import com.devsv.pixelworks_api.entities.OfertaProducto;
import com.devsv.pixelworks_api.entities.Producto;
import com.devsv.pixelworks_api.enums.EstadoClave;
import com.devsv.pixelworks_api.exceptions.ResourceNotFoundException;
import com.devsv.pixelworks_api.interfaces.IProductoService;
import com.devsv.pixelworks_api.mappers.ProductoMapper;
import com.devsv.pixelworks_api.repository.CategoriaRepository;
import com.devsv.pixelworks_api.repository.ClaveActivacionRepository;
import com.devsv.pixelworks_api.repository.DesarrolladorRepository;
import com.devsv.pixelworks_api.repository.OfertaProductoRepository;
import com.devsv.pixelworks_api.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductoService implements IProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final DesarrolladorRepository desarrolladorRepository;
    private final ClaveActivacionRepository claveActivacionRepository;
    private final OfertaProductoRepository ofertaProductoRepository;
    private final ProductoMapper productoMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ProductoDTO> listarTodos() {

        List<ProductoDTO> productos = productoRepository.findByActivoTrue()
                .stream()
                .map(productoMapper::toDTO)
                .collect(Collectors.toList());

        enriquecerInformacionComercial(productos);

        return productos;
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoDTO obtenerPorId(Integer id) {

        Producto producto = productoRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Producto no encontrado con el ID: " + id
                        )
                );

        ProductoDTO dto = productoMapper.toDTO(producto);

        enriquecerInformacionComercial(List.of(dto));

        return dto;
    }

    /**
     * Agrega al DTO la información necesaria para la tienda:
     * - stock de claves disponibles
     * - porcentaje de descuento de una oferta vigente
     * - precio final con descuento
     */
    private void enriquecerInformacionComercial(
            List<ProductoDTO> productos
    ) {

        if (productos == null || productos.isEmpty()) {
            return;
        }

        List<Integer> productoIds = productos.stream()
                .map(ProductoDTO::getId)
                .toList();

        /*
         * ==========================================================
         * STOCK DE CLAVES DISPONIBLES
         * ==========================================================
         */

        Map<Integer, Long> stockPorProducto = new HashMap<>();

        List<ClaveActivacionRepository.StockDisponibleProjection> stockResultados =
                claveActivacionRepository.obtenerStockPorProductos(
                        productoIds,
                        EstadoClave.DISPONIBLE
                );

        stockResultados.forEach(resultado ->
                stockPorProducto.put(
                        resultado.getProductoId(),
                        resultado.getStock()
                )
        );

        /*
         * ==========================================================
         * OFERTAS ACTIVAS
         * ==========================================================
         */

        Map<Integer, Oferta> ofertaPorProducto = new HashMap<>();

        List<OfertaProducto> relaciones =
                ofertaProductoRepository.obtenerOfertasActivasPorProductos(
                        productoIds
                );

        /*
         * La consulta devuelve las ofertas ordenadas por fecha de
         * inicio descendente. Si un producto tiene más de una oferta
         * vigente, conservamos la primera.
         */
        for (OfertaProducto relacion : relaciones) {

            Integer productoId =
                    relacion.getProducto().getId();

            ofertaPorProducto.putIfAbsent(
                    productoId,
                    relacion.getOferta()
            );
        }

        /*
         * ==========================================================
         * ENRIQUECIMIENTO DEL DTO
         * ==========================================================
         */

        for (ProductoDTO producto : productos) {

            /*
             * STOCK
             */

            Long stockDisponible =
                    stockPorProducto.getOrDefault(
                            producto.getId(),
                            0L
                    );

            producto.setStock(stockDisponible);

            /*
             * OFERTA
             */

            Oferta oferta =
                    ofertaPorProducto.get(producto.getId());

            if (oferta == null) {

                producto.setPorcentajeDescuento(null);
                producto.setPrecioConDescuento(null);

                continue;
            }

            BigDecimal descuento =
                    oferta.getPorcentajeDescuento();

            producto.setPorcentajeDescuento(descuento);

            /*
             * ======================================================
             * PRECIO CON DESCUENTO
             * ======================================================
             *
             * precioFinal =
             * precio - (precio * descuento / 100)
             */

            BigDecimal porcentaje =
                    descuento.divide(
                            BigDecimal.valueOf(100),
                            6,
                            RoundingMode.HALF_UP
                    );

            BigDecimal factor =
                    BigDecimal.ONE.subtract(porcentaje);

            BigDecimal precioFinal =
                    producto.getPrecio()
                            .multiply(factor)
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP
                            );

            producto.setPrecioConDescuento(precioFinal);
        }
    }

    @Override
    @Transactional
    public ProductoDTO guardar(ProductoDTO dto) {

        if (productoRepository.existsByNombre(dto.getNombre())) {
            throw new IllegalArgumentException(
                    "Ya existe un producto registrado con ese nombre."
            );
        }

        Categoria categoria =
                categoriaRepository.findById(dto.getCategoriaId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Categoría no encontrada con ID: "
                                                + dto.getCategoriaId()
                                )
                        );

        Desarrollador desarrollador =
                desarrolladorRepository.findById(dto.getDesarrolladorId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Desarrollador no encontrado con ID: "
                                                + dto.getDesarrolladorId()
                                )
                        );

        Producto producto =
                productoMapper.toEntity(
                        dto,
                        categoria,
                        desarrollador
                );

        Producto productoGuardado =
                productoRepository.save(producto);

        ProductoDTO respuesta =
                productoMapper.toDTO(productoGuardado);

        enriquecerInformacionComercial(
                List.of(respuesta)
        );

        return respuesta;
    }

    @Override
    @Transactional
    public ProductoDTO actualizar(
            Integer id,
            ProductoDTO dto
    ) {

        Producto existente =
                productoRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Producto no encontrado con el ID: " + id
                                )
                        );

        existente.setNombre(dto.getNombre());
        existente.setDescripcion(dto.getDescripcion());
        existente.setAnioLanzamiento(dto.getAnioLanzamiento());
        existente.setPrecio(dto.getPrecio());
        existente.setImagen(dto.getImagen());

        if (
                dto.getCategoriaId() != null
                        && !existente
                        .getCategoria()
                        .getId()
                        .equals(dto.getCategoriaId())
        ) {

            Categoria categoria =
                    categoriaRepository.findById(
                            dto.getCategoriaId()
                    ).orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Categoría no encontrada con ID: "
                                            + dto.getCategoriaId()
                            )
                    );

            existente.setCategoria(categoria);
        }

        if (
                dto.getDesarrolladorId() != null
                        && !existente
                        .getDesarrollador()
                        .getId()
                        .equals(dto.getDesarrolladorId())
        ) {

            Desarrollador desarrollador =
                    desarrolladorRepository.findById(
                            dto.getDesarrolladorId()
                    ).orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Desarrollador no encontrado con ID: "
                                            + dto.getDesarrolladorId()
                            )
                    );

            existente.setDesarrollador(
                    desarrollador
            );
        }

        Producto actualizado =
                productoRepository.save(existente);

        ProductoDTO respuesta =
                productoMapper.toDTO(actualizado);

        enriquecerInformacionComercial(
                List.of(respuesta)
        );

        return respuesta;
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {

        Producto producto =
                productoRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Producto no encontrado con el ID: " + id
                                )
                        );

        /*
         * Eliminación lógica.
         */
        producto.setActivo(false);

        productoRepository.save(producto);
    }
}

