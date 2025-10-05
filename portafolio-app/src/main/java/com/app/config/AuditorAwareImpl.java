package com.app.config;

import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        // Aquí puedes obtener el usuario actual desde el contexto de seguridad
        // Si implementas Spring Security, usarías:
        // return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
        //         .map(Authentication::getName);
        
        // Por ahora, retornamos un valor por defecto
        return Optional.of("sistema");
    }
}