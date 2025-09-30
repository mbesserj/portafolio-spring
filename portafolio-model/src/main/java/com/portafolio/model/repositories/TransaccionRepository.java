package com.portafolio.model.repositories;

import com.portafolio.model.entities.TransaccionEntity;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface TransaccionRepository extends JpaRepository<TransaccionEntity, Long> {
    
    List<TransaccionEntity> findByEmpresa_Id(Long empresaId);
    
    List<TransaccionEntity> findByCustodio_Id(Long custodioId);
    
    List<TransaccionEntity> findByFechaTransaccionBetween(LocalDate inicio, LocalDate fin);
    
    @Query("SELECT t FROM TransaccionEntity t " +
           "WHERE t.empresa.id = :empresaId " +
           "AND t.fechaTransaccion BETWEEN :inicio AND :fin")
    List<TransaccionEntity> findByEmpresaAndFecha(
        @Param("empresaId") Long empresaId,
        @Param("inicio") LocalDate inicio,
        @Param("fin") LocalDate fin
    );
    
    @Query("SELECT t FROM TransaccionEntity t " +
           "JOIN FETCH t.empresa " +
           "JOIN FETCH t.custodio " +
           "JOIN FETCH t.instrumento " +
           "WHERE t.id = :id")
    Optional<TransaccionEntity> findByIdWithDetails(@Param("id") Long id);
}