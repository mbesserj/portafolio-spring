package com.portafolio.model.interfaces;

import com.portafolio.model.entities.EmpresaEntity;
import com.portafolio.model.entities.SaldoKardexEntity;
import com.portafolio.model.entities.CustodioEntity;
import com.portafolio.model.entities.InstrumentoEntity;
import com.portafolio.model.entities.SaldoEntity;
import com.portafolio.model.entities.SaldosDiariosEntity;
import com.portafolio.model.dto.ResumenSaldoDto;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Contrato UNIFICADO para TODAS las operaciones de consulta sobre Saldos.
 * Consolida las responsabilidades de Saldo, SaldosDiarios y SaldoKardex.
 */
public interface SaldoApi {

    // --- Métodos de SaldosDiarios (del SaldosDiariosRepositoryImpl original) ---
    
    /**
     * 
     * @param empresa
     * @param custodio
     * @param instrumento
     * @param cuenta
     * @param fecha
     * @return 
     */
    Optional<SaldosDiariosEntity> findLastBeforeDate(EmpresaEntity empresa, CustodioEntity custodio, InstrumentoEntity instrumento, String cuenta, LocalDate fecha);
    
    /**
     * 
     * @param empresa
     * @param custodio
     * @param instrumento
     * @param cuenta
     * @param fechaInicio
     * @param fechaFin
     * @return 
     */
    List<SaldosDiariosEntity> findAllByGroupAndDateRange(EmpresaEntity empresa, CustodioEntity custodio, InstrumentoEntity instrumento, String cuenta, LocalDate fechaInicio, LocalDate fechaFin);
    
    // --- Métodos de Saldo General (del SaldoRepositoryImpl original) ---
    /**
     * 
     * @param empresaId
     * @param custodioId
     * @param instrumentoId
     * @param cuenta
     * @param fechaObjetivo
     * @return 
     */
    Optional<SaldoEntity> buscarSaldoMasCercanoAFecha(Long empresaId, Long custodioId, Long instrumentoId, String cuenta, LocalDate fechaObjetivo);
    
    /**
     * 
     * @param empresaId
     * @param custodioId
     * @param cuenta
     * @param instrumentoId
     * @return 
     */
    Optional<SaldoEntity> obtenerUltimoSaldo(Long empresaId, Long custodioId, String cuenta, Long instrumentoId);
    
    /**
     * 
     * @param empresaId
     * @param custodioId
     * @return 
     */
    List<ResumenSaldoDto> obtenerResumenSaldosAgregados(Long empresaId, Long custodioId);
    
    /**
     * 
     * @param empresaId
     * @param custodioId
     * @param instrumentoId
     * @param cuenta
     * @return 
     */
    Optional<SaldoKardexEntity> findByGrupo(Long empresaId, Long custodioId, Long instrumentoId, String cuenta);
   
}