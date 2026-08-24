package com.devsv.pixelworks_api.services;

import com.devsv.pixelworks_api.entities.Rol;
import com.devsv.pixelworks_api.entities.Usuario;
import com.devsv.pixelworks_api.exceptions.ResourceNotFoundException;
import com.devsv.pixelworks_api.repository.RolRepository;
import com.devsv.pixelworks_api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;

    @Transactional
    public void cambiarRol(Integer idUsuarioTarget, Integer idNuevoRol, Integer idAdminLogueado) {

        Usuario usuario = usuarioRepository.findById(idUsuarioTarget)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + idUsuarioTarget));

        Rol nuevoRol = rolRepository.findById(idNuevoRol)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado en el sistema"));


        if (usuario.getRol().getId().equals(idNuevoRol)) {
            throw new IllegalArgumentException("El usuario ya tiene asignado este rol.");
        }


        if (usuario.getId().equals(idAdminLogueado) && !nuevoRol.getNombre().equals("ADMIN")) {
            throw new IllegalArgumentException("Operación denegada: No puedes revocar tu propio rol de administrador.");
        }

        usuario.setRol(nuevoRol);
        usuarioRepository.save(usuario);
    }
}