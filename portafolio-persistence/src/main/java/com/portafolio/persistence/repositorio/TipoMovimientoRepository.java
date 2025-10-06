package com.portafolio.persistence.repositorio;

import com.portafolio.model.entities.TipoMovimientoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para gestión de tipos de movimiento.
 */
@Repository
public interface TipoMovimientoRepository extends JpaRepository<TipoMovimientoEntity, Long> {

    /**
     * Busca un tipo de movimiento por su tipo.
     */
    Optional<TipoMovimientoEntity> findByTipoMovimiento(String tipoMovimiento);
    
    /**
     * Busca tipos de movimiento que sean saldo inicial.
     */
    Optional<TipoMovimientoEntity> findByEsSaldoInicialTrue();
    
    Optional<TipoMovimientoEntity> findByIdWithMovimientoContable(Long id);
}