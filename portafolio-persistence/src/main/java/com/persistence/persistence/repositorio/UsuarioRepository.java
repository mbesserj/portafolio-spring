package com.portafolio.persistence.repositorio;

import com.portafolio.model.entities.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Long> {

    // ==================== Búsquedas para Seguridad y Lógica ====================
    Optional<UsuarioEntity> findByUsuario(String usuario);
    Optional<UsuarioEntity> findByCorreo(String correo);
    boolean existsByUsuario(String usuario);
    boolean existsByCorreo(String correo);

    // ==================== Búsquedas por estado o perfil ====================
    List<UsuarioEntity> findByFechaInactivoIsNull(); // Encuentra usuarios activos
    
    @Query("SELECT u FROM UsuarioEntity u JOIN u.perfiles p WHERE p.perfil = :perfilNombre")
    List<UsuarioEntity> findByPerfil(@Param("perfilNombre") String perfilNombre);

    // ==================== Consulta de rendimiento (CRUCIAL para Seguridad) ====================
    /**
     * Carga un usuario y sus perfiles en una sola consulta para evitar N+1.
     * Ideal para ser usado por Spring Security (UserDetailsService).
     */
    @Query("SELECT u FROM UsuarioEntity u LEFT JOIN FETCH u.perfiles WHERE u.usuario = :usuario")
    Optional<UsuarioEntity> findByUsuarioWithPerfiles(@Param("usuario") String usuario);
}