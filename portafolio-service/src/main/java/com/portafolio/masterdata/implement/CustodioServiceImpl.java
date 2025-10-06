package com.portafolio.masterdata.implement;

import com.portafolio.model.dto.ActualizarCustodioDto;
import com.portafolio.model.dto.CrearCustodioDto;
import com.portafolio.model.dto.CustodioDto;
import com.portafolio.model.entities.CustodioEntity;
import com.portafolio.persistence.repositorio.CuentaRepository;
import com.portafolio.persistence.repositorio.CustodioRepository;
import com.portafolio.mapper.CustodioMapper;
import com.portafolio.masterdata.interfaces.CustodioService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CustodioServiceImpl implements CustodioService {

    private static final Logger logger = LoggerFactory.getLogger(CustodioServiceImpl.class);

    private final CustodioRepository custodioRepository;
    private final CuentaRepository cuentaRepository;
    private final CustodioMapper custodioMapper;

    // --- OPERACIONES CRUD ---

    @Override
    public CustodioDto crearCustodio(CrearCustodioDto crearDto) {
        if (custodioRepository.existsByNombreCustodio(crearDto.getNombreCustodio())) {
            throw new IllegalArgumentException("Ya existe un custodio con el nombre: " + crearDto.getNombreCustodio());
        }
        CustodioEntity nuevoCustodio = new CustodioEntity();
        nuevoCustodio.setNombreCustodio(crearDto.getNombreCustodio());
        
        CustodioEntity custodioGuardado = custodioRepository.save(nuevoCustodio);
        logger.info("Custodio '{}' creado exitosamente.", custodioGuardado.getNombreCustodio());
        return custodioMapper.toDtoComplete(custodioGuardado);
    }

    @Override
    public CustodioDto actualizarCustodio(Long id, ActualizarCustodioDto actualizarDto) {
        CustodioEntity custodio = custodioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Custodio no encontrado con id: " + id));

        custodio.setNombreCustodio(actualizarDto.getNombreCustodio());
        
        CustodioEntity custodioActualizado = custodioRepository.save(custodio);
        return custodioMapper.toDtoComplete(custodioActualizado);
    }

    @Override
    public void eliminarCustodio(Long id) {
        if (!custodioRepository.existsById(id)) {
            throw new EntityNotFoundException("Custodio no encontrado con id: " + id);
        }
        custodioRepository.deleteById(id);
        logger.info("Custodio con ID {} eliminado.", id);
    }

    // --- OPERACIONES DE LECTURA ---

    @Override
    @Transactional(readOnly = true)
    public List<CustodioDto> obtenerTodos(boolean detallesCompletos) {
        List<CustodioEntity> custodios = custodioRepository.findAllByOrderByNombreCustodioAsc();
        return detallesCompletos ? custodioMapper.toDtoCompleteList(custodios) : custodioMapper.toDtoBasicList(custodios);
    }

    @Override
    @Transactional(readOnly = true)
    public CustodioDto obtenerPorId(Long id) {
        CustodioEntity custodio = custodioRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new EntityNotFoundException("Custodio no encontrado con id: " + id));
        return custodioMapper.toDtoComplete(custodio);
    }

    @Transactional(readOnly = true)
    public List<CustodioEntity> obtenerCustodiosPorEmpresa(Long empresaId) {
        if (empresaId == null) {
            return Collections.emptyList();
        }
        return custodioRepository.findByEmpresaId(empresaId);
    }

    @Transactional(readOnly = true)
    public List<String> obtenerCuentasPorCustodioYEmpresa(Long custodioId, Long empresaId) {
        if (custodioId == null || empresaId == null) {
            return Collections.emptyList();
        }
        return cuentaRepository.findCuentasByCustodioAndEmpresa(custodioId, empresaId);
    }


    @Override
    public CustodioEntity findOrCreateByNombre(String custodioNombre) {
        if (custodioNombre == null || custodioNombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del custodio no puede ser nulo o vacío.");
        }
        // Busca el custodio por su nombre. Si no existe, ejecuta el código dentro de orElseGet.
        return custodioRepository.findByNombreCustodio(custodioNombre.trim())
                .orElseGet(() -> {
                    logger.info("Custodio '{}' no encontrado. Creando uno nuevo.", custodioNombre);
                    CustodioEntity nuevoCustodio = new CustodioEntity();
                    nuevoCustodio.setNombreCustodio(custodioNombre.trim());
                    return custodioRepository.save(nuevoCustodio);
                });
    }
}