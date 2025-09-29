package com.portafolio.model.repositories;

import com.portafolio.model.entities.InstrumentoEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface InstrumentoRepository extends JpaRepository<InstrumentoEntity, Long> {
    
    Optional<InstrumentoEntity> findByInstrumentoNemo(String nemo);
    
    List<InstrumentoEntity> findByProducto_Id(Long productoId);
    
    @Query("SELECT i FROM InstrumentoEntity i JOIN FETCH i.producto WHERE i.instrumentoNemo = :nemo")
    Optional<InstrumentoEntity> findByNemoWithProducto(@Param("nemo") String nemo);
    
    boolean existsByInstrumentoNemo(String nemo);
}
