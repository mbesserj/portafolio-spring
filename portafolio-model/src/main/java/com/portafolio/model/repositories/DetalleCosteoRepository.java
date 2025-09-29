
package com.portafolio.model.repositories;

import com.portafolio.model.entities.DetalleCosteoEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DetalleCosteoRepository extends JpaRepository<DetalleCosteoEntity, Long> {
    
    List<DetalleCosteoEntity> findByClaveAgrupacion(String claveAgrupacion);
    
    List<DetalleCosteoEntity> findByIngreso_Id(Long ingresoId);
    
    List<DetalleCosteoEntity> findByEgreso_Id(Long egresoId);
}