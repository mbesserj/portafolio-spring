package com.persistence.repositorio;

import com.model.entities.SaldoKardexEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SaldoKardexRepository extends JpaRepository<SaldoKardexEntity, Long> {

    // ==================== Búsqueda por Clave de Negocio ====================
    /**
     * Método principal para encontrar un saldo específico por su grupo único.
     */
    Optional<SaldoKardexEntity> findByEmpresaIdAndCustodioIdAndInstrumentoIdAndCuenta(
            Long empresaId, Long custodioId, Long instrumentoId, String cuenta);

    // ==================== Búsquedas Adicionales ====================
    List<SaldoKardexEntity> findByEmpresaId(Long empresaId);
    List<SaldoKardexEntity> findByFechaUltimaActualizacionBefore(LocalDate fecha);

    // ==================== Consulta de rendimiento (Evitar N+1) ====================
    @Query("""
        SELECT sk FROM SaldoKardexEntity sk
        LEFT JOIN FETCH sk.empresa
        LEFT JOIN FETCH sk.custodio
        LEFT JOIN FETCH sk.instrumento
        WHERE sk.id = :id
        """)
    Optional<SaldoKardexEntity> findByIdWithRelations(@Param("id") Long id);
}