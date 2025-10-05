package com.portafolio.ui.masterdata.implement;

import com.portafolio.model.dto.GrupoEmpresaDto;
import com.portafolio.model.entities.GrupoEmpresaEntity;
import com.portafolio.ui.mapper.GrupoEmpresaMapper;
import com.portafolio.persistence.repositorio.GrupoEmpresaRepository;
import com.portafolio.ui.masterdata.interfaces.GrupoEmpresaService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service // Anotación que le dice a Spring que esta es una clase de servicio
public class GrupoEmpresaServiceImpl implements GrupoEmpresaService {

    private final GrupoEmpresaRepository grupoEmpresaRepository;
    private final GrupoEmpresaMapper grupoEmpresaMapper;

    // Inyectamos nuestras "herramientas" a través del constructor
    @Autowired
    public GrupoEmpresaServiceImpl(GrupoEmpresaRepository grupoEmpresaRepository, GrupoEmpresaMapper grupoEmpresaMapper) {
        this.grupoEmpresaRepository = grupoEmpresaRepository;
        this.grupoEmpresaMapper = grupoEmpresaMapper;
    }

    @Override
    @Transactional(readOnly = true) // Optimización para consultas de solo lectura
    public List<GrupoEmpresaDto> findAllBasic() {
        // 1. Llama al repositorio para obtener las entidades
        List<GrupoEmpresaEntity> entities = grupoEmpresaRepository.findAll();
        // 2. Usa la vista "basic" del mapper para convertirlas a DTOs
        return grupoEmpresaMapper.toDtoBasicList(entities);
    }

    @Override
    @Transactional(readOnly = true)
    public GrupoEmpresaDto findByIdComplete(Long id) {
        // 1. Usa el método optimizado del repositorio
        GrupoEmpresaEntity entity = grupoEmpresaRepository.findByIdWithEmpresas(id)
                .orElseThrow(() -> new EntityNotFoundException("GrupoEmpresa no encontrado con id: " + id));
        // 2. Usa la vista "complete" del mapper para el detalle
        return grupoEmpresaMapper.toDtoComplete(entity);
    }

    @Override
    @Transactional // Transacción de escritura
    public GrupoEmpresaDto save(GrupoEmpresaDto dto) {
        // 1. Convierte el DTO a una entidad nueva
        GrupoEmpresaEntity newEntity = grupoEmpresaMapper.toEntity(dto);
        // 2. Guarda la entidad en la base de datos
        GrupoEmpresaEntity savedEntity = grupoEmpresaRepository.save(newEntity);
        // 3. Devuelve el DTO completo de la entidad recién guardada (con su nuevo ID)
        return grupoEmpresaMapper.toDtoComplete(savedEntity);
    }

    @Override
    @Transactional
    public GrupoEmpresaDto update(Long id, GrupoEmpresaDto dto) {
        // 1. Busca la entidad que se quiere actualizar
        GrupoEmpresaEntity existingEntity = grupoEmpresaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("GrupoEmpresa no encontrado con id: " + id));
        
        // 2. Usa el método de actualización del mapper para aplicar solo los cambios del DTO
        grupoEmpresaMapper.updateEntityFromDto(dto, existingEntity);
        
        // 3. Guarda la entidad actualizada
        GrupoEmpresaEntity updatedEntity = grupoEmpresaRepository.save(existingEntity);
        
        // 4. Devuelve el DTO completo y actualizado
        return grupoEmpresaMapper.toDtoComplete(updatedEntity);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        // Es una buena práctica verificar si la entidad existe antes de intentar borrarla
        if (!grupoEmpresaRepository.existsById(id)) {
            throw new EntityNotFoundException("GrupoEmpresa no encontrado con id: " + id);
        }
        grupoEmpresaRepository.deleteById(id);
    }
}