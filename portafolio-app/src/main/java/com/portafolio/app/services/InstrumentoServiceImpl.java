package com.portafolio.app.services;

import com.portafolio.model.dto.InstrumentoDto;
import com.portafolio.model.entities.InstrumentoEntity;
import com.portafolio.model.mappers.InstrumentoMapper;
import com.portafolio.model.repositories.InstrumentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InstrumentoServiceImpl implements InstrumentoService {

    @Autowired
    private InstrumentoRepository instrumentoRepository;

    @Autowired
    private InstrumentoMapper instrumentoMapper;

    @Override // Ahora el @Override es correcto
    public List<InstrumentoDto> findAll() {
        return instrumentoRepository.findAll()
                .stream()
                .map(instrumentoMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override // Ahora el @Override es correcto
    public InstrumentoDto save(InstrumentoDto dto) {
        InstrumentoEntity entity = instrumentoMapper.toEntity(dto);
        InstrumentoEntity savedEntity = instrumentoRepository.save(entity);
        return instrumentoMapper.toDto(savedEntity);
    }
}