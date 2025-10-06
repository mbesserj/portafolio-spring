package com.portafolio.model.dto;

import jakarta.persistence.ColumnResult;
import jakarta.persistence.ConstructorResult;
import jakarta.persistence.SqlResultSetMapping;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO para mapear el resultado de la consulta SQL nativa que confronta
 * y agrupa los saldos de Kardex y la tabla de Saldos.
 */
@SqlResultSetMapping(
    name = "ConfrontaSaldoMapping",
    classes = @ConstructorResult(
        targetClass = ConfrontaSaldoDto.class,
        columns = {
            // Columnas visibles en la tabla
            @ColumnResult(name = "empresa_nombre", type = String.class),
            @ColumnResult(name = "custodio_nombre", type = String.class),
            @ColumnResult(name = "instrumento_nemo", type = String.class),
            @ColumnResult(name = "cuenta", type = String.class),
            @ColumnResult(name = "ultima_fecha_kardex", type = LocalDate.class),
            @ColumnResult(name = "cantidad_kardex", type = BigDecimal.class),
            @ColumnResult(name = "valor_kardex", type = BigDecimal.class),
            @ColumnResult(name = "ultima_fecha_saldos", type = LocalDate.class),
            @ColumnResult(name = "cantidad_mercado", type = BigDecimal.class),
            @ColumnResult(name = "valor_mercado", type = BigDecimal.class),
            @ColumnResult(name = "diferencia_cantidad", type = BigDecimal.class),
            
            // Columnas adicionales para la lógica de negocio (ajustes)
            @ColumnResult(name = "empresa_id", type = Long.class),
            @ColumnResult(name = "custodio_id", type = Long.class),
            @ColumnResult(name = "instrumento_id", type = Long.class),
            @ColumnResult(name = "precio_mercado", type = BigDecimal.class)
        }
    )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfrontaSaldoDto {

    private String empresaNombre;
    private String custodioNombre;
    private String instrumentoNemo;
    private String cuenta;
    private LocalDate ultimaFechaKardex;
    private BigDecimal cantidadKardex;
    private BigDecimal valorKardex;
    private LocalDate ultimaFechaSaldos;
    private BigDecimal cantidadMercado;
    private BigDecimal valorMercado;
    private BigDecimal diferenciaCantidad;
    private Long empresaId;
    private Long custodioId;
    private Long instrumentoId;
    private BigDecimal precioMercado;

}
