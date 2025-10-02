
package com.persistence.repositorio;


import com.model.entities.AuditoriaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AuditoriaRepository extends JpaRepository<AuditoriaEntity, Long> {

    /**
     * Busca auditorías por tipo de entidad
     */
    List<AuditoriaEntity> findByTipoEntidade(String tipoEntidad);

    /**
     * Busca auditorías por archivo origen
     */
    List<AuditoriaEntity> findByArchivoOrigenContainingIgnoreCase(String archivoOrigen);

    /**
     * Busca auditorías por rango de fechas
     */
    List<AuditoriaEntity> findByFechaAuditoriaBetween(LocalDate fechaInicio, LocalDate fechaFin);

    /**
     * Busca auditorías con errores (registros rechazados > 0)
     */
    @Query("SELECT a FROM AuditoriaEntity a WHERE a.registrosRechazados > 0")
    List<AuditoriaEntity> findAuditoriasConErrores();

    /**
     * Obtiene estadísticas de procesamiento por fecha
     */
    @Query("SELECT a.fechaAuditoria, " +
           "SUM(a.registrosInsertados) as totalInsertados, " +
           "SUM(a.registrosRechazados) as totalRechazados, " +
           "SUM(a.registrosDuplicados) as totalDuplicados " +
           "FROM AuditoriaEntity a " +
           "WHERE a.fechaAuditoria BETWEEN :fechaInicio AND :fechaFin " +
           "GROUP BY a.fechaAuditoria " +
           "ORDER BY a.fechaAuditoria DESC")
    List<Object[]> obtenerEstadisticasPorFecha(
            @Param("fechaInicio") LocalDate fechaInicio, 
            @Param("fechaFin") LocalDate fechaFin);

    /**
     * Busca auditorías por valor clave específico
     */
    List<AuditoriaEntity> findByValorClaveAndTipoEntidad(String valorClave, String tipoEntidad);

    /**
     * Obtiene las últimas auditorías procesadas
     */
    List<AuditoriaEntity> findTop10ByOrderByFechaCreacionDesc();

    /**
     * Busca auditorías por motivo que contenga el texto especificado
     */
    List<AuditoriaEntity> findByMotivoContainingIgnoreCase(String motivo);

    /**
     * Cuenta auditorías por tipo de entidad en un periodo
     */
    @Query("SELECT COUNT(a) FROM AuditoriaEntity a " +
           "WHERE a.tipoEntidad = :tipoEntidad " +
           "AND a.fechaAuditoria BETWEEN :fechaInicio AND :fechaFin")
    Long contarAuditoriasPorTipoYPeriodo(
            @Param("tipoEntidad") String tipoEntidad,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin);
}