package com.persistence.repositorio;

import com.model.entities.ProductoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<ProductoEntity, Long> {

    // ==================== Búsquedas básicas ====================
    Optional<ProductoEntity> findByProducto(String producto);
    
    List<ProductoEntity> findByProductoContainingIgnoreCase(String producto);
    
    boolean existsByProducto(String producto);

    // ==================== Búsquedas por relación ====================
    @Query("SELECT p FROM ProductoEntity p WHERE SIZE(p.instrumentos) > 0")
    List<ProductoEntity> findProductosConInstrumentos();
    
    // ==================== Consulta de rendimiento (Evitar N+1) ====================
    @Query("SELECT p FROM ProductoEntity p LEFT JOIN FETCH p.instrumentos WHERE p.id = :id")
    Optional<ProductoEntity> findByIdWithInstrumentos(@Param("id") Long id);
    
    List<ProductoEntity> findAllOrderByProductoAsc();
    
}