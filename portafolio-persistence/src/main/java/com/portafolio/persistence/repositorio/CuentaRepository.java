package com.portafolio.persistence.repositorio;

import com.portafolio.model.entities.CuentaEntity;
import com.portafolio.model.entities.EmpresaEntity;
import com.portafolio.model.entities.CustodioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CuentaRepository extends JpaRepository<CuentaEntity, Long> {

    /**
     * Busca cuentas por nombre de cuenta
     */
    List<CuentaEntity> findByCuenta(String cuenta);

    /**
     * Busca cuentas que contengan el texto especificado (case insensitive)
     */
    List<CuentaEntity> findByCuentaContainingIgnoreCase(String cuenta);

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
    @Query("SELECT c FROM CuentaEntity c "
            + "WHERE c.cuenta = :cuenta "
            + "AND c.empresa.id = :empresaId "
            + "AND c.custodio.id = :custodioId")
    Optional<CuentaEntity> findByCuentaAndIds(
            @Param("cuenta") String cuenta,
            @Param("empresaId") Long empresaId,
            @Param("custodioId") Long custodioId
    );

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
    @Query("SELECT COUNT(c) > 0 FROM CuentaEntity c "
            + "WHERE c.cuenta = :cuenta "
            + "AND c.empresa.id = :empresaId "
            + "AND c.custodio.id = :custodioId")
    boolean existsByCuentaAndIds(
            @Param("cuenta") String cuenta,
            @Param("empresaId") Long empresaId,
            @Param("custodioId") Long custodioId
    );

    /**
     * Obtiene todas las cuentas con información de empresa y custodio
     */
    @Query("SELECT c FROM CuentaEntity c "
            + "LEFT JOIN FETCH c.empresa "
            + "LEFT JOIN FETCH c.custodio "
            + "ORDER BY c.empresa.razonSocial, c.custodio.nombreCustodio, c.cuenta")
    List<CuentaEntity> findAllWithRelations();

    /**
     * Busca cuentas con filtros opcionales
     */
    @Query("SELECT c FROM CuentaEntity c "
            + "WHERE (:cuenta IS NULL OR LOWER(c.cuenta) LIKE LOWER(CONCAT('%', :cuenta, '%'))) "
            + "AND (:empresaId IS NULL OR c.empresa.id = :empresaId) "
            + "AND (:custodioId IS NULL OR c.custodio.id = :custodioId) "
            + "ORDER BY c.empresa.razonSocial, c.custodio.nombreCustodio, c.cuenta")
    List<CuentaEntity> buscarConFiltros(
            @Param("cuenta") String cuenta,
            @Param("empresaId") Long empresaId,
            @Param("custodioId") Long custodioId
    );

    /**
     * Busca cuentas duplicadas (mismo nombre, diferentes empresas/custodios)
     */
    @Query("SELECT c.cuenta, COUNT(c) "
            + "FROM CuentaEntity c "
            + "GROUP BY c.cuenta "
            + "HAVING COUNT(c) > 1 "
            + "ORDER BY COUNT(c) DESC")
    List<Object[]> encontrarCuentasDuplicadas();

    /**
     * Obtiene todas las cuentas ordenadas para reportes
     */
    @Query("SELECT c FROM CuentaEntity c "
            + "ORDER BY c.empresa.razonSocial ASC, c.custodio.nombreCustodio ASC, c.cuenta ASC")
    List<CuentaEntity> findAllOrdenadas();

    /**
     * Busca cuentas por múltiples nombres
     */
    @Query("SELECT c FROM CuentaEntity c WHERE c.cuenta IN :cuentas")
    List<CuentaEntity> findByCuentaIn(@Param("cuentas") List<String> cuentas);

    @Query("SELECT c.cuenta FROM CuentaEntity c WHERE c.custodio.id = :custodioId AND c.empresa.id = :empresaId ORDER BY c.cuenta ASC")
    List<String> findCuentasByCustodioAndEmpresa(
            @Param("custodioId") Long custodioId,
            @Param("empresaId") Long empresaId);
}
