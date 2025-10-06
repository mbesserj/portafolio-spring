package com.portafolio.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

// No necesitas constructores ni builders, solo getters y setters para que Spring pueda leer el JSON.
@Getter
@Setter
public class CrearTransaccionDto {

    // =========== Campos básicos ===========
    private LocalDate fechaTransaccion;
    private String folio;
    private String cuenta;
    
    // =========== Campos financieros ===========
    private BigDecimal cantidad;
    private BigDecimal precio;
    private BigDecimal comision;
    private BigDecimal gastos;
    private BigDecimal iva;
    private BigDecimal monto; 
    private String moneda;
    
    // =========== IDs de las entidades relacionadas (¡La clave!) ===========
    private Long empresaId;
    private Long custodioId;
    private Long instrumentoId;
    private Long tipoMovimientoId;
}