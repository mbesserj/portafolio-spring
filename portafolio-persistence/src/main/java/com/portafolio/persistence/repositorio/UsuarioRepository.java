package com.portafolio.persistence.repositorio;

import com.portafolio.model.entities.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Long> {

    Optional<UsuarioEntity> findByUsuario(String usuario);

    Optional<UsuarioEntity> findByCorreo(String correo);

    boolean existsByUsuario(String usuario);

    boolean existsByCorreo(String correo);

    List<UsuarioEntity> findByFechaInactivoIsNull(); 

    @Query("SELECT u FROM UsuarioEntity u JOIN u.perfiles p WHERE p.perfil = :perfilNombre")
    List<UsuarioEntity> findByPerfil(@Param("perfilNombre") String perfilNombre);

    /**
     * Carga un usuario y sus perfiles en una sola consulta para evitar N+1.
     * Ideal para ser usado por Spring Security (UserDetailsService).
     */
    @Query("SELECT u FROM UsuarioEntity u LEFT JOIN FETCH u.perfiles WHERE u.usuario = :usuario")
    Optional<UsuarioEntity> findByUsuarioWithPerfiles(@Param("usuario") String usuario);
    
    Optional<UsuarioEntity> findByUsuarioIgnoreCaseAndFechaInactivoIsNull(String ususario);
    
}