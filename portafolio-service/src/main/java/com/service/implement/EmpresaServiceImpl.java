package com.service.implement;

import com.model.dto.ActualizarEmpresaDto;
import com.model.dto.CrearEmpresaDto;
import com.model.dto.EmpresaDto;
import com.model.entities.CustodioEntity;
import com.model.entities.EmpresaEntity;
import com.model.entities.GrupoEmpresaEntity;
import com.persistence.repositorio.CustodioRepository;
import com.persistence.repositorio.EmpresaRepository;
import com.persistence.repositorio.GrupoEmpresaRepository;
import com.service.interfaces.EmpresaService;
import com.service.mapper.EmpresaMapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional // Anotación a nivel de clase para que todos los métodos sean transaccionales
public class EmpresaServiceImpl implements EmpresaService {

    // === Inyección de todas las dependencias necesarias ===
    private final EmpresaRepository empresaRepository;
    private final CustodioRepository custodioRepository;
    private final GrupoEmpresaRepository grupoEmpresaRepository;
    private final EmpresaMapper empresaMapper;

    public EmpresaServiceImpl(EmpresaRepository empresaRepository,
                              CustodioRepository custodioRepository,
                              GrupoEmpresaRepository grupoEmpresaRepository,
                              EmpresaMapper empresaMapper) {
        this.empresaRepository = empresaRepository;
        this.custodioRepository = custodioRepository;
        this.grupoEmpresaRepository = grupoEmpresaRepository;
        this.empresaMapper = empresaMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmpresaDto> obtenerTodas(boolean detallesCompletos) {
        List<EmpresaEntity> empresas = empresaRepository.findAllByOrderByRazonSocialAsc();
        if (detallesCompletos) {
            // Usa el método del mapper que creaste para DTOs completos
            return empresaMapper.toDtoCompleteList(empresas);
        }
        // Usa el método del mapper para DTOs básicos (ideal para listas)
        return empresaMapper.toDtoBasicList(empresas);
    }

    @Override
    @Transactional(readOnly = true)
    public EmpresaDto obtenerPorId(Long id) {
        // Usa el método optimizado del repositorio para cargar relaciones
        EmpresaEntity empresa = empresaRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new EntityNotFoundException("Empresa no encontrada con id: " + id));
        // Usa el mapper para la conversión completa
        return empresaMapper.toDtoComplete(empresa);
    }

    @Override
    public EmpresaDto crearEmpresa(CrearEmpresaDto crearDto) {
        if (empresaRepository.existsByRut(crearDto.getRut())) {
            throw new IllegalArgumentException("Ya existe una empresa con el RUT " + crearDto.getRut());
        }

        EmpresaEntity nuevaEmpresa = new EmpresaEntity();
        nuevaEmpresa.setRut(crearDto.getRut());
        nuevaEmpresa.setRazonSocial(crearDto.getRazonSocial());

        if (crearDto.getGrupoEmpresaId() != null) {
            GrupoEmpresaEntity grupo = grupoEmpresaRepository.findById(crearDto.getGrupoEmpresaId())
                    .orElseThrow(() -> new EntityNotFoundException("Grupo Empresa no encontrado con id: " + crearDto.getGrupoEmpresaId()));
            nuevaEmpresa.setGrupoEmpresa(grupo);
        }

        EmpresaEntity empresaGuardada = empresaRepository.save(nuevaEmpresa);
        return empresaMapper.toDtoComplete(empresaGuardada);
    }

    @Override
    public EmpresaDto actualizarEmpresa(Long id, ActualizarEmpresaDto actualizarDto) {
        EmpresaEntity empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Empresa no encontrada con id: " + id));

        // El mapper actualiza solo los campos presentes en el DTO
        empresa.setRazonSocial(actualizarDto.getRazonSocial());

        if (actualizarDto.getGrupoEmpresaId() != null) {
            GrupoEmpresaEntity grupo = grupoEmpresaRepository.findById(actualizarDto.getGrupoEmpresaId())
                    .orElseThrow(() -> new EntityNotFoundException("Grupo Empresa no encontrado"));
            empresa.setGrupoEmpresa(grupo);
        } else {
            empresa.setGrupoEmpresa(null);
        }
        
        empresaRepository.save(empresa);
        return empresaMapper.toDtoComplete(empresa);
    }

    @Override
    public void eliminarEmpresa(Long id) {
        if (!empresaRepository.existsById(id)) {
            throw new EntityNotFoundException("Empresa no encontrada con id: " + id);
        }
        // Aquí podrías añadir lógica para verificar si la empresa tiene transacciones antes de borrarla
        empresaRepository.deleteById(id);
    }

    @Override
    public EmpresaDto asociarCustodio(Long empresaId, Long custodioId) {
        EmpresaEntity empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new EntityNotFoundException("Empresa no encontrada"));
        CustodioEntity custodio = custodioRepository.findById(custodioId)
                .orElseThrow(() -> new EntityNotFoundException("Custodio no encontrado"));

        // Usa los métodos de conveniencia que creaste en tu entidad
        empresa.addCustodio(custodio);
        
        empresaRepository.save(empresa); // JPA se encarga de la tabla de unión
        return empresaMapper.toDtoComplete(empresa);
    }

    @Override
    public EmpresaDto desasociarCustodio(Long empresaId, Long custodioId) {
        EmpresaEntity empresa = empresaRepository.findByIdWithRelations(empresaId) // Con relaciones para evitar carga perezosa
                .orElseThrow(() -> new EntityNotFoundException("Empresa no encontrada"));
        CustodioEntity custodio = custodioRepository.findById(custodioId)
                .orElseThrow(() -> new EntityNotFoundException("Custodio no encontrado"));

        empresa.removeCustodio(custodio);
        
        empresaRepository.save(empresa);
        return empresaMapper.toDtoComplete(empresa);
    }

    @Override
    public EmpresaEntity findOrCreateByRut(String razonSocial, String rut) {
        if (rut == null || rut.trim().isEmpty()) {
            throw new IllegalArgumentException("El RUT no puede ser nulo o vacío.");
        }
        return empresaRepository.findByRut(rut)
                .orElseGet(() -> {
                    if (razonSocial == null || razonSocial.trim().isEmpty()) {
                        throw new IllegalArgumentException("La razón social no puede ser nula o vacía al crear una nueva empresa.");
                    }                    
                    EmpresaEntity nuevaEmpresa = new EmpresaEntity();
                    nuevaEmpresa.setRut(rut);
                    nuevaEmpresa.setRazonSocial(razonSocial);
                    return empresaRepository.save(nuevaEmpresa);
                });
    }
}