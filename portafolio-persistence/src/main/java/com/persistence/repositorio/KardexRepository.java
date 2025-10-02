package com.persistence.repositorio;

import com.model.entities.KardexEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface KardexRepository extends JpaRepository<KardexEntity, Long> {

    // ==================== Búsquedas básicas ====================
    List<KardexEntity> findByFolio(String folio);
    List<KardexEntity> findByFechaTransaccionBetween(LocalDate start, LocalDate end);

    // ==================== Consultas de negocio (del DAO original) ====================
    @Query("""
        SELECT k FROM KardexEntity k
        WHERE k.empresa.id = :empresaId AND k.cuenta = :cuenta
        AND k.custodio.id = :custodioId AND k.instrumento.id = :instrumentoId
        ORDER BY k.fechaTransaccion DESC, k.id DESC
        LIMIT 1
        """)
    Optional<KardexEntity> findLastByGroup(
            @Param("empresaId") Long empresaId, @Param("cuenta") String cuenta,
            @Param("custodioId") Long custodioId, @Param("instrumentoId") Long instrumentoId);

    // ==================== Consultas de rendimiento (Evitar N+1) ====================
    @Query("""
        SELECT k FROM KardexEntity k
        LEFT JOIN FETCH k.transaccion
        LEFT JOIN FETCH k.custodio
        LEFT JOIN FETCH k.empresa
        LEFT JOIN FETCH k.instrumento i
        LEFT JOIN FETCH i.producto
        WHERE k.id = :id
        """)
    Optional<KardexEntity> findByIdWithAllRelations(@Param("id") Long id);
}