package com.etl.service;

import com.etl.interfaces.CargaMapperInterfaz;
import com.etl.interfaces.CargaProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class EtlStrategyFactory {
    private final Map<String, CargaProcessor> processors;
    private final Map<String, CargaMapperInterfaz> mappers;

    // Spring automáticamente busca todos los beans de tipo CargaProcessor y CargaMapperInterfaz
    // y los inyecta aquí en estos mapas.
    @Autowired
    public EtlStrategyFactory(Map<String, CargaProcessor> processors, Map<String, CargaMapperInterfaz> mappers) {
        this.processors = processors;
        this.mappers = mappers;
    }

    // Este método reemplaza a tu antiguo getProcessor(key)
    public CargaProcessor getProcessor(String key) {
        return processors.get(key + "Processor");
    }

    // Este método reemplaza a tu antiguo getMapper(key)
    public CargaMapperInterfaz getMapper(String key) {
        return mappers.get(key + "Mapper");
    }
}