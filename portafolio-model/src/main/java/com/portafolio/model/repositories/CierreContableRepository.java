
package com.portafolio.model.repositories;

import com.portafolio.model.entities.CierreContableEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface CierreContableRepository extends JpaRepository<CierreContableEntity, Long> {
    
    Optional<CierreContableEntity> findByEjercicioAndEmpresa_IdAndCustodio_IdAndInstrumento_Id(
        int ejercicio, Long empresaId, Long custodioId, Long instrumentoId);
    
    List<CierreContableEntity> findByEjercicio(int ejercicio);
    
    @Query("SELECT cc FROM CierreContableEntity cc " +
           "WHERE cc.ejercicio = :ejercicio AND cc.empresa.id = :empresaId")
    List<CierreContableEntity> findByEjercicioAndEmpresa(
        @Param("ejercicio") int ejercicio,
        @Param("empresaId") Long empresaId
    );
}