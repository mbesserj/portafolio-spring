
package com.portafolio.model.repositories;

import com.portafolio.model.entities.AuditoriaEntity;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface AuditoriaRepository extends JpaRepository<AuditoriaEntity, Long> {
    
    List<AuditoriaEntity> findByArchivoOrigen(String archivoOrigen);
    
    List<AuditoriaEntity> findByFechaAuditoriaBetween(LocalDate inicio, LocalDate fin);
    
    @Query("SELECT a FROM AuditoriaEntity a WHERE a.tipoEntidad = :tipo " +
           "ORDER BY a.fechaAuditoria DESC")
    List<AuditoriaEntity> findByTipoEntidadOrdered(@Param("tipo") String tipo);
}
