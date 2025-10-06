package com.portafolio.persistence.repositorio;

import com.portafolio.model.entities.CustodioEntity;
import com.portafolio.model.entities.EmpresaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustodioRepository extends JpaRepository<CustodioEntity, Long> {

    // --- MÉTODOS DE BÚSQUEDA (Ya los tenías) ---
    Optional<CustodioEntity> findByNombreCustodio(String nombreCustodio);
    
    List<CustodioEntity> findByNombreCustodioContainingIgnoreCase(String nombre);
    
    List<CustodioEntity> findByNombreCustodioStartingWithIgnoreCase(String prefijo);
    
    boolean existsByNombreCustodio(String nombreCustodio);
    
    List<CustodioEntity> findAllByOrderByNombreCustodioAsc();

    // --- MÉTODOS POR RELACIONES (Ya los tenías) ---
    @Query("SELECT DISTINCT c FROM CustodioEntity c WHERE SIZE(c.empresas) > 0")
    List<CustodioEntity> findCustodiosConEmpresas();

    @Query("SELECT c FROM CustodioEntity c WHERE SIZE(c.empresas) = 0")
    List<CustodioEntity> findCustodiosSinEmpresas();
    
    @Query("SELECT c FROM CustodioEntity c JOIN c.empresas e WHERE e.id = :empresaId ORDER BY c.nombreCustodio ASC")
    List<CustodioEntity> findByEmpresaId(@Param("empresaId") Long empresaId);
    
    // --- MÉTODOS DE FILTRO (Ya los tenías) ---
    @Query("SELECT c FROM CustodioEntity c "
            + "WHERE (:nombre IS NULL OR LOWER(c.nombreCustodio) LIKE LOWER(CONCAT('%', :nombre, '%'))) "
            + "AND (:conEmpresas IS NULL OR "
            + "     (:conEmpresas = true AND SIZE(c.empresas) > 0) OR "
            + "     (:conEmpresas = false AND SIZE(c.empresas) = 0)) "
            + "ORDER BY c.nombreCustodio")
    List<CustodioEntity> buscarConFiltros(@Param("nombre") String nombre, @Param("conEmpresas") Boolean conEmpresas);

    // --- MÉTODOS DE BÚSQUEDA MÚLTIPLE (Ya los tenías) ---
    @Query("SELECT c FROM CustodioEntity c WHERE c.nombreCustodio IN :nombres")
    List<CustodioEntity> findByNombresIn(@Param("nombres") List<String> nombres);


    @Query("SELECT c FROM CustodioEntity c "
            + "LEFT JOIN FETCH c.empresas "
            + "LEFT JOIN FETCH c.cuentas "
            + "WHERE c.id = :id")
    Optional<CustodioEntity> findByIdWithRelations(@Param("id") Long id);


    @Query("SELECT c.id, c.nombreCustodio, "
            + "SIZE(c.empresas) as totalEmpresas, "
            + "SIZE(c.cuentas) as totalCuentas, "
            + "SIZE(c.transacciones) as totalTransacciones "
            + "FROM CustodioEntity c "
            + "ORDER BY c.nombreCustodio")
    List<Object[]> findCustodiosConEstadisticas();


    @Query("SELECT c.nombreCustodio, SIZE(c.transacciones) "
            + "FROM CustodioEntity c "
            + "GROUP BY c.nombreCustodio "
            + "ORDER BY SIZE(c.transacciones) DESC")
    List<Object[]> contarTransaccionesPorCustodio();

    List<CustodioEntity> findTop10ByOrderByFechaCreacionDesc();
    
    Optional<CustodioEntity> findByNombre(String nombre);
    
}