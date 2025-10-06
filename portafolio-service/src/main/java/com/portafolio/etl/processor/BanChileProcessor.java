package com.portafolio.etl.processor;

import com.portafolio.model.dto.CartolaBanChileDto;
import com.portafolio.model.entities.CargaTransaccionEntity;
import com.portafolio.model.utiles.Pk;
import com.portafolio.persistence.repositorio.CargaTransaccionRepository;
import com.portafolio.etl.interfaces.CargaProcessor;
import com.portafolio.etl.mapper.CartolaBanChileMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("banChileProcessor") // Le damos un nombre para que la fábrica lo encuentre
public class BanChileProcessor implements CargaProcessor<CartolaBanChileDto> {

    private static final Logger logger = LoggerFactory.getLogger(BanChileProcessor.class);

    private final CargaTransaccionRepository cargaRepo;
    private final CartolaBanChileMapper banChileMapper;

    @Autowired
    public BanChileProcessor(CargaTransaccionRepository cargaRepo, CartolaBanChileMapper banChileMapper) {
        this.cargaRepo = cargaRepo;
        this.banChileMapper = banChileMapper;
    }

    @Override
    @Transactional
    public void procesar(CartolaBanChileDto dto) {
        if (dto == null) {
            return;
        }

        // 1. Delegamos la conversión al Mapper especializado
        CargaTransaccionEntity entity = banChileMapper.toCargaTransaccionEntity(dto);

        // 2. Construimos la clave primaria para la búsqueda
        Pk id = new Pk(entity.getFechaTransaccion(), entity.getRowNum(), entity.getTipoClase());

        // 3. Usamos el Repositorio para verificar si ya existe y para guardar
        if (!cargaRepo.existsById(id)) {
            cargaRepo.save(entity);
        } else {
            logger.info("Registro de BanChile ya existe en staging, omitiendo: {}", id);
        }
    }
}