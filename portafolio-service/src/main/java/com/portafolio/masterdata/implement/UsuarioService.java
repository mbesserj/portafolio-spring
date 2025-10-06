package com.portafolio.masterdata.implement;

import com.portafolio.model.entities.PerfilEntity;
import com.portafolio.model.entities.UsuarioEntity;
import com.portafolio.persistence.repositorio.PerfilRepository;
import com.portafolio.persistence.repositorio.UsuarioRepository;
import com.portafolio.ui.util.PasswordSecurityConfig;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioService.class);

    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public boolean hayUsuariosRegistrados() {
        return usuarioRepository.count() > 0;
    }

    @Transactional
    public UserRegistrationResult crearUsuarioAdmin(String usuario, String contrasena) {
        if (hayUsuariosRegistrados()) {
            return UserRegistrationResult.failure("Ya existen usuarios en el sistema.");
        }
        return registrarNuevoUsuario(usuario, contrasena, null, "ADMIN");
    }

    @Transactional
    public UserRegistrationResult registrarNuevoUsuario(String usuario, String password, String email) {
        return registrarNuevoUsuario(usuario, password, email, "CONSULTA");
    }

    private UserRegistrationResult registrarNuevoUsuario(String usuario, String password, String email, String defaultProfile) {
        // Validaciones
        if (usuario == null || usuario.trim().isEmpty()) {
            return UserRegistrationResult.failure("El nombre de usuario no puede estar vacío.");
        }
        if (usuarioRepository.existsByUsuario(usuario.trim())) {
            return UserRegistrationResult.failure("El nombre de usuario ya existe.");
        }
        var validation = PasswordSecurityConfig.validatePassword(password);
        if (!validation.isValid()) {
            return UserRegistrationResult.failure(validation.getErrorMessage());
        }

        try {
            // Buscar o crear el perfil por defecto
            PerfilEntity perfil = perfilRepository.findByPerfilIgnoreCase(defaultProfile)
                    .orElseGet(() -> {
                        PerfilEntity newPerfil = new PerfilEntity();
                        newPerfil.setPerfil(defaultProfile);
                        return perfilRepository.save(newPerfil);
                    });

            UsuarioEntity nuevoUsuario = new UsuarioEntity();
            nuevoUsuario.setUsuario(usuario.trim());
            nuevoUsuario.setPassword(passwordEncoder.encode(password));
            nuevoUsuario.setCorreo(email != null ? email.trim() : null);
            nuevoUsuario.setPerfiles(new HashSet<>(Collections.singletonList(perfil)));

            UsuarioEntity usuarioGuardado = usuarioRepository.save(nuevoUsuario);
            logger.info("Usuario '{}' creado exitosamente con perfil '{}'", usuarioGuardado.getUsuario(), defaultProfile);
            return UserRegistrationResult.success(usuarioGuardado);

        } catch (Exception e) {
            logger.error("Error al registrar nuevo usuario: {}", usuario, e);
            return UserRegistrationResult.failure("Error interno al crear el usuario.");
        }
    }

    @Transactional(readOnly = true)
    public List<UsuarioEntity> obtenerUsuariosPorPerfil(PerfilEntity perfil) {
        if (perfil == null || perfil.getPerfil() == null) {
            return Collections.emptyList();
        }
        return usuarioRepository.findByPerfil(perfil.getPerfil());
    }

    @Transactional
    public void cambiarPerfilDeUsuario(Long usuarioId, PerfilEntity perfilOrigen, PerfilEntity perfilDestino) {
        UsuarioEntity usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + usuarioId));

        Set<PerfilEntity> perfiles = usuario.getPerfiles();
        if (perfilOrigen != null) {
            perfiles.remove(perfilOrigen);
        }
        perfiles.add(perfilDestino);

        usuarioRepository.save(usuario);
        logger.info("Perfil de usuario '{}' actualizado a '{}'", usuario.getUsuario(), perfilDestino.getPerfil());
    }

    @Transactional
    public void desactivarUsuario(Long usuarioId) {
        UsuarioEntity usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + usuarioId));
        
        usuario.setFechaInactivo(LocalDate.now());
        usuarioRepository.save(usuario);
        logger.info("Usuario '{}' desactivado.", usuario.getUsuario());
    }
    
    // La clase interna de resultado se puede mantener igual
    public static class UserRegistrationResult {
        private final boolean success;
        private final String errorMessage;
        private final UsuarioEntity user;

        private UserRegistrationResult(boolean success, String errorMessage, UsuarioEntity user) {
            this.success = success;
            this.errorMessage = errorMessage;
            this.user = user;
        }

        public static UserRegistrationResult success(UsuarioEntity user) {
            return new UserRegistrationResult(true, null, user);
        }

        public static UserRegistrationResult failure(String errorMessage) {
            return new UserRegistrationResult(false, errorMessage, null);
        }
        
        public boolean isSuccess() { return success; }
        public String getErrorMessage() { return errorMessage; }
        public UsuarioEntity getUser() { return user; }
    }
}