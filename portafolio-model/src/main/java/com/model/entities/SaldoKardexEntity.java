package com.model.entities; // O el paquete que corresponda

import com.model.utiles.BaseEntity;
import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "saldos_kardex", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"empresa_id", "custodio_id", "instrumento_id", "cuenta"}, name = "uk_saldo_kardex_grupo")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true, exclude = {"empresa", "custodio", "instrumento"})
public class SaldoKardexEntity extends BaseEntity implements Serializable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private EmpresaEntity empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "custodio_id", nullable = false)
    private CustodioEntity custodio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instrumento_id", nullable = false)
    private InstrumentoEntity instrumento;
    
    @Column(name = "cuenta", nullable = false)
    private String cuenta;

    @Column(name = "saldo_cantidad", precision = 19, scale = 6, nullable = false)
    private BigDecimal saldoCantidad;

    @Column(name = "costo_total", precision = 19, scale = 6, nullable = false)
    private BigDecimal costoTotal;

    @Column(name = "costo_promedio", precision = 19, scale = 6, nullable = false)
    private BigDecimal costoPromedio;
    
    @Column(name = "fecha_ultima_actualizacion", nullable = false)
    private LocalDate fechaUltimaActualizacion;

    public void recalcularCostoPromedio() {
        if (this.saldoCantidad != null && this.saldoCantidad.compareTo(BigDecimal.ZERO) != 0) {
            this.costoPromedio = this.costoTotal.divide(this.saldoCantidad, 6, RoundingMode.HALF_UP);
        } else {
            this.costoPromedio = BigDecimal.ZERO;
        }
    }
}