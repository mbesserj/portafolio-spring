package com.portafolio.model.repositories;

import com.portafolio.model.entities.CargaTransaccionEntity;
import com.portafolio.model.utiles.Pk;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CargaTransaccionRepository extends JpaRepository<CargaTransaccionEntity, Pk> {
    
    List<CargaTransaccionEntity> findByProcesado(boolean procesado);
    
    @Query("SELECT ct FROM CargaTransaccionEntity ct WHERE ct.id.fechaTransaccion = :fecha")
    List<CargaTransaccionEntity> findByFecha(@Param("fecha") LocalDate fecha);
    
    @Query("SELECT ct FROM CargaTransaccionEntity ct WHERE ct.procesado = false " +
           "ORDER BY ct.id.fechaTransaccion ASC")
    List<CargaTransaccionEntity> findPendientesProcesar();
}