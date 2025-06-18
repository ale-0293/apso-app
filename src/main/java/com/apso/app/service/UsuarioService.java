package com.apso.app.service;

import com.apso.app.model.Usuario;
import com.apso.app.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    /**
     * Retorna el Usuario correspondiente al OidcUser (Auth0).
     * Si no existe, lo crea en la base de datos.
     */
    public Usuario obtenerOcrearUsuarioDesdeOidc(OidcUser oidcUser) {
        String auth0Id = oidcUser.getSubject(); // Claim 'sub'
        Optional<Usuario> usuarioOpt = usuarioRepository.findByAuth0Id(auth0Id);

        if (usuarioOpt.isPresent()) {
            return usuarioOpt.get();
        }

        // Crear nuevo usuario
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setAuth0Id(auth0Id);
        nuevoUsuario.setNombre(oidcUser.getFullName());
        nuevoUsuario.setEmail(oidcUser.getEmail());

        return usuarioRepository.save(nuevoUsuario);
    }

    /**
     * Retorna el Usuario autenticado desde un HttpServletRequest.
     */
    public Usuario obtenerUsuarioDesdeRequest(HttpServletRequest request) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof OidcUser oidcUser) {
            return obtenerOcrearUsuarioDesdeOidc(oidcUser);
        } else {
            throw new RuntimeException("Usuario no autenticado o no es un OidcUser");
        }
    }
}
