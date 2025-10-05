package com.portafolio.persistence.repositorio;

import com.portafolio.model.entities.MovimientoContableEntity;
import com.portafolio.model.enums.TipoEnumsCosteo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MovimientoContableRepository extends JpaRepository<MovimientoContableEntity, Long> {

    // ==================== Búsquedas básicas ====================
    
    /**
     * Busca un movimiento por su tipo de enum.
     */
    Optional<MovimientoContableEntity> findByTipoContable(TipoEnumsCosteo tipoContable);

    /**
     * Busca movimientos cuya descripción contenga un texto específico.
     */
    List<MovimientoContableEntity> findByDescripcionContableContainingIgnoreCase(String descripcion);

    /**
     * Verifica si existe un movimiento con un tipo contable específico.
     */
    boolean existsByTipoContable(TipoEnumsCosteo tipoContable);
    
    /**
     * Devuelve todos los movimientos ordenados por descripción.
     */
    List<MovimientoContableEntity> findAllByOrderByDescripcionContableAsc();
}