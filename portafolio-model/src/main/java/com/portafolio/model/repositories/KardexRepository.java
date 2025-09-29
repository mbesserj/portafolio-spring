
package com.portafolio.model.repositories;

import com.portafolio.model.entities.KardexEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface KardexRepository extends JpaRepository<KardexEntity, Long> {
    
    @Query("SELECT k FROM KardexEntity k " +
           "WHERE k.empresa.id = :empresaId " +
           "AND k.instrumento.id = :instrumentoId " +
           "ORDER BY k.fechaTransaccion ASC")
    List<KardexEntity> findKardexByEmpresaAndInstrumento(
        @Param("empresaId") Long empresaId,
        @Param("instrumentoId") Long instrumentoId
    );
    
    @Query("SELECT k FROM KardexEntity k " +
           "WHERE k.claveAgrupacion = :clave " +
           "AND k.cantidadDisponible > 0 " +
           "ORDER BY k.fechaTransaccion ASC")
    List<KardexEntity> findDisponiblesByClaveAgrupacion(@Param("clave") String clave);
}