
package com.portafolio.model.repositories;

import com.portafolio.model.entities.CuentaEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CuentaRepository extends JpaRepository<CuentaEntity, Long> {
    
    Optional<CuentaEntity> findByCuentaAndEmpresa_IdAndCustodio_Id(
        String cuenta, Long empresaId, Long custodioId);
    
    List<CuentaEntity> findByEmpresa_Id(Long empresaId);
    
    boolean existsByCuentaAndEmpresa_IdAndCustodio_Id(
        String cuenta, Long empresaId, Long custodioId);
}