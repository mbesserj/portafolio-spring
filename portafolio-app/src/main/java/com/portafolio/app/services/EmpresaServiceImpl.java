package com.portafolio.app.services;

import com.portafolio.model.dto.EmpresaDto;
import com.portafolio.model.entities.EmpresaEntity;
import com.portafolio.model.mappers.EmpresaMapper;
import com.portafolio.model.repositories.EmpresaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmpresaServiceImpl implements EmpresaService {

    @Autowired
    private EmpresaRepository empresaRepository;
    @Autowired
    private EmpresaMapper empresaMapper;

    @Override
    public List<EmpresaDto> findAll() {
        return empresaRepository.findAll().stream()
                .map(empresaMapper::toDto) // Cambiar de toDTO a toDto
                .collect(Collectors.toList());
    }

    @Override
    public EmpresaDto save(EmpresaDto empresaDto) {
        EmpresaEntity entity = empresaMapper.toEntity(empresaDto);
        EmpresaEntity savedEntity = empresaRepository.save(entity);
        return empresaMapper.toDto(savedEntity);  // Cambiar de toDTO a toDto
    }
}
