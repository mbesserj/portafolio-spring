package com.portafolio.persistence.repositorio;

import com.portafolio.model.entities.TransaccionEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
     * @return entrega una lista de transaccciones marcadas para revisión
     * ordenadas por fecha.
     */
    List<TransaccionEntity> findByParaRevisionTrueOrderByFechaAsc();

    /**
     *
     * @param id
     * @return retorna true si alguna transaccion tiene asignado el tipo de
     * movimiento.
     */
    boolean existsByTipoMovimientoId(Long id);

    /**
     * Busca transacciones para revisión por empresa
     */
    List<TransaccionEntity> findByEmpresaIdAndParaRevisionTrueOrderByFechaTransaccionAsc(Long empresaId);

    /**
     * Busca transacciones costeadas por grupo con paginación
     */
    Page<TransaccionEntity> findByEmpresaIdAndCustodioIdAndInstrumentoIdAndCuentaAndCosteadoTrue(
            Long empresaId, Long custodioId, Long instrumentoId, String cuenta, Pageable pageable);

    /**
     * Busca transacciones por grupo con paginación
     */
    Page<TransaccionEntity> findByEmpresaIdAndCustodioIdAndInstrumentoIdAndCuenta(
            Long empresaId, Long custodioId, Long instrumentoId, String cuenta, Pageable pageable);

    /**
     * Busca transacciones para revisión por grupo
     */
    List<TransaccionEntity> findByEmpresaIdAndCustodioIdAndInstrumentoIdAndCuentaAndParaRevisionTrueOrderByFechaTransaccionAsc(
            Long empresaId, Long custodioId, Long instrumentoId, String cuenta);

    /**
     * Busca transacción con todas las relaciones
     */
    @Query("""
        SELECT t FROM TransaccionEntity t
        LEFT JOIN FETCH t.empresa
        LEFT JOIN FETCH t.custodio
        LEFT JOIN FETCH t.instrumento
        LEFT JOIN FETCH t.tipoMovimiento tm
        LEFT JOIN FETCH tm.movimientoContable
        WHERE t.id = :id
        """)
    Optional<TransaccionEntity> findByIdWithAllRelations(@Param("id") Long id);
}