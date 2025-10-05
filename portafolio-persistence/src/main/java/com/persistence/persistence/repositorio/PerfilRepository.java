package com.portafolio.persistence.repositorio;

import com.portafolio.model.entities.PerfilEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface PerfilRepository extends JpaRepository<PerfilEntity, Long> {

    // ==================== Búsquedas básicas ====================
    Optional<PerfilEntity> findByPerfilIgnoreCase(String perfil);
    boolean existsByPerfilIgnoreCase(String perfil);
    List<PerfilEntity> findAllByOrderByPerfilAsc();

    // ==================== Búsquedas por relación ====================
    @Query("SELECT p FROM PerfilEntity p WHERE SIZE(p.usuarios) > 0")
    List<PerfilEntity> findPerfilesConUsuarios();

    // ==================== Búsqueda por múltiples nombres ====================
    @Query("SELECT p FROM PerfilEntity p WHERE p.perfil IN :nombres")
    Set<PerfilEntity> findByPerfilIn(@Param("nombres") Set<String> nombres);
    
    // ==================== Consulta de rendimiento ====================
    @Query("SELECT p FROM PerfilEntity p LEFT JOIN FETCH p.usuarios WHERE p.id = :id")
    Optional<PerfilEntity> findByIdWithUsuarios(@Param("id") Long id);
}