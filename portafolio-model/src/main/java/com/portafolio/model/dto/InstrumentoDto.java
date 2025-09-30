package com.portafolio.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor // Lombok se encarga de todos los constructores
public class InstrumentoDto {
    private Long id;
    private String instrumentoNemo;
    private String instrumentoNombre;
    private LocalDate fechaCreado;
}