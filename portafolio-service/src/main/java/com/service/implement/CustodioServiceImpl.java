package com.service.implement;

import com.model.dto.ActualizarCustodioDto;
import com.model.dto.CrearCustodioDto;
import com.model.dto.CustodioDto;
import com.model.entities.CustodioEntity;
import com.persistence.repositorio.CustodioRepository;
import com.service.interfaces.CustodioService;
import com.service.mapper.CustodioMapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CustodioServiceImpl implements CustodioService {

    private final CustodioRepository custodioRepository;
    private final CustodioMapper custodioMapper;

    public CustodioServiceImpl(CustodioRepository custodioRepository, CustodioMapper custodioMapper) {
        this.custodioRepository = custodioRepository;
        this.custodioMapper = custodioMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustodioDto> obtenerTodos(boolean detallesCompletos) {
        List<CustodioEntity> custodios = custodioRepository.findAllByOrderByNombreCustodioAsc();
        if (detallesCompletos) {
            return custodioMapper.toDtoCompleteList(custodios);
        }
        return custodioMapper.toDtoBasicList(custodios);
    }

    @Override
    @Transactional(readOnly = true)
    public CustodioDto obtenerPorId(Long id) {
        // Usamos el método optimizado para cargar relaciones
        CustodioEntity custodio = custodioRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new EntityNotFoundException("Custodio no encontrado con id: " + id));
        return custodioMapper.toDtoComplete(custodio);
    }

    @Override
    public CustodioDto crearCustodio(CrearCustodioDto crearDto) {
        // Regla de negocio: No permitir nombres duplicados.
        if (custodioRepository.existsByNombreCustodio(crearDto.getNombreCustodio())) {
            throw new IllegalArgumentException("Ya existe un custodio con el nombre: " + crearDto.getNombreCustodio());
        }

        // Usamos el método del mapper para crear la entidad
        CustodioEntity nuevoCustodio = custodioMapper.createNewCustodio(crearDto);
        CustodioEntity custodioGuardado = custodioRepository.save(nuevoCustodio);

        return custodioMapper.toDtoComplete(custodioGuardado);
    }

    @Override
    public CustodioDto actualizarCustodio(Long id, ActualizarCustodioDto actualizarDto) {
        CustodioEntity custodio = custodioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Custodio no encontrado con id: " + id));

        // Regla de negocio: Verificar que el nuevo nombre no esté ya en uso por OTRO custodio.
        Optional<CustodioEntity> custodioConMismoNombre = custodioRepository.findByNombreCustodio(actualizarDto.getNombreCustodio());
        if (custodioConMismoNombre.isPresent() && !custodioConMismoNombre.get().getId().equals(id)) {
            throw new IllegalArgumentException("El nombre '" + actualizarDto.getNombreCustodio() + "' ya está en uso por otro custodio.");
        }

        // Usamos el mapper para una actualización segura
        custodioMapper.updateEntityFromDto(actualizarDto, custodio);

        CustodioEntity custodioActualizado = custodioRepository.save(custodio);
        return custodioMapper.toDtoComplete(custodioActualizado);
    }

    @Override
    public void eliminarCustodio(Long id) {
        // Regla de negocio: No permitir eliminar un custodio si tiene relaciones activas.
        CustodioEntity custodio = custodioRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new EntityNotFoundException("Custodio no encontrado con id: " + id));

        if (custodio.tieneEmpresas() || custodio.tieneCuentas() || custodio.tieneTransacciones()) {
            StringBuilder errorMsg = new StringBuilder("No se puede eliminar el custodio '");
            errorMsg.append(custodio.getNombreCustodio()).append("' porque tiene relaciones activas: ");
            if (custodio.tieneEmpresas()) {
                errorMsg.append(custodio.getTotalEmpresas()).append(" empresas. ");
            }
            if (custodio.tieneCuentas()) {
                errorMsg.append(custodio.getTotalCuentas()).append(" cuentas. ");
            }
            if (custodio.tieneTransacciones()) {
                errorMsg.append(custodio.getTotalTransacciones()).append(" transacciones.");
            }
            throw new IllegalStateException(errorMsg.toString());
        }

        custodioRepository.delete(custodio);
    }

    @Override
    public CustodioEntity findOrCreateByNombre(String nombreCustodio) {
        return custodioRepository.findByNombreCustodio(nombreCustodio)
                .orElseGet(() -> {
                    if (nombreCustodio == null || nombreCustodio.trim().isEmpty()) {
                        throw new IllegalArgumentException("El nombre del custodio no puede ser nulo o vacío.");
                    }
                    CustodioEntity nuevoCustodio = new CustodioEntity();
                    nuevoCustodio.setNombreCustodio(nombreCustodio);
                    return custodioRepository.save(nuevoCustodio);
                });
    }
}