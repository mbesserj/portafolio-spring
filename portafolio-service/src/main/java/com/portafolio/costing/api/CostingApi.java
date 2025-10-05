package com.portafolio.costing.api;

import com.portafolio.model.dto.AjustePropuestoDto;
import com.portafolio.model.dto.CostingGroupDto;
import com.portafolio.model.dto.KardexDto;
import com.portafolio.model.enums.TipoAjuste;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * API principal del módulo de costeo FIFO.
 * Define el contrato para el procesamiento de costeo.
 */
public interface CostingApi {

    /**
     * Procesa el costeo para todos los grupos pendientes hasta una fecha.
     *
     * @param fechaCorte Fecha hasta la cual procesar
     * @return Número de grupos procesados exitosamente
     */
    int procesarCosteo(LocalDate fechaCorte);

    /**
     * Procesa el costeo para un grupo específico.
     *
     * @param grupo Grupo de costeo (empresa, custodio, instrumento, cuenta)
     * @param fechaCorte Fecha hasta la cual procesar
     */
    void procesarGrupo(CostingGroupDto grupo, LocalDate fechaCorte);

    /**
     * Calcula ajustes pendientes para un grupo.
     *
     * @param empresaId ID de la empresa
     * @param custodioId ID del custodio
     * @param instrumentoId ID del instrumento
     * @param cuenta Número de cuenta
     * @param tipoAjuste Tipo de ajuste a calcular (INGRESO/EGRESO)
     * @return Lista de ajustes propuestos
     */
    List<AjustePropuestoDto> calcularAjustes(
            Long empresaId, 
            Long custodioId, 
            Long instrumentoId,
            String cuenta,
            TipoAjuste tipoAjuste
    );

    /**
     * Obtiene el kardex para un grupo específico.
     *
     * @param empresaId ID de la empresa
     * @param custodioId ID del custodio
     * @param instrumentoId ID del instrumento
     * @param cuenta Número de cuenta
     * @return Lista de movimientos del kardex
     */
    List<KardexDto> obtenerKardex(
            Long empresaId, 
            Long custodioId, 
            Long instrumentoId,
            String cuenta
    );

    /**
     * Obtiene el saldo actual de un grupo.
     *
     * @param empresaId ID de la empresa
     * @param custodioId ID del custodio
     * @param instrumentoId ID del instrumento
     * @param cuenta Número de cuenta
     * @return Cantidad en saldo
     */
    BigDecimal obtenerSaldoActual(
            Long empresaId, 
            Long custodioId, 
            Long instrumentoId,
            String cuenta
    );

    /**
     * Reinicia el costeo para una fecha específica.
     * CUIDADO: Elimina todos los registros de kardex desde la fecha indicada.
     *
     * @param fechaDesde Fecha desde la cual reiniciar
     */
    void reiniciarCosteo(LocalDate fechaDesde);

    /**
     * Reinicia el costeo para un grupo específico.
     *
     * @param empresaId ID de la empresa
     * @param custodioId ID del custodio
     * @param instrumentoId ID del instrumento
     * @param cuenta Número de cuenta
     * @param fechaDesde Fecha desde la cual reiniciar
     */
    void reiniciarGrupo(
            Long empresaId, 
            Long custodioId, 
            Long instrumentoId,
            String cuenta,
            LocalDate fechaDesde
    );
}