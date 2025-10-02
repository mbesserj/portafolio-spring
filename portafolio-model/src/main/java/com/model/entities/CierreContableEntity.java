package com.model.entities;

import com.model.utiles.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "cierres_contables",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_cierre_ejercicio_empresa_custodio_instrumento",
        columnNames = {"ejercicio", "empresa_id", "custodio_id", "instrumento_id"}
    )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder  
@EqualsAndHashCode(callSuper = true, exclude = {"empresa", "custodio", "instrumento"})
public class CierreContableEntity extends BaseEntity implements Serializable {

    @Column(name = "ejercicio", nullable = false)
    private Integer ejercicio;  

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private EmpresaEntity empresa;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "custodio_id", nullable = false)
    private CustodioEntity custodio;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "instrumento_id", nullable = false)
    private InstrumentoEntity instrumento;

    @Column(name = "cantidad_cierre", nullable = false, precision = 18, scale = 4)
    private BigDecimal cantidadCierre;

    @Column(name = "valor_cierre", nullable = false, precision = 18, scale = 4)
    private BigDecimal valorCierre;

    /**
     * Calcula el valor unitario del cierre
     * @return valor unitario (valor_cierre / cantidad_cierre)
     */
    public BigDecimal getValorUnitario() {
        if (cantidadCierre == null || cantidadCierre.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return valorCierre.divide(cantidadCierre, 4, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Genera una clave única para identificar el cierre
     * @return clave en formato "ejercicio|empresaId|custodioId|instrumentoId"
     */
    public String getClaveCierre() {
        if (ejercicio == null || empresa == null || custodio == null || instrumento == null) {
            return "invalid_key";
        }
        return ejercicio + "|" + empresa.getId() + "|" + custodio.getId() + "|" + instrumento.getId();
    }

    @Override
    public String toString() {
        return String.format("CierreContable[ejercicio=%d, empresa=%s, custodio=%s, instrumento=%s, cantidad=%s, valor=%s]",
                ejercicio,
                empresa != null ? empresa.getRazonSocial() : "null",
                custodio != null ? custodio.getNombreCustodio() : "null",
                instrumento != null ? instrumento.getInstrumentoNemo() : "null",
                cantidadCierre,
                valorCierre);
    }
}