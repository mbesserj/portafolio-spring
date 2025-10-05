
package com.portafolio.persistence.repositorio;


import com.portafolio.model.entities.DetalleCosteoEntity;
import com.portafolio.model.entities.TransaccionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repositorio para DetalleCosteoEntity - Orientado al motor de costeo
 * Solo incluye operaciones esenciales para el procesamiento de costeos
 */
@Repository
public interface DetalleCosteoRepository extends JpaRepository<DetalleCosteoEntity, Long> {

    // ==================== Operaciones para el motor de costeo ====================
    
    /**
     * Busca detalles de costeo por clave de agrupación
     * Utilizado por el motor para encontrar costeos existentes
     */
    List<DetalleCosteoEntity> findByClaveAgrupacion(String claveAgrupacion);
    
    /**
     * Busca detalles de costeo por transacción de ingreso
     * Para identificar qué egresos se costearon con un ingreso específico
     */
    List<DetalleCosteoEntity> findByIngreso(TransaccionEntity ingreso);
    
    /**
     * Busca detalles de costeo por transacción de egreso
     * Para identificar con qué ingresos se costeó un egreso específico
     */
    List<DetalleCosteoEntity> findByEgreso(TransaccionEntity egreso);
    
    /**
     * Elimina todos los detalles de costeo por clave de agrupación
     * Utilizado cuando se recalcula el costeo de un grupo
     */
    void deleteByClaveAgrupacion(String claveAgrupacion);
    
    /**
     * Elimina detalles de costeo asociados a una transacción específica
     * Utilizado cuando se elimina o modifica una transacción
     */
    void deleteByIngresoOrEgreso(TransaccionEntity transaccion, TransaccionEntity transaccion2);

    // ==================== Consultas de resumen para el motor ====================
    
    /**
     * Obtiene el costo total por clave de agrupación
     * Utilizado para verificar cálculos del motor
     */
    @Query("SELECT d.claveAgrupacion, SUM(d.costoParcial) " +
           "FROM DetalleCosteoEntity d " +
           "WHERE d.claveAgrupacion = :clave " +
           "GROUP BY d.claveAgrupacion")
    Object[] obtenerCostoTotalPorClave(@Param("clave") String claveAgrupacion);
    
    /**
     * Obtiene la cantidad total utilizada por clave de agrupación
     * Utilizado para verificar balances del motor
     */
    @Query("SELECT d.claveAgrupacion, SUM(d.cantidadUsada) " +
           "FROM DetalleCosteoEntity d " +
           "WHERE d.claveAgrupacion = :clave " +
           "GROUP BY d.claveAgrupacion")
    Object[] obtenerCantidadTotalPorClave(@Param("clave") String claveAgrupacion);
    
    /**
     * Verifica si existe costeo para una combinación ingreso-egreso específica
     * Evita duplicados en el motor de costeo
     */
    boolean existsByIngresoAndEgreso(TransaccionEntity ingreso, TransaccionEntity egreso);
    
    /**
     * Obtiene detalles de costeo incompletos
     * Para debugging y mantenimiento del motor
     */
    @Query("SELECT d FROM DetalleCosteoEntity d " +
           "WHERE d.claveAgrupacion IS NULL " +
           "OR d.cantidadUsada IS NULL " +
           "OR d.costoParcial IS NULL " +
           "OR d.ingreso IS NULL " +
           "OR d.egreso IS NULL")
    List<DetalleCosteoEntity> findDetallesIncompletos();

    // ==================== Operaciones de limpieza ====================
    
    /**
     * Encuentra detalles huérfanos (sin transacciones válidas)
     * Para mantenimiento de la base de datos
     */
    @Query("SELECT d FROM DetalleCosteoEntity d " +
           "WHERE d.ingreso IS NULL OR d.egreso IS NULL")
    List<DetalleCosteoEntity> findDetallesHuerfanos();
    
    /**
     * Cuenta detalles de costeo por clave de agrupación
     * Para estadísticas del motor
     */
    Long countByClaveAgrupacion(String claveAgrupacion);
    
    /**
     * Obtiene resumen de costeos por transacción de ingreso
     * Para reportes del motor de costeo
     */
    @Query("SELECT d.ingreso.id, COUNT(d), SUM(d.costoParcial), SUM(d.cantidadUsada) " +
           "FROM DetalleCosteoEntity d " +
           "WHERE d.ingreso.id = :ingresoId " +
           "GROUP BY d.ingreso.id")
    Object[] obtenerResumenPorIngreso(@Param("ingresoId") Long ingresoId);
}