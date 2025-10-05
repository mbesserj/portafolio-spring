
package com.portafolio.persistence.repositorio;

import com.portafolio.model.entities.InstrumentoEntity;
import com.portafolio.model.entities.ProductoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InstrumentoRepository extends JpaRepository<InstrumentoEntity, Long> {

    Optional<InstrumentoEntity> findByInstrumentoNemo(String nemo);
    
    List<InstrumentoEntity> findByInstrumentoNombreContainingIgnoreCase(String nombre);
    
    boolean existsByInstrumentoNemo(String nemo);
    
    List<InstrumentoEntity> findByProducto(ProductoEntity producto);
    
    List<InstrumentoEntity> findByProductoId(Long productoId);

    @Query("SELECT i FROM InstrumentoEntity i WHERE i.producto.nombre = :nombreProducto")
    List<InstrumentoEntity> findByProductoNombre(@Param("nombreProducto") String nombreProducto);
    
    @Query("SELECT i FROM InstrumentoEntity i JOIN FETCH i.producto")
    List<InstrumentoEntity> findAllWithProducto();

    @Query("SELECT i FROM InstrumentoEntity i JOIN FETCH i.producto WHERE i.id = :id")
    Optional<InstrumentoEntity> findByIdWithProducto(@Param("id") Long id);
    
    List<InstrumentoEntity> findByNombreContainingIgnoreCase(String nombre);
    
    List<InstrumentoEntity> findByProducto_Id(Long productoId);
}