package com.portafolio.persistence.repositorio;

import com.portafolio.model.entities.InstrumentoEntity;
import com.portafolio.model.entities.KardexEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;

@Repository
public interface KardexRepository extends JpaRepository<KardexEntity, Long> {

    List<KardexEntity> findByFolio(String folio);

    List<KardexEntity> findByFechaTransaccionBetween(LocalDate start, LocalDate end);

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

    /**
     * Busca kardex por grupo ordenado por fecha e ID
     */
    List<KardexEntity> findByEmpresaIdAndCustodioIdAndInstrumentoIdAndCuentaOrderByFechaTransaccionAscIdAsc(
            Long empresaId, Long custodioId, Long instrumentoId, String cuenta);

    /**
     * Busca kardex por grupo con paginación
     */
    Page<KardexEntity> findByEmpresaIdAndCustodioIdAndInstrumentoIdAndCuenta(
            Long empresaId, Long custodioId, Long instrumentoId, String cuenta, Pageable pageable);

    /**
     * Busca kardex por grupo (sin paginación)
     */
    List<KardexEntity> findByEmpresaIdAndCustodioIdAndInstrumentoIdAndCuenta(
            Long empresaId, Long custodioId, Long instrumentoId, String cuenta);

    @Modifying
    @Query("DELETE FROM KardexEntity k WHERE k.instrumento IN :instrumentos")
    void limpiarPorInstrumentos(@Param("instrumentos") List<InstrumentoEntity> instrumentos);

}
