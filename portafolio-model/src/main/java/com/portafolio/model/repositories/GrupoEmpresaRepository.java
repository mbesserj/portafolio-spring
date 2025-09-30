
package com.portafolio.model.repositories;

import com.portafolio.model.entities.GrupoEmpresaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface GrupoEmpresaRepository extends JpaRepository<GrupoEmpresaEntity, Long> {
    
    Optional<GrupoEmpresaEntity> findByNombreGrupo(String nombreGrupo);
    
    @Query("SELECT g FROM GrupoEmpresaEntity g JOIN FETCH g.empresas WHERE g.id = :id")
    Optional<GrupoEmpresaEntity> findByIdWithEmpresas(@Param("id") Long id);
}
