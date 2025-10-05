package com.costing.engine;

import com.costing.exception.InsufficientBalanceException;
import com.model.entities.*;
import com.model.enums.TipoEnumsCosteo;
import com.persistence.repositorio.KardexRepository;
import com.persistence.repositorio.SaldoKardexRepository;
import com.persistence.repositorio.TipoMovimientoRepository;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Procesador especializado para un grupo de costeo específico.
 * Maneja el estado y el flujo FIFO para todas las transacciones de un grupo.
 */
@Slf4j
public class CostingGroupProcessor {

    // Dependencias
    private final EntityManager entityManager;
    private final KardexRepository kardexRepository;
    private final SaldoKardexRepository saldoKardexRepository;
    
    // Handlers especializados
    private final IngresoHandler ingresoHandler;
    private final EgresoHandler egresoHandler;

    // Estado del grupo
    private final String claveAgrupacion;
    private final List<TransaccionEntity> transacciones;
    private BigDecimal saldoCantidad;
    private BigDecimal saldoValor;
    private final Queue<IngresoDisponible> ingresosQueue = new LinkedList<>();
    private final Map<String, SaldoKardexEntity> cacheSaldos = new HashMap<>();

    /**
     * Constructor que inicializa el procesador con todas sus dependencias.
     */
    public CostingGroupProcessor(
            String claveAgrupacion,
            List<TransaccionEntity> transacciones,
            EntityManager entityManager,
            KardexRepository kardexRepository,
            SaldoKardexRepository saldoKardexRepository,
            TipoMovimientoRepository tipoMovimientoRepository) {

        this.claveAgrupacion = claveAgrupacion;
        this.transacciones = transacciones;
        this.entityManager = entityManager;
        this.kardexRepository = kardexRepository;
        this.saldoKardexRepository = saldoKardexRepository;

        // Inicializar handlers con sus dependencias
        KardexFactory kardexFactory = new KardexFactory();
        this.ingresoHandler = new IngresoHandler(entityManager, kardexFactory);
        this.egresoHandler = new EgresoHandler(entityManager, tipoMovimientoRepository, kardexFactory);
    }

    /**
     * Procesa todas las transacciones del grupo usando el algoritmo FIFO.
     */
    public void process() {
        if (transacciones == null || transacciones.isEmpty()) {
            log.warn("No hay transacciones para procesar en el grupo: {}", claveAgrupacion);
            return;
        }

        log.info("=== Procesando grupo: {} ({} transacciones) ===",
                claveAgrupacion, transacciones.size());

        // 1. Inicializar saldos
        inicializarSaldos();

        // 2. Inicializar cola FIFO con ingresos históricos
        inicializarColaFIFO();

        // 3. Procesar transacciones una por una
        boolean haFallado = false;
        int procesadas = 0;

        for (TransaccionEntity tx : transacciones) {
            // Si ya hubo un error, marcar el resto para revisión
            if (haFallado) {
                marcarParaRevision(tx);
                continue;
            }

            try {
                procesarTransaccion(tx);
                marcarComoCosteada(tx);
                actualizarSaldoKardex(tx);
                procesadas++;

            } catch (InsufficientBalanceException e) {
                log.error("Saldo insuficiente para Tx ID: {}. Marcando para revisión", tx.getId(), e);
                marcarParaRevision(tx);
                haFallado = true;

            } catch (Exception e) {
                log.error("Error inesperado procesando Tx ID: {}. Marcando para revisión", tx.getId(), e);
                marcarParaRevision(tx);
                haFallado = true;
            }
        }

        // 4. Actualizar saldos diarios solo si no hubo errores
        if (!haFallado) {
            actualizarSaldosDiarios();
            log.info("✓ Grupo procesado exitosamente: {} transacciones", procesadas);
        } else {
            log.warn("⚠ Grupo procesado con errores: {} procesadas, resto marcado para revisión", procesadas);
        }
    }

    /**
     * Procesa una transacción individual según su tipo.
     */
    private void procesarTransaccion(TransaccionEntity tx) throws InsufficientBalanceException {
        TipoEnumsCosteo tipoContable = tx.getTipoMovimiento()
                .getMovimientoContable()
                .getTipoContable();

        if (tipoContable == TipoEnumsCosteo.INGRESO) {
            IngresoHandler.IngresoResult resultado = ingresoHandler.handle(
                    tx, ingresosQueue, saldoCantidad, saldoValor, claveAgrupacion);
            
            saldoCantidad = resultado.nuevoSaldoCantidad();
            saldoValor = resultado.nuevoSaldoValor();

        } else if (tipoContable == TipoEnumsCosteo.EGRESO) {
            EgresoHandler.EgresoResult resultado = egresoHandler.handle(
                    tx, ingresosQueue, saldoCantidad, saldoValor, claveAgrupacion);
            
            saldoCantidad = resultado.nuevoSaldoCantidad();
            saldoValor = resultado.nuevoSaldoValor();
        }

        log.debug("Transacción procesada - ID: {}, Tipo: {}, Nuevo saldo: qty={}, val={}",
                tx.getId(), tipoContable, saldoCantidad, saldoValor);
    }

    /**
     * Inicializa los saldos de cantidad y valor para el grupo.
     * Si la primera transacción es un saldo inicial, parte desde cero.
     * Si no, busca el último saldo del kardex.
     */
    private void inicializarSaldos() {
        TransaccionEntity primeraTx = transacciones.get(0);

        if (primeraTx.getTipoMovimiento().isEsSaldoInicial()) {
            log.info("Detectado saldo inicial para grupo {}. Partiendo desde CERO", claveAgrupacion);
            this.saldoCantidad = BigDecimal.ZERO;
            this.saldoValor = BigDecimal.ZERO;
        } else {
            // Buscar el último saldo antes de esta fecha
            Optional<KardexEntity> ultimoKardex = kardexRepository.findByEmpresaIdAndCustodioIdAndInstrumentoIdAndCuenta(
                    primeraTx.getEmpresa().getId(),
                    primeraTx.getCustodio().getId(),
                    primeraTx.getInstrumento().getId(),
                    primeraTx.getCuenta()
            ).stream()
            .filter(k -> k.getFechaTransaccion().isBefore(primeraTx.getFechaTransaccion()))
            .max(Comparator.comparing(KardexEntity::getFechaTransaccion)
                           .thenComparing(KardexEntity::getId));

            this.saldoCantidad = ultimoKardex.map(KardexEntity::getSaldoCantidad)
                    .orElse(BigDecimal.ZERO);
            this.saldoValor = ultimoKardex.map(KardexEntity::getSaldoValor)
                    .orElse(BigDecimal.ZERO);

            log.info("Continuando historial para grupo {}. Saldo inicial: qty={}, val={}",
                    claveAgrupacion, saldoCantidad, saldoValor);
        }
    }

    /**
     * Inicializa la cola FIFO con los ingresos históricos que aún tienen cantidad disponible.
     */
    private void inicializarColaFIFO() {
        TransaccionEntity primeraTx = transacciones.get(0);

        List<KardexEntity> ingresosHistoricos = entityManager.createQuery("""
            SELECT k FROM KardexEntity k
            WHERE k.claveAgrupacion = :clave
              AND k.tipoContable = :tipoIngreso
              AND k.cantidadDisponible > 0
              AND k.fechaTransaccion < :hastaFecha
            ORDER BY k.fechaTransaccion ASC, k.id ASC
            """, KardexEntity.class)
                .setParameter("clave", claveAgrupacion)
                .setParameter("tipoIngreso", TipoEnumsCosteo.INGRESO)
                .setParameter("hastaFecha", primeraTx.getFechaTransaccion())
                .getResultList();

        ingresosHistoricos.forEach(k -> ingresosQueue.add(new IngresoDisponible(k)));

        log.debug("Cola FIFO inicializada con {} ingresos históricos disponibles", ingresosQueue.size());
    }

    /**
     * Actualiza el saldo consolidado en la tabla saldos_kardex.
     */
    private void actualizarSaldoKardex(TransaccionEntity tx) {
        SaldoKardexEntity saldo = cacheSaldos.get(claveAgrupacion);

        if (saldo == null) {
            // Buscar o crear el saldo
            saldo = saldoKardexRepository
                    .findByEmpresaIdAndCustodioIdAndInstrumentoIdAndCuenta(
                            tx.getEmpresa().getId(),
                            tx.getCustodio().getId(),
                            tx.getInstrumento().getId(),
                            tx.getCuenta()
                    )
                    .orElseGet(() -> crearNuevoSaldoKardex(tx));

            cacheSaldos.put(claveAgrupacion, saldo);
        }

        // Actualizar valores
        saldo.setSaldoCantidad(saldoCantidad);
        saldo.setCostoTotal(saldoValor);
        saldo.recalcularCostoPromedio();
        saldo.setFechaUltimaActualizacion(tx.getFechaTransaccion());

        if (saldo.getId() == null) {
            entityManager.persist(saldo);
        } else {
            entityManager.merge(saldo);
        }
    }

    /**
     * Crea un nuevo registro de saldo kardex.
     */
    private SaldoKardexEntity crearNuevoSaldoKardex(TransaccionEntity tx) {
        return SaldoKardexEntity.builder()
                .empresa(tx.getEmpresa())
                .custodio(tx.getCustodio())
                .instrumento(tx.getInstrumento())
                .cuenta(tx.getCuenta())
                .saldoCantidad(BigDecimal.ZERO)
                .costoTotal(BigDecimal.ZERO)
                .costoPromedio(BigDecimal.ZERO)
                .fechaUltimaActualizacion(tx.getFechaTransaccion())
                .build();
    }

    /**
     * Actualiza los saldos diarios para todas las fechas afectadas por las transacciones procesadas.
     */
    private void actualizarSaldosDiarios() {
        if (transacciones == null || transacciones.isEmpty()) {
            return;
        }

        TransaccionEntity primeraTx = transacciones.get(0);

        // Determinar el rango de fechas
        LocalDate fechaInicio = transacciones.stream()
                .map(TransaccionEntity::getFechaTransaccion)
                .min(LocalDate::compareTo)
                .orElse(LocalDate.now());

        LocalDate fechaFin = transacciones.stream()
                .map(TransaccionEntity::getFechaTransaccion)
                .max(LocalDate::compareTo)
                .orElse(LocalDate.now());

        log.debug("Actualizando saldos diarios para grupo {} desde {} hasta {}",
                claveAgrupacion, fechaInicio, fechaFin);

        // 1. Obtener el último saldo ANTES del rango
        Optional<SaldosDiariosEntity> ultimoSaldoPrevio = buscarUltimoSaldoDiarioAntes(primeraTx, fechaInicio);

        BigDecimal saldoCantidadArrastre = ultimoSaldoPrevio
                .map(SaldosDiariosEntity::getSaldoCantidad)
                .orElse(BigDecimal.ZERO);
        BigDecimal saldoValorArrastre = ultimoSaldoPrevio
                .map(SaldosDiariosEntity::getSaldoValor)
                .orElse(BigDecimal.ZERO);

        // 2. Obtener movimientos del kardex agrupados por día
        Map<LocalDate, List<KardexEntity>> kardexPorDia = obtenerKardexPorDia(primeraTx, fechaInicio, fechaFin);

        // 3. Obtener saldos diarios existentes
        Map<LocalDate, SaldosDiariosEntity> saldosExistentes = obtenerSaldosDiariosExistentes(
                primeraTx, fechaInicio, fechaFin);

        // 4. Iterar día por día actualizando o creando saldos
        for (LocalDate dia = fechaInicio; !dia.isAfter(fechaFin); dia = dia.plusDays(1)) {
            List<KardexEntity> movimientosDelDia = kardexPorDia.get(dia);

            // Si hubo movimientos, actualizar con el último saldo del día
            if (movimientosDelDia != null && !movimientosDelDia.isEmpty()) {
                KardexEntity ultimoMovimiento = movimientosDelDia.stream()
                        .max(Comparator.comparing(KardexEntity::getId))
                        .orElseThrow();

                saldoCantidadArrastre = ultimoMovimiento.getSaldoCantidad();
                saldoValorArrastre = ultimoMovimiento.getSaldoValor();
            }
            // Si no hubo movimientos, se arrastra el saldo del día anterior

            // Actualizar o crear el saldo diario
            actualizarOCrearSaldoDiario(dia, primeraTx, saldoCantidadArrastre, saldoValorArrastre, saldosExistentes);
        }
    }

    /**
     * Busca el último saldo diario antes de una fecha.
     */
    private Optional<SaldosDiariosEntity> buscarUltimoSaldoDiarioAntes(TransaccionEntity tx, LocalDate fecha) {
        return entityManager.createQuery("""
            SELECT s FROM SaldosDiariosEntity s
            WHERE s.empresa = :empresa
              AND s.custodio = :custodio
              AND s.instrumento = :instrumento
              AND s.cuenta = :cuenta
              AND s.fecha < :fecha
            ORDER BY s.fecha DESC
            """, SaldosDiariosEntity.class)
                .setParameter("empresa", tx.getEmpresa())
                .setParameter("custodio", tx.getCustodio())
                .setParameter("instrumento", tx.getInstrumento())
                .setParameter("cuenta", tx.getCuenta())
                .setParameter("fecha", fecha)
                .setMaxResults(1)
                .getResultStream()
                .findFirst();
    }

    /**
     * Obtiene los movimientos de kardex agrupados por día.
     */
    private Map<LocalDate, List<KardexEntity>> obtenerKardexPorDia(
            TransaccionEntity tx, LocalDate fechaInicio, LocalDate fechaFin) {
        
        List<KardexEntity> kardexList = entityManager.createQuery("""
            SELECT k FROM KardexEntity k
            WHERE k.empresa.id = :empresaId
              AND k.custodio.id = :custodioId
              AND k.instrumento.id = :instrumentoId
              AND k.cuenta = :cuenta
              AND k.fechaTransaccion BETWEEN :fechaInicio AND :fechaFin
            ORDER BY k.fechaTransaccion ASC, k.id ASC
            """, KardexEntity.class)
                .setParameter("empresaId", tx.getEmpresa().getId())
                .setParameter("custodioId", tx.getCustodio().getId())
                .setParameter("instrumentoId", tx.getInstrumento().getId())
                .setParameter("cuenta", tx.getCuenta())
                .setParameter("fechaInicio", fechaInicio)
                .setParameter("fechaFin", fechaFin)
                .getResultList();

        return kardexList.stream()
                .collect(Collectors.groupingBy(KardexEntity::getFechaTransaccion));
    }

    /**
     * Obtiene los saldos diarios existentes en el rango.
     */
    private Map<LocalDate, SaldosDiariosEntity> obtenerSaldosDiariosExistentes(
            TransaccionEntity tx, LocalDate fechaInicio, LocalDate fechaFin) {
        
        List<SaldosDiariosEntity> saldosList = entityManager.createQuery("""
            SELECT s FROM SaldosDiariosEntity s
            WHERE s.empresa = :empresa
              AND s.custodio = :custodio
              AND s.instrumento = :instrumento
              AND s.cuenta = :cuenta
              AND s.fecha BETWEEN :fechaInicio AND :fechaFin
            """, SaldosDiariosEntity.class)
                .setParameter("empresa", tx.getEmpresa())
                .setParameter("custodio", tx.getCustodio())
                .setParameter("instrumento", tx.getInstrumento())
                .setParameter("cuenta", tx.getCuenta())
                .setParameter("fechaInicio", fechaInicio)
                .setParameter("fechaFin", fechaFin)
                .getResultList();

        return saldosList.stream()
                .collect(Collectors.toMap(SaldosDiariosEntity::getFecha, Function.identity()));
    }

    /**
     * Actualiza o crea un saldo diario para una fecha específica.
     */
    private void actualizarOCrearSaldoDiario(
            LocalDate dia,
            TransaccionEntity tx,
            BigDecimal saldoCantidad,
            BigDecimal saldoValor,
            Map<LocalDate, SaldosDiariosEntity> saldosExistentes) {

        SaldosDiariosEntity saldoDiario = saldosExistentes.get(dia);

        if (saldoDiario != null) {
            // Actualizar existente
            saldoDiario.setSaldoCantidad(saldoCantidad);
            saldoDiario.setSaldoValor(saldoValor);
            entityManager.merge(saldoDiario);
        } else {
            // Crear nuevo
            saldoDiario = SaldosDiariosEntity.builder()
                    .fecha(dia)
                    .empresa(tx.getEmpresa())
                    .custodio(tx.getCustodio())
                    .instrumento(tx.getInstrumento())
                    .cuenta(tx.getCuenta())
                    .saldoCantidad(saldoCantidad)
                    .saldoValor(saldoValor)
                    .build();
            entityManager.persist(saldoDiario);
        }
    }

    /**
     * Marca una transacción como costeada.
     */
    private void marcarComoCosteada(TransaccionEntity tx) {
        tx.setCosteado(true);
        tx.setParaRevision(false);
        entityManager.merge(tx);
    }

    /**
     * Marca una transacción para revisión manual.
     */
    private void marcarParaRevision(TransaccionEntity tx) {
        tx.setCosteado(false);
        tx.setParaRevision(true);
        entityManager.merge(tx);
    }
}