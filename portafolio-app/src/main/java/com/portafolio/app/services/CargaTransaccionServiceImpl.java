package com.portafolio.app.services;

import com.portafolio.model.dto.CargaTransaccionDto;
import com.portafolio.model.entities.CargaTransaccionEntity;
import com.portafolio.model.mappers.CargaTransaccionMapper;
import com.portafolio.model.repositories.CargaTransaccionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CargaTransaccionServiceImpl implements CargaTransaccionService {

    @Autowired
    private CargaTransaccionRepository cargaTransaccionRepository;
    @Autowired
    private CargaTransaccionMapper cargaTransaccionMapper;

    @Override
    public List<CargaTransaccionDto> findAll() {
        return cargaTransaccionRepository.findAll().stream()
                .map(cargaTransaccionMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CargaTransaccionDto save(CargaTransaccionDto cargaTransaccionDto) {
        CargaTransaccionEntity entity = cargaTransaccionMapper.toEntity(cargaTransaccionDto);
        CargaTransaccionEntity savedEntity = cargaTransaccionRepository.save(entity);
        return cargaTransaccionMapper.toDto(savedEntity);
    }
}