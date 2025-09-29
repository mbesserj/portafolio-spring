
package com.portafolio.model.repositories;

import com.portafolio.model.entities.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustodioRepository extends JpaRepository<CustodioEntity, Long> {
    
    Optional<CustodioEntity> findByNombreCustodio(String nombreCustodio);
    
    @Query("SELECT c FROM CustodioEntity c JOIN FETCH c.empresas WHERE c.id = :id")
    Optional<CustodioEntity> findByIdWithEmpresas(@Param("id") Long id);
    
    boolean existsByNombreCustodio(String nombreCustodio);
}