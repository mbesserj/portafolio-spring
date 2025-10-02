
package com.model.dto;

import com.model.enums.TipoEnumsCosteo;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KardexDto {

    private Long id;
    private LocalDate fechaTransaccion;
    private String folio;
    private BigDecimal costoUnitario;
    private BigDecimal costoTotal;
    private BigDecimal saldoCantidad;
    private BigDecimal saldoValor;
    private String cuenta;
    private BigDecimal cantidad;
    private BigDecimal cantidadDisponible;
    private TipoEnumsCosteo tipoContable;
    
    // =========== Información de Relaciones Aplanadas ===========
    private Long transaccionId;
    private Long custodioId;
    private String custodioNombre;
    private Long empresaId;
    private String empresaRazonSocial;
    private Long instrumentoId;
    private String instrumentoDisplayName; 
    
    // =========== Metadatos de auditoría ===========
    private LocalDate fechaCreacion;
 
}
