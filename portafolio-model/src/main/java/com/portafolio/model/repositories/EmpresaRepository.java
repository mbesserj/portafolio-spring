package com.portafolio.model.repositories;

import com.portafolio.model.entities.EmpresaEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EmpresaRepository extends JpaRepository<EmpresaEntity, Long> {
    
    Optional<EmpresaEntity> findByRut(String rut);
    
    List<EmpresaEntity> findByRazonSocialContainingIgnoreCase(String razonSocial);
    
    @Query("SELECT e FROM EmpresaEntity e JOIN FETCH e.custodios WHERE e.id = :id")
    Optional<EmpresaEntity> findByIdWithCustodios(@Param("id") Long id);
    
    boolean existsByRut(String rut);
}