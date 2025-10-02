
package com.persistence.repositorio;

import com.model.entities.CustodioEntity;
import com.model.entities.EmpresaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustodioRepository extends JpaRepository<CustodioEntity, Long> {

    // ==================== Búsquedas básicas ====================
    
    /**
     * Busca custodio por nombre exacto
     */
    Optional<CustodioEntity> findByNombreCustodio(String nombreCustodio);
    
    /**
     * Busca custodios que contengan el texto especificado (case insensitive)
     */
    List<CustodioEntity> findByNombreCustodioContainingIgnoreCase(String nombre);
    
    /**
     * Busca custodios que empiecen con el texto especificado
     */
    List<CustodioEntity> findByNombreCustodioStartingWithIgnoreCase(String prefijo);
    
    /**
     * Verifica si existe un custodio con el nombre especificado
     */
    boolean existsByNombreCustodio(String nombreCustodio);

    // ==================== Búsquedas por relaciones ====================
    
    /**
     * Busca custodios que tengan al menos una empresa asociada
     */
    @Query("SELECT DISTINCT c FROM CustodioEntity c WHERE SIZE(c.empresas) > 0")
    List<CustodioEntity> findCustodiosConEmpresas();
    
    /**
     * Busca custodios sin empresas asociadas
     */
    @Query("SELECT c FROM CustodioEntity c WHERE SIZE(c.empresas) = 0")
    List<CustodioEntity> findCustodiosSinEmpresas();
    
    /**
     * Busca custodios que tengan transacciones
     */
    @Query("SELECT DISTINCT c FROM CustodioEntity c WHERE SIZE(c.transacciones) > 0")
    List<CustodioEntity> findCustodiosConTransacciones();
    
    /**
     * Busca custodios que tengan cuentas
     */
    @Query("SELECT DISTINCT c FROM CustodioEntity c WHERE SIZE(c.cuentas) > 0")
    List<CustodioEntity> findCustodiosConCuentas();

    // ==================== Búsquedas por empresa específica ====================
    
    /**
     * Busca custodios asociados a una empresa específica
     */
    @Query("SELECT c FROM CustodioEntity c JOIN c.empresas e WHERE e = :empresa")
    List<CustodioEntity> findByEmpresa(@Param("empresa") EmpresaEntity empresa);
    
    /**
     * Busca custodios asociados a una empresa por ID
     */
    @Query("SELECT c FROM CustodioEntity c JOIN c.empresas e WHERE e.id = :empresaId")
    List<CustodioEntity> findByEmpresaId(@Param("empresaId") Long empresaId);
    
    /**
     * Busca custodios asociados a una empresa por RUT
     */
    @Query("SELECT c FROM CustodioEntity c JOIN c.empresas e WHERE e.rut = :rut")
    List<CustodioEntity> findByEmpresaRut(@Param("rut") String rut);

    // ==================== Consultas de estadísticas ====================
    
    /**
     * Obtiene custodios con estadísticas de empresas, cuentas y transacciones
     */
    @Query("SELECT c.id, c.nombreCustodio, " +
           "SIZE(c.empresas) as totalEmpresas, " +
           "SIZE(c.cuentas) as totalCuentas, " +
           "SIZE(c.transacciones) as totalTransacciones " +
           "FROM CustodioEntity c " +
           "ORDER BY c.nombreCustodio")
    List<Object[]> findCustodiosConEstadisticas();
    
    /**
     * Obtiene los custodios más activos (con más transacciones)
     */
    @Query("SELECT c FROM CustodioEntity c " +
           "ORDER BY SIZE(c.transacciones) DESC")
    List<CustodioEntity> findCustodiosMasActivos();
    
    /**
     * Cuenta el total de transacciones por custodio
     */
    @Query("SELECT c.nombreCustodio, SIZE(c.transacciones) " +
           "FROM CustodioEntity c " +
           "ORDER BY SIZE(c.transacciones) DESC")
    List<Object[]> contarTransaccionesPorCustodio();
    
    /**
     * Cuenta el total de empresas por custodio
     */
    @Query("SELECT c.nombreCustodio, SIZE(c.empresas) " +
           "FROM CustodioEntity c " +
           "ORDER BY SIZE(c.empresas) DESC")
    List<Object[]> contarEmpresasPorCustodio();

    // ==================== Consultas de resumen ====================
    
    /**
     * Obtiene resumen general de custodios
     */
    @Query("SELECT COUNT(c), " +
           "AVG(SIZE(c.empresas)), " +
           "AVG(SIZE(c.transacciones)), " +
           "AVG(SIZE(c.cuentas)) " +
           "FROM CustodioEntity c")
    Object[] obtenerResumenGeneral();
    
    /**
     * Busca custodios con más de X empresas
     */
    @Query("SELECT c FROM CustodioEntity c WHERE SIZE(c.empresas) > :minEmpresas")
    List<CustodioEntity> findCustodiosConMasDeXEmpresas(@Param("minEmpresas") int minEmpresas);
    
    /**
     * Busca custodios con más de X transacciones
     */
    @Query("SELECT c FROM CustodioEntity c WHERE SIZE(c.transacciones) > :minTransacciones")
    List<CustodioEntity> findCustodiosConMasDeXTransacciones(@Param("minTransacciones") int minTransacciones);

    // ==================== Consultas con JOIN FETCH para evitar N+1 ====================
    
    /**
     * Obtiene todos los custodios con sus empresas cargadas
     */
    @Query("SELECT DISTINCT c FROM CustodioEntity c LEFT JOIN FETCH c.empresas")
    List<CustodioEntity> findAllWithEmpresas();
    
    /**
     * Obtiene custodio específico con todas sus relaciones cargadas
     */
    @Query("SELECT c FROM CustodioEntity c " +
           "LEFT JOIN FETCH c.empresas " +
           "LEFT JOIN FETCH c.cuentas " +
           "WHERE c.id = :id")
    Optional<CustodioEntity> findByIdWithRelations(@Param("id") Long id);
    
    /**
     * Obtiene custodio por nombre con empresas cargadas
     */
    @Query("SELECT c FROM CustodioEntity c " +
           "LEFT JOIN FETCH c.empresas " +
           "WHERE c.nombreCustodio = :nombre")
    Optional<CustodioEntity> findByNombreWithEmpresas(@Param("nombre") String nombre);

    // ==================== Búsquedas con filtros ====================
    
    /**
     * Busca custodios con filtros opcionales
     */
    @Query("SELECT c FROM CustodioEntity c " +
           "WHERE (:nombre IS NULL OR LOWER(c.nombreCustodio) LIKE LOWER(CONCAT('%', :nombre, '%'))) " +
           "AND (:conEmpresas IS NULL OR " +
           "     (:conEmpresas = true AND SIZE(c.empresas) > 0) OR " +
           "     (:conEmpresas = false AND SIZE(c.empresas) = 0)) " +
           "ORDER BY c.nombreCustodio")
    List<CustodioEntity> buscarConFiltros(
            @Param("nombre") String nombre,
            @Param("conEmpresas") Boolean conEmpresas
    );
    
    /**
     * Busca custodios ordenados alfabéticamente
     */
    List<CustodioEntity> findAllByOrderByNombreCustodioAsc();
    
    /**
     * Busca los custodios más recientes
     */
    List<CustodioEntity> findTop10ByOrderByFechaCreacionDesc();
    
    /**
     * Busca custodios por múltiples nombres
     */
    @Query("SELECT c FROM CustodioEntity c WHERE c.nombreCustodio IN :nombres")
    List<CustodioEntity> findByNombresIn(@Param("nombres") List<String> nombres);
    
    /**
     * Busca custodios que no tengan relación con una empresa específica
     */
    @Query("SELECT c FROM CustodioEntity c " +
           "WHERE c NOT IN (SELECT DISTINCT c2 FROM CustodioEntity c2 JOIN c2.empresas e WHERE e.id = :empresaId)")
    List<CustodioEntity> findCustodiosNoAsociadosAEmpresa(@Param("empresaId") Long empresaId);

    // ==================== Operaciones de mantenimiento ====================
    
    /**
     * Encuentra custodios duplicados por nombre
     */
    @Query("SELECT c.nombreCustodio, COUNT(c) " +
           "FROM CustodioEntity c " +
           "GROUP BY c.nombreCustodio " +
           "HAVING COUNT(c) > 1")
    List<Object[]> encontrarCustodiosDuplicados();
    
    /**
     * Encuentra custodios huérfanos (sin empresas, cuentas ni transacciones)
     */
    @Query("SELECT c FROM CustodioEntity c " +
           "WHERE SIZE(c.empresas) = 0 " +
           "AND SIZE(c.cuentas) = 0 " +
           "AND SIZE(c.transacciones) = 0")
    List<CustodioEntity> findCustodiosHuerfanos();
}