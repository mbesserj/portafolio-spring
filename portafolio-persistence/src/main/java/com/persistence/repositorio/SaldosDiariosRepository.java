package com.persistence.repositorio;

import com.model.entities.SaldosDiariosEntity;
import java.math.BigDecimal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SaldosDiariosRepository extends JpaRepository<SaldosDiariosEntity, Long> {

    // ==================== Búsqueda por Clave de Negocio ====================
    Optional<SaldosDiariosEntity> findByFechaAndEmpresaIdAndCustodioIdAndInstrumentoIdAndCuenta(
            LocalDate fecha, Long empresaId, Long custodioId, Long instrumentoId, String cuenta);

    // ==================== Búsquedas por Rango y Grupo ====================
    List<SaldosDiariosEntity> findByFechaBetweenOrderByFechaDesc(LocalDate fechaInicio, LocalDate fechaFin);
    List<SaldosDiariosEntity> findByEmpresaIdAndFechaBetween(Long empresaId, LocalDate fechaInicio, LocalDate fechaFin);
    
    // ==================== Consultas de Agregación ====================
    @Query("SELECT SUM(s.saldoValor) FROM SaldosDiariosEntity s WHERE s.fecha = :fecha")
    Optional<BigDecimal> sumSaldoValorByFecha(@Param("fecha") LocalDate fecha);
    
    // ==================== Consulta de rendimiento (Evitar N+1) ====================
    @Query("""
        SELECT sd FROM SaldosDiariosEntity sd
        LEFT JOIN FETCH sd.empresa
        LEFT JOIN FETCH sd.custodio
        LEFT JOIN FETCH sd.instrumento
        WHERE sd.id = :id
        """)
    Optional<SaldosDiariosEntity> findByIdWithRelations(@Param("id") Long id);
}