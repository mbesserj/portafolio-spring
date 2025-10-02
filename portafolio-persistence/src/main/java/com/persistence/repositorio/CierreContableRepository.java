
package com.persistence.repositorio;

import com.model.entities.CierreContableEntity;
import com.model.entities.EmpresaEntity;
import com.model.entities.CustodioEntity;
import com.model.entities.InstrumentoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface CierreContableRepository extends JpaRepository<CierreContableEntity, Long> {

    // ==================== Búsquedas por Ejercicio ====================
    
    /**
     * Busca cierres por ejercicio
     */
    List<CierreContableEntity> findByEjercicio(Integer ejercicio);
    
    /**
     * Busca cierres por ejercicio ordenados por empresa y custodio
     */
    List<CierreContableEntity> findByEjercicioOrderByEmpresaRazonSocialAscCustodioNombreCustodioAsc(Integer ejercicio);

    // ==================== Búsquedas por Empresa ====================
    
    /**
     * Busca cierres por empresa
     */
    List<CierreContableEntity> findByEmpresa(EmpresaEntity empresa);
    
    /**
     * Busca cierres por empresa y ejercicio
     */
    List<CierreContableEntity> findByEmpresaAndEjercicio(EmpresaEntity empresa, Integer ejercicio);
    
    /**
     * Busca cierres por ID de empresa
     */
    List<CierreContableEntity> findByEmpresaId(Long empresaId);

    // ==================== Búsquedas por Custodio ====================
    
    /**
     * Busca cierres por custodio
     */
    List<CierreContableEntity> findByCustodio(CustodioEntity custodio);
    
    /**
     * Busca cierres por custodio y ejercicio
     */
    List<CierreContableEntity> findByCustodioAndEjercicio(CustodioEntity custodio, Integer ejercicio);

    // ==================== Búsquedas por Instrumento ====================
    
    /**
     * Busca cierres por instrumento
     */
    List<CierreContableEntity> findByInstrumento(InstrumentoEntity instrumento);
    
    /**
     * Busca cierres por instrumento y ejercicio
     */
    List<CierreContableEntity> findByInstrumentoAndEjercicio(InstrumentoEntity instrumento, Integer ejercicio);
    
    /**
     * Busca cierres por nemo del instrumento
     */
    @Query("SELECT c FROM CierreContableEntity c WHERE c.instrumento.instrumentoNemo = :nemo")
    List<CierreContableEntity> findByInstrumentoNemo(@Param("nemo") String nemo);

    // ==================== Búsqueda única por clave compuesta ====================
    
    /**
     * Busca cierre específico por todos los criterios únicos
     */
    Optional<CierreContableEntity> findByEjercicioAndEmpresaAndCustodioAndInstrumento(
            Integer ejercicio,
            EmpresaEntity empresa,
            CustodioEntity custodio,
            InstrumentoEntity instrumento
    );
    
    /**
     * Busca cierre específico por IDs
     */
    @Query("SELECT c FROM CierreContableEntity c " +
           "WHERE c.ejercicio = :ejercicio " +
           "AND c.empresa.id = :empresaId " +
           "AND c.custodio.id = :custodioId " +
           "AND c.instrumento.id = :instrumentoId")
    Optional<CierreContableEntity> findByEjercicioAndIds(
            @Param("ejercicio") Integer ejercicio,
            @Param("empresaId") Long empresaId,
            @Param("custodioId") Long custodioId,
            @Param("instrumentoId") Long instrumentoId
    );

    // ==================== Búsquedas por valores ====================
    
    /**
     * Busca cierres con cantidad mayor a un valor específico
     */
    List<CierreContableEntity> findByCantidadCierreGreaterThan(BigDecimal cantidad);
    
    /**
     * Busca cierres con valor mayor a un monto específico
     */
    List<CierreContableEntity> findByValorCierreGreaterThan(BigDecimal valor);
    
    /**
     * Busca cierres con cantidad cero
     */
    @Query("SELECT c FROM CierreContableEntity c WHERE c.cantidadCierre = 0")
    List<CierreContableEntity> findCierresConCantidadCero();

    // ==================== Consultas de resumen ====================
    
    /**
     * Obtiene resumen de cierres por empresa en un ejercicio
     */
    @Query("SELECT c.empresa.razonSocial, " +
           "COUNT(c), " +
           "SUM(c.cantidadCierre), " +
           "SUM(c.valorCierre) " +
           "FROM CierreContableEntity c " +
           "WHERE c.ejercicio = :ejercicio " +
           "GROUP BY c.empresa.id, c.empresa.razonSocial " +
           "ORDER BY SUM(c.valorCierre) DESC")
    List<Object[]> obtenerResumenPorEmpresa(@Param("ejercicio") Integer ejercicio);
    
    /**
     * Obtiene resumen de cierres por custodio en un ejercicio
     */
    @Query("SELECT c.custodio.nombreCustodio, " +
           "COUNT(c), " +
           "SUM(c.cantidadCierre), " +
           "SUM(c.valorCierre) " +
           "FROM CierreContableEntity c " +
           "WHERE c.ejercicio = :ejercicio " +
           "GROUP BY c.custodio.id, c.custodio.nombreCustodio " +
           "ORDER BY SUM(c.valorCierre) DESC")
    List<Object[]> obtenerResumenPorCustodio(@Param("ejercicio") Integer ejercicio);
    
    /**
     * Obtiene resumen de cierres por instrumento en un ejercicio
     */
    @Query("SELECT c.instrumento.instrumentoNemo, " +
           "c.instrumento.instrumentoNombre, " +
           "COUNT(c), " +
           "SUM(c.cantidadCierre), " +
           "SUM(c.valorCierre) " +
           "FROM CierreContableEntity c " +
           "WHERE c.ejercicio = :ejercicio " +
           "GROUP BY c.instrumento.id, c.instrumento.instrumentoNemo, c.instrumento.instrumentoNombre " +
           "ORDER BY SUM(c.valorCierre) DESC")
    List<Object[]> obtenerResumenPorInstrumento(@Param("ejercicio") Integer ejercicio);

    // ==================== Consultas específicas del negocio ====================
    
    /**
     * Obtiene todos los ejercicios disponibles
     */
    @Query("SELECT DISTINCT c.ejercicio FROM CierreContableEntity c ORDER BY c.ejercicio DESC")
    List<Integer> obtenerEjerciciosDisponibles();
    
    /**
     * Cuenta cierres por ejercicio
     */
    Long countByEjercicio(Integer ejercicio);
    
    /**
     * Verifica si existe un cierre específico
     */
    boolean existsByEjercicioAndEmpresaAndCustodioAndInstrumento(
            Integer ejercicio,
            EmpresaEntity empresa,
            CustodioEntity custodio,
            InstrumentoEntity instrumento
    );
    
    /**
     * Busca cierres con filtros múltiples
     */
    @Query("SELECT c FROM CierreContableEntity c " +
           "WHERE (:ejercicio IS NULL OR c.ejercicio = :ejercicio) " +
           "AND (:empresaId IS NULL OR c.empresa.id = :empresaId) " +
           "AND (:custodioId IS NULL OR c.custodio.id = :custodioId) " +
           "AND (:instrumentoId IS NULL OR c.instrumento.id = :instrumentoId) " +
           "ORDER BY c.ejercicio DESC, c.empresa.razonSocial ASC, c.custodio.nombreCustodio ASC")
    List<CierreContableEntity> buscarConFiltros(
            @Param("ejercicio") Integer ejercicio,
            @Param("empresaId") Long empresaId,
            @Param("custodioId") Long custodioId,
            @Param("instrumentoId") Long instrumentoId
    );
    
    /**
     * Obtiene el valor total de cierre por ejercicio
     */
    @Query("SELECT SUM(c.valorCierre) FROM CierreContableEntity c WHERE c.ejercicio = :ejercicio")
    BigDecimal obtenerValorTotalPorEjercicio(@Param("ejercicio") Integer ejercicio);
    
    /**
     * Elimina cierres por ejercicio
     */
    void deleteByEjercicio(Integer ejercicio);
}