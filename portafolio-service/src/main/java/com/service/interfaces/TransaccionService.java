package com.service.interfaces;

import com.model.dto.TransaccionDto;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransaccionService {

    /**
     * 
     * @param id
     * @return Busca una transacción por su ID, cargando todas sus relaciones.
     */
    TransaccionDto findByIdWithRelations(Long id);

    /**
     * 
     * @param folio
     * @param fecha
     * @param cuenta
     * @return usca una transacción por su clave de negocio.
     */
    Optional<TransaccionDto> findByBusinessKey(String folio, LocalDate fecha, String cuenta);

    /**
     * 
     * @return Devuelve todas las transacciones ordenadas por fecha.
     */
    List<TransaccionDto> findAllOrderedByDate();

    /**
     * 
     * @return Devuelve todas las transacciones no costeadas, ordenadas por fecha.
     */
    List<TransaccionDto> findUncostedTransactions();
    
    /**
     * 
     * @return Devuelve todas las transacciones marcadas para revisión.
     */
    List<TransaccionDto> findTransactionsForReview();
}