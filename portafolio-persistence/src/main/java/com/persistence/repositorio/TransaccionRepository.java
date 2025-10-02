package com.persistence.repositorio;

import com.model.entities.TransaccionEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransaccionRepository extends JpaRepository<TransaccionEntity, Long> {

    /**
     * 
     * @param folio
     * @param fecha
     * @param cuenta
     * @return Entrega la transacción asociada a una cuenta, fecha y folio.
     */
    Optional<TransaccionEntity> findByFolioAndFechaAndCuenta(String folio, LocalDate fecha, String cuenta);
    
    /**
     * 
     * @param id
     * @return 
     */
    Optional<TransaccionEntity> findByIdWithRelations(Long id);
    
    /**
     * 
     * @return entrega una lista ordenada de transacciones.
     */
    List<TransaccionEntity> findAllByOrderByFechaAsc();
    
    /**
     * 
     * @return entrega una lista de transacciones no costeadas.
     */
    List<TransaccionEntity> findByCosteadoFalseOrderByFechaAsc();
    
    /**
     * 
     * @return entrega una lista de transaccciones marcadas para revisión ordenadas por fecha.
     */
    List<TransaccionEntity> findByParaRevisionTrueOrderByFechaAsc();
    
    /**
     * 
     * @param id
     * @return retorna true si alguna transaccion tiene asignado el tipo de movimiento.
     */
    boolean existsByTipoMovimientoId(Long id);
    
}
