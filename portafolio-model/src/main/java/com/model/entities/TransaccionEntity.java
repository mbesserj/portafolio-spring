package com.model.entities;

import com.model.utiles.BaseEntity;
import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "transacciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder 
@EqualsAndHashCode(callSuper = true, exclude = {"empresa", "instrumento", "custodio", "tipoMovimiento"}) 
public class TransaccionEntity extends BaseEntity implements Serializable {

    @Column(name = "fecha_transaccion", nullable = false)
    private LocalDate fechaTransaccion;

    @Column(name = "folio", length = 50)
    private String folio;

    @Column(name = "cuenta")
    private String cuenta;

    @Column(name = "glosa")
    private String glosa;

    @Column(name = "cantidad", precision = 19, scale = 6)
    private BigDecimal cantidad;

    @Column(name = "precio", precision = 19, scale = 6)
    private BigDecimal precio;

    @Column(name = "comision", precision = 19, scale = 6)
    private BigDecimal comision;

    @Column(name = "gasto", precision = 19, scale = 6)
    private BigDecimal gastos;

    @Column(name = "iva", precision = 19, scale = 6)
    private BigDecimal iva;

    @Column(name = "monto_total", precision = 19, scale = 6)
    private BigDecimal montoTotal;

    @Column(name = "monto", precision = 19, scale = 6)
    private BigDecimal monto;

    @Column(name = "monto_clp", precision = 19, scale = 6)
    private BigDecimal montoClp;

    @Column(name = "moneda", length = 3)
    private String moneda;

    @Column(name = "costeado")
    private boolean costeado;

    @Column(name = "para_revision")
    @Builder.Default
    private boolean paraRevision = false;

    @Column(name = "ignorar_en_costeo")
    @Builder.Default
    private boolean ignorarEnCosteo = false;

    // --- RELACIONES MEJORADAS ---
    
    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "empresa_id", nullable = false)
    private EmpresaEntity empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instrumento_id", nullable = false)
    private InstrumentoEntity instrumento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "custodio_id", nullable = false)
    private CustodioEntity custodio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movimiento_id")
    private TipoMovimientoEntity tipoMovimiento;

    @PrePersist
    @PreUpdate
    public void calcularTotal() {
        if (this.cantidad != null && this.precio != null) {
            this.montoTotal = this.cantidad.multiply(this.precio);
        } else if (this.montoTotal == null) {
            this.montoTotal = BigDecimal.ZERO;
        }
    }

    public String getClaveAgrupacion() {
        if (getEmpresa() == null || getCustodio() == null || getInstrumento() == null || getCuenta() == null) {
            return "invalid_key";
        }
        return this.getEmpresa().getId() + "|"
                + this.getCuenta() + "|"
                + this.getCustodio().getId() + "|"
                + this.getInstrumento().getId();
    }
}