package com.portafolio.persistence.repositorio;

import com.portafolio.model.entities.SaldoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SaldoRepository extends JpaRepository<SaldoEntity, Long> {

    // ==================== Búsquedas por fecha y relaciones ====================
    List<SaldoEntity> findByFechaBetweenOrderByFechaDesc(LocalDate fechaInicio, LocalDate fechaFin);
    List<SaldoEntity> findByEmpresaIdAndFecha(Long empresaId, LocalDate fecha);
    
    // Encuentra el último saldo registrado para un grupo específico
    Optional<SaldoEntity> findFirstByEmpresaIdAndCustodioIdAndInstrumentoIdOrderByFechaDesc(
            Long empresaId, Long custodioId, Long instrumentoId);

    // ==================== Consulta de rendimiento (Evitar N+1) ====================
    @Query("""
        SELECT s FROM SaldoEntity s
        LEFT JOIN FETCH s.instrumento
        LEFT JOIN FETCH s.custodio
        LEFT JOIN FETCH s.empresa
        WHERE s.id = :id
        """)
    Optional<SaldoEntity> findByIdWithRelations(@Param("id") Long id);
}