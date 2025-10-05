
package com.portafolio.persistence.repositorio;

import com.portafolio.model.entities.GrupoEmpresaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GrupoEmpresaRepository extends JpaRepository<GrupoEmpresaEntity, Long> {

    // ==================== Búsquedas básicas ====================
    Optional<GrupoEmpresaEntity> findByNombreGrupo(String nombreGrupo);
    List<GrupoEmpresaEntity> findByNombreGrupoContainingIgnoreCase(String nombre);
    boolean existsByNombreGrupo(String nombreGrupo);

    // ==================== Búsquedas por relaciones ====================
    @Query("SELECT g FROM GrupoEmpresaEntity g WHERE SIZE(g.empresas) > 0")
    List<GrupoEmpresaEntity> findGruposConEmpresas();

    @Query("SELECT g FROM GrupoEmpresaEntity g WHERE SIZE(g.empresas) = 0")
    List<GrupoEmpresaEntity> findGruposSinEmpresas();
    
    // ==================== Consultas de rendimiento (Evitar N+1) ====================
    @Query("SELECT DISTINCT g FROM GrupoEmpresaEntity g LEFT JOIN FETCH g.empresas")
    List<GrupoEmpresaEntity> findAllWithEmpresas();

    @Query("SELECT g FROM GrupoEmpresaEntity g LEFT JOIN FETCH g.empresas e WHERE g.id = :id")
    Optional<GrupoEmpresaEntity> findByIdWithEmpresas(@Param("id") Long id);
    
    // ==================== Consultas de estadísticas ====================
    @Query("SELECT g.nombreGrupo, SIZE(g.empresas) FROM GrupoEmpresaEntity g ORDER BY g.nombreGrupo")
    List<Object[]> contarEmpresasPorGrupo();
}