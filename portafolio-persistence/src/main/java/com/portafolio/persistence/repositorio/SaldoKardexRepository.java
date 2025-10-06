package com.portafolio.persistence.repositorio;

import com.portafolio.model.entities.InstrumentoEntity;
import com.portafolio.model.entities.SaldoKardexEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;

@Repository
public interface SaldoKardexRepository extends JpaRepository<SaldoKardexEntity, Long> {


    Optional<SaldoKardexEntity> findByEmpresaIdAndCustodioIdAndInstrumentoIdAndCuenta(
            Long empresaId, Long custodioId, Long instrumentoId, String cuenta);

    List<SaldoKardexEntity> findByEmpresaId(Long empresaId);

    List<SaldoKardexEntity> findByFechaUltimaActualizacionBefore(LocalDate fecha);

    @Query("""
        SELECT sk FROM SaldoKardexEntity sk
        LEFT JOIN FETCH sk.empresa
        LEFT JOIN FETCH sk.custodio
        LEFT JOIN FETCH sk.instrumento
        WHERE sk.id = :id
        """)
    Optional<SaldoKardexEntity> findByIdWithRelations(@Param("id") Long id);

    @Modifying
    @Query("DELETE FROM KardexEntity k WHERE k.instrumento IN :instrumentos")
    void limpiarPorInstrumentos(@Param("instrumentos") List<InstrumentoEntity> instrumentos);
    
}
