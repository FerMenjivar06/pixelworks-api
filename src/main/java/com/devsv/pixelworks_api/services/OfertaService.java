package com.devsv.pixelworks_api.services;

import com.devsv.pixelworks_api.dto.OfertaDTO;
import com.devsv.pixelworks_api.entities.Oferta;
import com.devsv.pixelworks_api.exceptions.ResourceNotFoundException;
import com.devsv.pixelworks_api.interfaces.IOfertaService;
import com.devsv.pixelworks_api.mappers.OfertaMapper;
import com.devsv.pixelworks_api.repository.OfertaRepository;
import com.devsv.pixelworks_api.repository.OfertaProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OfertaService implements IOfertaService {

    private final OfertaRepository ofertaRepository;
    private final OfertaProductoRepository ofertaProductoRepository;
    private final OfertaMapper ofertaMapper;

    @Override
    public List<OfertaDTO> listarTodas() {
        return ofertaRepository.findAll().stream()
                .map(ofertaMapper::toDTO)
                .toList();
    }

    @Override
    public OfertaDTO obtenerPorId(Integer id) {
        Oferta oferta = ofertaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Oferta no encontrada con el ID: " + id));
        return ofertaMapper.toDTO(oferta);
    }

    @Override
    public OfertaDTO guardar(OfertaDTO dto) {
        if (dto.getFechaInicio().isAfter(dto.getFechaFin())) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin.");
        }
        Oferta oferta = ofertaMapper.toEntity(dto);
        return ofertaMapper.toDTO(ofertaRepository.save(oferta));
    }

    @Override
    public OfertaDTO actualizar(Integer id, OfertaDTO dto) {
        Oferta ofertaExistente = ofertaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Oferta no encontrada con el ID: " + id));

        if (dto.getFechaInicio().isAfter(dto.getFechaFin())) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin.");
        }

        ofertaExistente.setNombre(dto.getNombre());
        ofertaExistente.setPorcentajeDescuento(dto.getPorcentajeDescuento());
        ofertaExistente.setFechaInicio(dto.getFechaInicio());
        ofertaExistente.setFechaFin(dto.getFechaFin());

        return ofertaMapper.toDTO(ofertaRepository.save(ofertaExistente));
    }

    @Override
    public void eliminar(Integer id) {
        if (!ofertaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Oferta no encontrada con el ID: " + id);
        }
        ofertaRepository.deleteById(id);
    }

    @Override
    public BigDecimal obtenerDescuentoActivo(Integer productoId) {
        LocalDate hoy = LocalDate.now();

        return ofertaRepository.findAll().stream()
                .filter(oferta -> !hoy.isBefore(oferta.getFechaInicio()) && !hoy.isAfter(oferta.getFechaFin()))
                .filter(oferta -> ofertaProductoRepository.existsByOfertaIdAndProductoId(oferta.getId(), productoId))
                .map(Oferta::getPorcentajeDescuento)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }
}