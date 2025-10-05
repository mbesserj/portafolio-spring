package com.portafolio.model.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaldoKardexDto {

    private Long id;
    private String cuenta;
    private BigDecimal saldoCantidad;
    private BigDecimal costoTotal;
    private BigDecimal costoPromedio;
    private LocalDate fechaUltimaActualizacion;
    
    // =========== Información de Relaciones Aplanadas ===========
    private Long empresaId;
    private String empresaRazonSocial;
    private Long custodioId;
    private String custodioNombre;
    private Long instrumentoId;
    private String instrumentoDisplayName;
    
    // =========== Metadatos de auditoría ===========
    private LocalDate fechaCreacion;
}