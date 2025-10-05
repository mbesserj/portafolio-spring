package com.portafolio.etl.processor;

import com.portafolio.model.dto.CartolaFynsaDto;
import com.portafolio.model.entities.CargaTransaccionEntity;
import com.portafolio.model.utiles.Pk;
import com.portafolio.persistence.repositorio.CargaTransaccionRepository;
import com.portafolio.etl.interfaces.CargaProcessor;
import com.portafolio.ui.mapper.CartolaFynsaMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("fynsaProcessor") // Le damos un nombre único para que la fábrica lo encuentre
public class FynsaProcessor implements CargaProcessor<CartolaFynsaDto> {

    private static final Logger logger = LoggerFactory.getLogger(FynsaProcessor.class);

    private final CargaTransaccionRepository cargaRepo;
    private final CartolaFynsaMapper fynsaMapper;

    @Autowired
    public FynsaProcessor(CargaTransaccionRepository cargaRepo, CartolaFynsaMapper fynsaMapper) {
        this.cargaRepo = cargaRepo;
        this.fynsaMapper = fynsaMapper;
    }

    @Override
    @Transactional
    public void procesar(CartolaFynsaDto dto) {
        if (dto == null) {
            return;
        }

        // 1. Delegamos la conversión al Mapper especializado
        CargaTransaccionEntity entity = fynsaMapper.toCargaTransaccionEntity(dto);

        // 2. Construimos la clave primaria para la búsqueda
        Pk id = new Pk(entity.getFechaTransaccion(), entity.getRowNum(), entity.getTipoClase());

        // 3. Usamos el Repositorio para verificar si ya existe y para guardar
        if (!cargaRepo.existsById(id)) {
            cargaRepo.save(entity);
        } else {
            logger.info("Registro de Fynsa ya existe en staging, omitiendo: {}", id);
        }
    }
}