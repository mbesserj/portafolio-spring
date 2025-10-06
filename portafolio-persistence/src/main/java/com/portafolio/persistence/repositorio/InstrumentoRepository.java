package com.portafolio.persistence.repositorio;

import com.portafolio.model.entities.InstrumentoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InstrumentoRepository extends JpaRepository<InstrumentoEntity, Long> {

    /**
     * Busca un instrumento por su "nemo" (ticker), ignorando mayúsculas/minúsculas.
     */
    Optional<InstrumentoEntity> findByInstrumentoNemoIgnoreCase(String nemo);

    /**
     * Obtiene una lista de todos los instrumentos, ordenados alfabéticamente por su nemo.
     */
    List<InstrumentoEntity> findAllByOrderByInstrumentoNemoAsc();

    /**
     * Obtiene una lista única de instrumentos que tienen transacciones asociadas
     * para una empresa, custodio y cuenta específicos.
     * Esta consulta reemplaza la lógica del CriteriaBuilder.
     */
    @Query("SELECT DISTINCT t.instrumento FROM TransaccionEntity t " +
           "WHERE t.empresa.id = :empresaId " +
           "AND t.custodio.id = :custodioId " +
           "AND t.cuenta = :cuenta " +
           "ORDER BY t.instrumento.instrumentoNemo ASC")
    List<InstrumentoEntity> findInstrumentosConTransacciones(
            @Param("empresaId") Long empresaId,
            @Param("custodioId") Long custodioId,
            @Param("cuenta") String cuenta);

    /**
     * Obtiene una lista única de instrumentos asociados a una empresa y custodio específicos
     * a través de las transacciones.
     */
    @Query("SELECT DISTINCT t.instrumento FROM TransaccionEntity t " +
           "WHERE t.custodio.nombreCustodio = :nombreCustodio " +
           "AND t.empresa.razonSocial = :razonSocial " +
           "ORDER BY t.instrumento.instrumentoNemo ASC")
    List<InstrumentoEntity> findInstrumentosPorCustodioYEmpresa(
            @Param("nombreCustodio") String nombreCustodio,
            @Param("razonSocial") String razonSocial);
    
    Optional<InstrumentoEntity> findByInstrumentoNemo(String nemo);
    
    Optional<InstrumentoEntity> findByIdWithProducto(Long id);
    
}