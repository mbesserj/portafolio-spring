
package com.persistence.repositorio;

import com.model.entities.CuentaEntity;
import com.model.entities.EmpresaEntity;
import com.model.entities.CustodioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CuentaRepository extends JpaRepository<CuentaEntity, Long> {

    // ==================== Búsquedas básicas ====================
    
    /**
     * Busca cuentas por nombre de cuenta
     */
    List<CuentaEntity> findByCuenta(String cuenta);
    
    /**
     * Busca cuentas que contengan el texto especificado (case insensitive)
     */
    List<CuentaEntity> findByCuentaContainingIgnoreCase(String cuenta);

    // ==================== Búsquedas por Empresa ====================
    
    /**
     * Busca cuentas por empresa
     */
    List<CuentaEntity> findByEmpresa(EmpresaEntity empresa);
    
    /**
     * Busca cuentas por ID de empresa
     */
    List<CuentaEntity> findByEmpresaId(Long empresaId);
    
    /**
     * Busca cuentas por empresa ordenadas por nombre de cuenta
     */
    List<CuentaEntity> findByEmpresaOrderByCuentaAsc(EmpresaEntity empresa);
    
    /**
     * Busca cuentas por RUT de empresa
     */
    @Query("SELECT c FROM CuentaEntity c WHERE c.empresa.rut = :rut")
    List<CuentaEntity> findByEmpresaRut(@Param("rut") String rut);

    // ==================== Búsquedas por Custodio ====================
    
    /**
     * Busca cuentas por custodio
     */
    List<CuentaEntity> findByCustodio(CustodioEntity custodio);
    
    /**
     * Busca cuentas por ID de custodio
     */
    List<CuentaEntity> findByCustodioId(Long custodioId);
    
    /**
     * Busca cuentas por custodio ordenadas por nombre de cuenta
     */
    List<CuentaEntity> findByCustodioOrderByCuentaAsc(CustodioEntity custodio);

    // ==================== Búsquedas por combinaciones ====================
    
    /**
     * Busca cuenta específica por empresa y custodio
     */
    List<CuentaEntity> findByEmpresaAndCustodio(EmpresaEntity empresa, CustodioEntity custodio);
    
    /**
     * Busca cuenta específica por los tres campos únicos
     */
    Optional<CuentaEntity> findByCuentaAndEmpresaAndCustodio(
            String cuenta, 
            EmpresaEntity empresa, 
            CustodioEntity custodio
    );
    
    /**
     * Busca cuenta específica por IDs
     */
    @Query("SELECT c FROM CuentaEntity c " +
           "WHERE c.cuenta = :cuenta " +
           "AND c.empresa.id = :empresaId " +
           "AND c.custodio.id = :custodioId")
    Optional<CuentaEntity> findByCuentaAndIds(
            @Param("cuenta") String cuenta,
            @Param("empresaId") Long empresaId,
            @Param("custodioId") Long custodioId
    );

    // ==================== Verificaciones de existencia ====================
    
    /**
     * Verifica si existe una cuenta específica
     */
    boolean existsByCuentaAndEmpresaAndCustodio(
            String cuenta, 
            EmpresaEntity empresa, 
            CustodioEntity custodio
    );
    
    /**
     * Verifica si existe una cuenta por IDs
     */
    @Query("SELECT COUNT(c) > 0 FROM CuentaEntity c " +
           "WHERE c.cuenta = :cuenta " +
           "AND c.empresa.id = :empresaId " +
           "AND c.custodio.id = :custodioId")
    boolean existsByCuentaAndIds(
            @Param("cuenta") String cuenta,
            @Param("empresaId") Long empresaId,
            @Param("custodioId") Long custodioId
    );

    // ==================== Consultas de conteo ====================
    
    /**
     * Cuenta cuentas por empresa
     */
    Long countByEmpresa(EmpresaEntity empresa);
    
    /**
     * Cuenta cuentas por custodio
     */
    Long countByCustodio(CustodioEntity custodio);
    
    /**
     * Cuenta cuentas por empresa y custodio
     */
    Long countByEmpresaAndCustodio(EmpresaEntity empresa, CustodioEntity custodio);

    // ==================== Consultas de resumen ====================
    
    /**
     * Obtiene resumen de cuentas por empresa
     */
    @Query("SELECT c.empresa.razonSocial, COUNT(c) " +
           "FROM CuentaEntity c " +
           "GROUP BY c.empresa.id, c.empresa.razonSocial " +
           "ORDER BY COUNT(c) DESC")
    List<Object[]> obtenerResumenPorEmpresa();
    
    /**
     * Obtiene resumen de cuentas por custodio
     */
    @Query("SELECT c.custodio.nombreCustodio, COUNT(c) " +
           "FROM CuentaEntity c " +
           "GROUP BY c.custodio.id, c.custodio.nombreCustodio " +
           "ORDER BY COUNT(c) DESC")
    List<Object[]> obtenerResumenPorCustodio();
    
    /**
     * Obtiene todas las cuentas con información de empresa y custodio
     */
    @Query("SELECT c FROM CuentaEntity c " +
           "LEFT JOIN FETCH c.empresa " +
           "LEFT JOIN FETCH c.custodio " +
           "ORDER BY c.empresa.razonSocial, c.custodio.nombreCustodio, c.cuenta")
    List<CuentaEntity> findAllWithRelations();

    // ==================== Búsquedas con filtros ====================
    
    /**
     * Busca cuentas con filtros opcionales
     */
    @Query("SELECT c FROM CuentaEntity c " +
           "WHERE (:cuenta IS NULL OR LOWER(c.cuenta) LIKE LOWER(CONCAT('%', :cuenta, '%'))) " +
           "AND (:empresaId IS NULL OR c.empresa.id = :empresaId) " +
           "AND (:custodioId IS NULL OR c.custodio.id = :custodioId) " +
           "ORDER BY c.empresa.razonSocial, c.custodio.nombreCustodio, c.cuenta")
    List<CuentaEntity> buscarConFiltros(
            @Param("cuenta") String cuenta,
            @Param("empresaId") Long empresaId,
            @Param("custodioId") Long custodioId
    );
    
    /**
     * Busca cuentas duplicadas (mismo nombre, diferentes empresas/custodios)
     */
    @Query("SELECT c.cuenta, COUNT(c) " +
           "FROM CuentaEntity c " +
           "GROUP BY c.cuenta " +
           "HAVING COUNT(c) > 1 " +
           "ORDER BY COUNT(c) DESC")
    List<Object[]> encontrarCuentasDuplicadas();
    
    /**
     * Obtiene las cuentas más recientes
     */
    List<CuentaEntity> findTop10ByOrderByFechaCreacionDesc();
    
    /**
     * Busca cuentas que empiecen con un prefijo específico
     */
    List<CuentaEntity> findByCuentaStartingWithIgnoreCase(String prefijo);
    
    /**
     * Busca cuentas que terminen con un sufijo específico
     */
    List<CuentaEntity> findByCuentaEndingWithIgnoreCase(String sufijo);
    
    /**
     * Obtiene todas las cuentas ordenadas para reportes
     */
    @Query("SELECT c FROM CuentaEntity c " +
           "ORDER BY c.empresa.razonSocial ASC, c.custodio.nombreCustodio ASC, c.cuenta ASC")
    List<CuentaEntity> findAllOrdenadas();
    
    /**
     * Busca cuentas por múltiples nombres
     */
    @Query("SELECT c FROM CuentaEntity c WHERE c.cuenta IN :cuentas")
    List<CuentaEntity> findByCuentaIn(@Param("cuentas") List<String> cuentas);
}