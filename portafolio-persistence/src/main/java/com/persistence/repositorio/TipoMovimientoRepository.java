package com.persistence.repositorio;

import com.model.entities.MovimientoContableEntity;
import com.model.entities.TipoMovimientoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TipoMovimientoRepository extends JpaRepository<TipoMovimientoEntity, Long> {

    // ==================== Búsquedas básicas ====================
    Optional<TipoMovimientoEntity> findByTipoMovimiento(String tipoMovimiento);
    
    List<TipoMovimientoEntity> findByEsSaldoInicial(boolean esSaldoInicial);

    // ==================== Búsquedas por relación ====================
    List<TipoMovimientoEntity> findByMovimientoContable(MovimientoContableEntity movimientoContable);
    
    // ==================== Consulta de rendimiento (Evitar N+1) ====================
    @Query("SELECT tm FROM TipoMovimientoEntity tm LEFT JOIN FETCH tm.movimientoContable WHERE tm.id = :id")
    Optional<TipoMovimientoEntity> findByIdWithMovimientoContable(@Param("id") Long id);
        
}