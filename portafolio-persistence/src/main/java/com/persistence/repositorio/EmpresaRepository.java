
package com.persistence.repositorio;


import com.model.entities.EmpresaEntity;
import com.model.entities.GrupoEmpresaEntity;
import com.model.entities.CustodioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmpresaRepository extends JpaRepository<EmpresaEntity, Long> {

    // ==================== Búsquedas básicas ====================
    
    /**
     * Busca empresa por RUT exacto
     */
    Optional<EmpresaEntity> findByRut(String rut);
    
    /**
     * Busca empresa por razón social exacta
     */
    Optional<EmpresaEntity> findByRazonSocial(String razonSocial);
    
    /**
     * Busca empresas que contengan el texto en la razón social (case insensitive)
     */
    List<EmpresaEntity> findByRazonSocialContainingIgnoreCase(String razonSocial);
    
    /**
     * Busca empresas que empiecen con el texto especificado en la razón social
     */
    List<EmpresaEntity> findByRazonSocialStartingWithIgnoreCase(String prefijo);
    
    /**
     * Verifica si existe una empresa con el RUT especificado
     */
    boolean existsByRut(String rut);
    
    /**
     * Verifica si existe una empresa con la razón social especificada
     */
    boolean existsByRazonSocial(String razonSocial);

    // ==================== Búsquedas por grupo empresarial ====================
    
    /**
     * Busca empresas por grupo empresarial
     */
    List<EmpresaEntity> findByGrupoEmpresa(GrupoEmpresaEntity grupoEmpresa);
    
    /**
     * Busca empresas por ID de grupo empresarial
     */
    List<EmpresaEntity> findByGrupoEmpresaId(Long grupoEmpresaId);
    
    /**
     * Busca empresas sin grupo empresarial
     */
    List<EmpresaEntity> findByGrupoEmpresaIsNull();
    
    /**
     * Busca empresas con grupo empresarial
     */
    List<EmpresaEntity> findByGrupoEmpresaIsNotNull();

    // ==================== Búsquedas por relaciones ====================
    
    /**
     * Busca empresas que tengan al menos un custodio asociado
     */
    @Query("SELECT DISTINCT e FROM EmpresaEntity e WHERE SIZE(e.custodios) > 0")
    List<EmpresaEntity> findEmpresasConCustodios();
    
    /**
     * Busca empresas sin custodios asociados
     */
    @Query("SELECT e FROM EmpresaEntity e WHERE SIZE(e.custodios) = 0")
    List<EmpresaEntity> findEmpresasSinCustodios();
    
    /**
     * Busca empresas que tengan transacciones
     */
    @Query("SELECT DISTINCT e FROM EmpresaEntity e WHERE SIZE(e.transacciones) > 0")
    List<EmpresaEntity> findEmpresasConTransacciones();
    
    /**
     * Busca empresas que tengan cuentas
     */
    @Query("SELECT DISTINCT e FROM EmpresaEntity e WHERE SIZE(e.cuentas) > 0")
    List<EmpresaEntity> findEmpresasConCuentas();

    // ==================== Búsquedas por custodio específico ====================
    
    /**
     * Busca empresas asociadas a un custodio específico
     */
    @Query("SELECT e FROM EmpresaEntity e JOIN e.custodios c WHERE c = :custodio")
    List<EmpresaEntity> findByCustodio(@Param("custodio") CustodioEntity custodio);
    
    /**
     * Busca empresas asociadas a un custodio por ID
     */
    @Query("SELECT e FROM EmpresaEntity e JOIN e.custodios c WHERE c.id = :custodioId")
    List<EmpresaEntity> findByCustodioId(@Param("custodioId") Long custodioId);
    
    /**
     * Busca empresas asociadas a un custodio por nombre
     */
    @Query("SELECT e FROM EmpresaEntity e JOIN e.custodios c WHERE c.nombreCustodio = :nombreCustodio")
    List<EmpresaEntity> findByCustodioNombre(@Param("nombreCustodio") String nombreCustodio);

    // ==================== Consultas de estadísticas ====================
    
    /**
     * Obtiene empresas con estadísticas de custodios, cuentas y transacciones
     */
    @Query("SELECT e.id, e.rut, e.razonSocial, " +
           "SIZE(e.custodios) as totalCustodios, " +
           "SIZE(e.cuentas) as totalCuentas, " +
           "SIZE(e.transacciones) as totalTransacciones " +
           "FROM EmpresaEntity e " +
           "ORDER BY e.razonSocial")
    List<Object[]> findEmpresasConEstadisticas();
    
    /**
     * Obtiene las empresas más activas (con más transacciones)
     */
    @Query("SELECT e FROM EmpresaEntity e " +
           "ORDER BY SIZE(e.transacciones) DESC")
    List<EmpresaEntity> findEmpresasMasActivas();
    
    /**
     * Cuenta el total de transacciones por empresa
     */
    @Query("SELECT e.razonSocial, SIZE(e.transacciones) " +
           "FROM EmpresaEntity e " +
           "ORDER BY SIZE(e.transacciones) DESC")
    List<Object[]> contarTransaccionesPorEmpresa();
    
    /**
     * Cuenta el total de custodios por empresa
     */
    @Query("SELECT e.razonSocial, SIZE(e.custodios) " +
           "FROM EmpresaEntity e " +
           "ORDER BY SIZE(e.custodios) DESC")
    List<Object[]> contarCustodiosPorEmpresa();

    // ==================== Consultas por grupo empresarial ====================
    
    /**
     * Obtiene resumen de empresas por grupo
     */
    @Query("SELECT COALESCE(ge.nombre, 'Sin grupo'), COUNT(e) " +
           "FROM EmpresaEntity e " +
           "LEFT JOIN e.grupoEmpresa ge " +
           "GROUP BY ge.id, ge.nombre " +
           "ORDER BY COUNT(e) DESC")
    List<Object[]> contarEmpresasPorGrupo();
    
    /**
     * Busca empresas de un grupo ordenadas por razón social
     */
    List<EmpresaEntity> findByGrupoEmpresaOrderByRazonSocialAsc(GrupoEmpresaEntity grupoEmpresa);

    // ==================== Consultas de resumen ====================
    
    /**
     * Obtiene resumen general de empresas
     */
    @Query("SELECT COUNT(e), " +
           "AVG(SIZE(e.custodios)), " +
           "AVG(SIZE(e.transacciones)), " +
           "AVG(SIZE(e.cuentas)) " +
           "FROM EmpresaEntity e")
    Object[] obtenerResumenGeneral();
    
    /**
     * Busca empresas con más de X custodios
     */
    @Query("SELECT e FROM EmpresaEntity e WHERE SIZE(e.custodios) > :minCustodios")
    List<EmpresaEntity> findEmpresasConMasDeXCustodios(@Param("minCustodios") int minCustodios);
    
    /**
     * Busca empresas con más de X transacciones
     */
    @Query("SELECT e FROM EmpresaEntity e WHERE SIZE(e.transacciones) > :minTransacciones")
    List<EmpresaEntity> findEmpresasConMasDeXTransacciones(@Param("minTransacciones") int minTransacciones);

    // ==================== Consultas con JOIN FETCH para evitar N+1 ====================
    
    /**
     * Obtiene todas las empresas con sus custodios cargados
     */
    @Query("SELECT DISTINCT e FROM EmpresaEntity e LEFT JOIN FETCH e.custodios")
    List<EmpresaEntity> findAllWithCustodios();
    
    /**
     * Obtiene empresa específica con todas sus relaciones cargadas
     */
    @Query("SELECT e FROM EmpresaEntity e " +
           "LEFT JOIN FETCH e.custodios " +
           "LEFT JOIN FETCH e.cuentas " +
           "LEFT JOIN FETCH e.grupoEmpresa " +
           "WHERE e.id = :id")
    Optional<EmpresaEntity> findByIdWithRelations(@Param("id") Long id);
    
    /**
     * Obtiene empresa por RUT con relaciones cargadas
     */
    @Query("SELECT e FROM EmpresaEntity e " +
           "LEFT JOIN FETCH e.custodios " +
           "LEFT JOIN FETCH e.grupoEmpresa " +
           "WHERE e.rut = :rut")
    Optional<EmpresaEntity> findByRutWithRelations(@Param("rut") String rut);

    // ==================== Búsquedas con filtros ====================
    
    /**
     * Busca empresas con filtros opcionales
     */
    @Query("SELECT e FROM EmpresaEntity e " +
           "WHERE (:razonSocial IS NULL OR LOWER(e.razonSocial) LIKE LOWER(CONCAT('%', :razonSocial, '%'))) " +
           "AND (:rut IS NULL OR e.rut LIKE CONCAT('%', :rut, '%')) " +
           "AND (:grupoId IS NULL OR e.grupoEmpresa.id = :grupoId) " +
           "AND (:conCustodios IS NULL OR " +
           "     (:conCustodios = true AND SIZE(e.custodios) > 0) OR " +
           "     (:conCustodios = false AND SIZE(e.custodios) = 0)) " +
           "ORDER BY e.razonSocial")
    List<EmpresaEntity> buscarConFiltros(
            @Param("razonSocial") String razonSocial,
            @Param("rut") String rut,
            @Param("grupoId") Long grupoId,
            @Param("conCustodios") Boolean conCustodios
    );
    
    /**
     * Busca empresas ordenadas alfabéticamente
     */
    List<EmpresaEntity> findAllByOrderByRazonSocialAsc();
    
    /**
     * Busca las empresas más recientes
     */
    List<EmpresaEntity> findTop10ByOrderByFechaCreacionDesc();
    
    /**
     * Busca empresas por múltiples RUTs
     */
    @Query("SELECT e FROM EmpresaEntity e WHERE e.rut IN :ruts")
    List<EmpresaEntity> findByRutsIn(@Param("ruts") List<String> ruts);
    
    /**
     * Busca empresas que no tengan relación con un custodio específico
     */
    @Query("SELECT e FROM EmpresaEntity e " +
           "WHERE e NOT IN (SELECT DISTINCT e2 FROM EmpresaEntity e2 JOIN e2.custodios c WHERE c.id = :custodioId)")
    List<EmpresaEntity> findEmpresasNoAsociadasACustodio(@Param("custodioId") Long custodioId);

    // ==================== Operaciones de mantenimiento ====================
    
    /**
     * Encuentra empresas duplicadas por RUT
     */
    @Query("SELECT e.rut, COUNT(e) " +
           "FROM EmpresaEntity e " +
           "GROUP BY e.rut " +
           "HAVING COUNT(e) > 1")
    List<Object[]> encontrarEmpresasDuplicadasPorRut();
    
    /**
     * Encuentra empresas duplicadas por razón social
     */
    @Query("SELECT e.razonSocial, COUNT(e) " +
           "FROM EmpresaEntity e " +
           "GROUP BY e.razonSocial " +
           "HAVING COUNT(e) > 1")
    List<Object[]> encontrarEmpresasDuplicadasPorRazonSocial();
    
    /**
     * Encuentra empresas huérfanas (sin custodios, cuentas ni transacciones)
     */
    @Query("SELECT e FROM EmpresaEntity e " +
           "WHERE SIZE(e.custodios) = 0 " +
           "AND SIZE(e.cuentas) = 0 " +
           "AND SIZE(e.transacciones) = 0")
    List<EmpresaEntity> findEmpresasHuerfanas();
}