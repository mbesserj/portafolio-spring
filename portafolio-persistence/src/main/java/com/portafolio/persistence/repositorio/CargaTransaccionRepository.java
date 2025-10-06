package com.portafolio.persistence.repositorio;

import com.portafolio.model.dto.CargaTransaccionDto;
import com.portafolio.model.entities.CargaTransaccionEntity;
import com.portafolio.model.utiles.Pk;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CargaTransaccionRepository extends JpaRepository<CargaTransaccionEntity, Pk> {

    /**
     * Busca todas las transacciones no procesadas (retorna entidades)
     *
     * @return
     */
    List<CargaTransaccionEntity> findByProcesadoFalse();

    /**
     * Busca un lote de transacciones NO PROCESADAS y las devuelve como DTOs.
     * Esta es la migración del método findUnprocessedDtoBatch del DAO antiguo.
     *
     * @param offset
     * @param batchSize
     * @return
     */
    @Query("""
        SELECT new com.model.dto.CargaTransaccion(
            c.id.transactionDate,
            c.id.rowNum,
            c.id.tipoClase,
            c.razonSocial,
            c.rut,
            c.custodioNombre,
            c.cuenta,
            c.instrumentoNemo,
            c.instrumentoNombre,
            c.tipoMovimiento,
            c.monto,
            c.montoTotal,
            c.moneda,
            c.movimientoCaja,
            c.montoClp,
            c.montoUsd,
            c.cantidad,
            c.precio,
            c.comisiones,
            c.gastos,
            c.iva,
            c.folio
        )
        FROM CargaTransaccionEntity c
        WHERE c.procesado = false
        ORDER BY c.id.transactionDate, c.id.rowNum
        """)
    List<CargaTransaccionDto> findUnprocessedDtoBatch(
            @Param("offset") int offset,
            @Param("batchSize") int batchSize
    );

    /**
     * Alternativa usando Pageable (más idiomático en Spring Data)
     *
     * @param pageable
     * @return
     */
    @Query("""
        SELECT new com.model.dto.CargaTransaccion(
            c.id.transactionDate,
            c.id.rowNum,
            c.id.tipoClase,
            c.razonSocial,
            c.rut,
            c.custodioNombre,
            c.cuenta,
            c.instrumentoNemo,
            c.instrumentoNombre,
            c.tipoMovimiento,
            c.monto,
            c.montoTotal,
            c.moneda,
            c.movimientoCaja,
            c.montoClp,
            c.montoUsd,
            c.cantidad,
            c.precio,
            c.comisiones,
            c.gastos,
            c.iva,
            c.folio
        )
        FROM CargaTransaccionEntity c
        WHERE c.procesado = false
        """)
    Page<CargaTransaccionDto> findUnprocessedDtoPage(Pageable pageable);

    /**
     * Verifica si existe una transacción por su clave de negocio Migración de
     * existsByUniqueBusinessFields
     *
     * @param transactionId
     * @param fileOrigin
     * @param transactionDate
     * @return
     */
    boolean existsByIdTransactionIdAndIdTransactionDateAndIdFileOrigin(
            String transactionId,
            LocalDate transactionDate,
            String fileOrigin
    );

    /**
     * Cuenta transacciones no procesadas
     *
     * @return
     */
    long countByProcesadoFalse();

    /**
     * Limpia la tabla de carga (usar con @Transactional y @Modifying)
     */
    @Modifying
    @Query("DELETE FROM CargaTransaccionEntity")
    void clearTable();

    /**
     * Busca transacciones por fecha
     *
     * @param transactionDate
     * @return
     */
    List<CargaTransaccionEntity> findByIdTransactionDate(LocalDate transactionDate);

    /**
     * Busca transacciones por rango de fechas
     *
     * @param fechaInicio
     * @param fechaFin
     * @return
     */
    @Query("SELECT c FROM CargaTransaccionEntity c "
            + "WHERE c.id.transactionDate BETWEEN :fechaInicio AND :fechaFin")
    List<CargaTransaccionEntity> findByDateRange(
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin
    );

    /**
     * Marca transacciones como procesadas
     *
     * @param ids
     */
    @Modifying
    @Query("UPDATE CargaTransaccionEntity c SET c.procesado = true "
            + "WHERE c.id IN :ids")
    void markAsProcessed(@Param("ids") List<Pk> ids);

    /**
     * Busca transacciones por custodio
     *
     * @param custodioNombre
     * @return
     */
    List<CargaTransaccionEntity> findByCustodioNombre(String custodioNombre);

    /**
     * Busca transacciones por RUT
     *
     * @param rut
     * @return
     */
    List<CargaTransaccionEntity> findByRut(String rut);

    /**
     * Busca registros no procesados por fecha
     */
    List<CargaTransaccionEntity> findByIdFechaTransaccionAndProcesadoFalse(LocalDate fecha);

    /**
     * Cuenta registros procesados
     */
    long countByProcesadoTrue();

    /**
     * Reinicia el estado de procesamiento por rango de fechas
     */
    @Modifying
    @Query("UPDATE CargaTransaccionEntity c SET c.procesado = false WHERE c.id.fechaTransaccion BETWEEN :fechaDesde AND :fechaHasta")
    int resetProcesadoByFechaRange(@Param("fechaDesde") LocalDate fechaDesde, @Param("fechaHasta") LocalDate fechaHasta);

    /**
     * Elimina registros procesados antes de una fecha
     */
    @Modifying
    @Query("DELETE FROM CargaTransaccionEntity c WHERE c.procesado = true AND c.id.fechaTransaccion < :fechaHasta")
    int deleteByProcesadoTrueAndIdFechaTransaccionBefore(@Param("fechaHasta") LocalDate fechaHasta);

    /**
     * Busca registros no procesados por fecha
     */
    List<CargaTransaccionEntity> findByFechaTransaccionAndProcesadoFalse(LocalDate fecha);

    /**
     * Elimina registros procesados antes de una fecha
     */
    @Modifying
    @Query("DELETE FROM CargaTransaccionEntity c WHERE c.procesado = true AND c.fechaTransaccion < :fechaHasta")
    int deleteByProcesadoTrueAndFechaTransaccionBefore(@Param("fechaHasta") LocalDate fechaHasta);
    
}
