package com.devsv.pixelworks_api.services;

import com.devsv.pixelworks_api.dto.RealizarCompraDTO;
import com.devsv.pixelworks_api.dto.DetalleItemDTO;
import com.devsv.pixelworks_api.dto.CompraResponseDTO;
import com.devsv.pixelworks_api.dto.DetalleCompraResponseDTO;
import com.devsv.pixelworks_api.entities.*;
import com.devsv.pixelworks_api.enums.EstadoClave;
import com.devsv.pixelworks_api.exceptions.ResourceNotFoundException;
import com.devsv.pixelworks_api.interfaces.ICompraService;
import com.devsv.pixelworks_api.interfaces.IOfertaService;
import com.devsv.pixelworks_api.mappers.CompraMapper;
import com.devsv.pixelworks_api.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CompraService implements ICompraService {

    private final CompraRepository compraRepository;
    private final DetalleCompraRepository detalleCompraRepository;
    private final ProductoRepository productoRepository;
    private final MetodoPagoRepository metodoPagoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ClaveActivacionRepository claveActivacionRepository;
    private final CompraMapper compraMapper;
    private final IOfertaService ofertaService;

    @Override
    @Transactional
    public CompraResponseDTO procesarCompra(RealizarCompraDTO dto, Integer usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));

        MetodoPago metodoPago = metodoPagoRepository.findById(dto.getMetodoPagoId())
                .orElseThrow(() -> new ResourceNotFoundException("Método de pago no válido."));

        Compra compra = new Compra();
        compra.setUsuario(usuario);
        compra.setMetodoPago(metodoPago);
        compra.setFechaVenta(LocalDateTime.now());
        compra.setTotal(BigDecimal.ZERO);

        Compra compraGuardada = compraRepository.save(compra);

        BigDecimal totalCompra = BigDecimal.ZERO;
        List<DetalleCompraResponseDTO> listaDetallesDTO = new ArrayList<>();

        for (DetalleItemDTO item : dto.getItems()) {
            Producto producto = productoRepository.findById(item.getProductoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto ID " + item.getProductoId() + " no existe."));

            if (!producto.getActivo()) {
                throw new IllegalArgumentException("El juego " + producto.getNombre() + " ya no está disponible.");
            }

            List<ClaveActivacion> clavesDisponibles = claveActivacionRepository
                    .findByProductoIdAndEstado(producto.getId(), EstadoClave.DISPONIBLE, PageRequest.of(0, item.getCantidad()));

            if (clavesDisponibles.size() < item.getCantidad()) {
                throw new IllegalArgumentException("Stock insuficiente para el juego: " + producto.getNombre());
            }

            BigDecimal subTotal = producto.getPrecio().multiply(BigDecimal.valueOf(item.getCantidad()));
            totalCompra = totalCompra.add(subTotal);

            DetalleCompra detalle = new DetalleCompra();
            detalle.setCompra(compraGuardada);
            detalle.setProducto(producto);
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitario(producto.getPrecio());
            detalle.setSubTotal(subTotal);

            DetalleCompra detalleGuardado = detalleCompraRepository.save(detalle);

            List<String> codigosEntregados = new ArrayList<>();
            for (ClaveActivacion clave : clavesDisponibles) {
                clave.setEstado(EstadoClave.VENDIDA);
                clave.setDetalleCompra(detalleGuardado);
                claveActivacionRepository.save(clave);
                codigosEntregados.add(clave.getCodigo());
            }

            DetalleCompraResponseDTO detalleDTO = compraMapper.toDetalleResponseDTO(detalleGuardado);
            detalleDTO.setClaves(codigosEntregados);
            listaDetallesDTO.add(detalleDTO);
        }

        compraGuardada.setTotal(totalCompra);
        compraRepository.save(compraGuardada);

        CompraResponseDTO response = compraMapper.toResponseDTO(compraGuardada);
        response.setDetalles(listaDetallesDTO);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompraResponseDTO> obtenerMisCompras(Integer usuarioId) {
        List<Compra> misCompras = compraRepository.findByUsuarioId(usuarioId);

        return misCompras.stream().map(compra -> {
            CompraResponseDTO compraDTO = compraMapper.toResponseDTO(compra);

            List<DetalleCompra> detallesDeLaCompra = detalleCompraRepository.findByCompraId(compra.getId());

            List<DetalleCompraResponseDTO> detallesDTO = detallesDeLaCompra.stream().map(detalle -> {
                DetalleCompraResponseDTO detalleDTO = compraMapper.toDetalleResponseDTO(detalle);

                List<String> codigos = claveActivacionRepository.findByDetalleCompraId(detalle.getId())
                        .stream()
                        .map(ClaveActivacion::getCodigo)
                        .toList();

                detalleDTO.setClaves(codigos);
                return detalleDTO;
            }).toList();

            compraDTO.setDetalles(detallesDTO);
            return compraDTO;
        }).toList();
    }
}