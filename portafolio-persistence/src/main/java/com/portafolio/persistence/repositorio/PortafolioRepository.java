package com.portafolio.persistence.repositorio;

import com.portafolio.model.entities.PortafolioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PortafolioRepository extends JpaRepository<PortafolioEntity, Long> {

    // ==================== Búsquedas básicas ====================
    Optional<PortafolioEntity> findByNombrePortafolio(String nombrePortafolio);
    List<PortafolioEntity> findByNombrePortafolioContainingIgnoreCase(String nombre);
    boolean existsByNombrePortafolio(String nombrePortafolio);

    // ==================== Búsquedas por relación ====================
    @Query("SELECT p FROM PortafolioEntity p WHERE SIZE(p.portafolioTransacciones) > 0")
    List<PortafolioEntity> findPortafoliosConTransacciones();
    
    // ==================== Consulta de rendimiento (Evitar N+1) ====================
    @Query("SELECT p FROM PortafolioEntity p LEFT JOIN FETCH p.portafolioTransacciones WHERE p.id = :id")
    Optional<PortafolioEntity> findByIdWithTransacciones(@Param("id") Long id);
}