package com.portafolio.etl.service;

import com.portafolio.model.dto.ResultadoCargaDto;
import com.portafolio.model.enums.ListaEnumsCustodios;
import com.portafolio.etl.interfaces.CargaMapperInterfaz;
import com.portafolio.etl.interfaces.CargaProcessor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;

@Service
public class EtlServiceImpl implements EtlService {

    private static final Logger logger = LoggerFactory.getLogger(EtlServiceImpl.class);
    private final EtlStrategyFactory strategyFactory;

    @Autowired
    public EtlServiceImpl(EtlStrategyFactory strategyFactory) {
        this.strategyFactory = strategyFactory;
    }

    @Override
    @Transactional
    public ResultadoCargaDto procesarArchivo(InputStream inputStream, ListaEnumsCustodios custodio) {
        String bankKey = custodio.name().toLowerCase();
        logger.info("Iniciando procesamiento de archivo para el custodio: {}", bankKey);
        
        Instant inicio = Instant.now();
        int filasProcesadas = 0;
        int errores = 0;

        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            // Obtenemos el procesador una sola vez, ya que es el mismo para todo el archivo del banco.
            CargaProcessor processor = strategyFactory.getProcessor(bankKey);

            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                
                // Determinamos qué tipo de hoja es (S, T, C, etc.)
                String sheetType = determineSheetType(bankKey, i, sheet.getSheetName());
                if (sheetType.isBlank()) {
                    logger.warn("Omitiendo hoja '{}' porque no tiene un tipo definido.", sheet.getSheetName());
                    continue;
                }
                
                logger.info("Procesando hoja '{}' (tipo: {})", sheet.getSheetName(), sheetType);
                String mapperKey = bankKey + "_" + sheetType; // ej: "banchile_S", "fynsa_T"

                // Obtenemos el mapper específico para esta hoja
                CargaMapperInterfaz mapper = strategyFactory.getMapper(mapperKey);

                int headerRowIndex = getHeaderRowIndexFor(bankKey, sheetType);
                for (int j = headerRowIndex; j <= sheet.getLastRowNum(); j++) {
                    Row row = sheet.getRow(j);
                    if (row == null) continue;

                    try {
                        Object dto = mapper.map(row, j + 1, "cargado_desde_app");
                        if (dto != null) {
                            processor.procesar(dto);
                            filasProcesadas++;
                        }
                    } catch (Exception e) {
                        errores++;
                        logger.error("Error al procesar la fila {} de la hoja '{}': {}", j + 1, sheet.getSheetName(), e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error crítico al procesar el archivo para {}: {}", bankKey, e.getMessage(), e);
            return ResultadoCargaDto.fallido("Error al procesar el archivo: " + e.getMessage());
        }
        
        Duration duracion = Duration.between(inicio, Instant.now());
        String mensaje = String.format("Carga a staging completada. Filas procesadas: %d, Errores: %d.", filasProcesadas, errores);
        return ResultadoCargaDto.exitoso(filasProcesadas, duracion, mensaje);
    }

    /**
     * Centraliza la lógica para determinar el tipo de hoja.
     */
    private String determineSheetType(String bankKey, int sheetIndex, String sheetName) {
        if ("fynsa".equalsIgnoreCase(bankKey)) {
            if (sheetName.toLowerCase().startsWith("stock")) return "S";
            if (sheetName.toLowerCase().startsWith("mvto")) {
                if (sheetIndex == 0) return "C"; // Asumiendo un orden específico si los nombres son iguales
                if (sheetIndex == 1) return "T";
            }
            return "";
        } else if ("banchile".equalsIgnoreCase(bankKey)) {
            if (sheetIndex == 0) return "S";
            if (sheetIndex == 1) return "T";
            return "T"; // Valor por defecto para otras hojas de BanChile
        }
        return "";
    }

    /**
     * Centraliza la lógica para saber en qué fila empiezan los datos.
     */
    private int getHeaderRowIndexFor(String bankKey, String sheetType) {
        if ("banchile".equals(bankKey)) {
            return 4; 
        }
        return 1; 
    }
}