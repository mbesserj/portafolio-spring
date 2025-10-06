package com.portafolio.persistence.repositorio;

import com.portafolio.model.entities.InstrumentoEntity;
import com.portafolio.model.entities.SaldosDiariosEntity;
import java.math.BigDecimal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;

@Repository
public interface SaldosDiariosRepository extends JpaRepository<SaldosDiariosEntity, Long> {

    Optional<SaldosDiariosEntity> findByFechaAndEmpresaIdAndCustodioIdAndInstrumentoIdAndCuenta(
            LocalDate fecha, Long empresaId, Long custodioId, Long instrumentoId, String cuenta);

    List<SaldosDiariosEntity> findByFechaBetweenOrderByFechaDesc(LocalDate fechaInicio, LocalDate fechaFin);

    List<SaldosDiariosEntity> findByEmpresaIdAndFechaBetween(Long empresaId, LocalDate fechaInicio, LocalDate fechaFin);

    @Query("SELECT SUM(s.saldoValor) FROM SaldosDiariosEntity s WHERE s.fecha = :fecha")
    Optional<BigDecimal> sumSaldoValorByFecha(@Param("fecha") LocalDate fecha);

    @Query("""
        SELECT sd FROM SaldosDiariosEntity sd
        LEFT JOIN FETCH sd.empresa
        LEFT JOIN FETCH sd.custodio
        LEFT JOIN FETCH sd.instrumento
        WHERE sd.id = :id
        """)
    Optional<SaldosDiariosEntity> findByIdWithRelations(@Param("id") Long id);

    @Modifying
    @Query("DELETE FROM SaldosDiariosEntity s WHERE s.instrumento IN :instrumentos")
    void limpiarPorInstrumentos(@Param("instrumentos") List<InstrumentoEntity> instrumentos);

}