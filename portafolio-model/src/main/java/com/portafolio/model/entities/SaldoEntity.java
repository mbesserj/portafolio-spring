package com.portafolio.model.entities;

import com.portafolio.model.dto.CargaTransaccionDto;
import com.portafolio.model.utiles.BaseEntity;
import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "saldos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class SaldoEntity extends BaseEntity implements Serializable {

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "cuenta", length = 100, nullable = false)
    private String cuenta;

    @Column(name = "cuenta_psh", length = 100)
    private String cuentaPsh;

    @Column(name = "cant_libre", precision = 19, scale = 4)
    private BigDecimal cantLibre;

    @Column(name = "cant_garantia", precision = 19, scale = 4)
    private BigDecimal cantGarantia;

    @Column(name = "cant_plazo", precision = 19, scale = 4)
    private BigDecimal cantPlazo;

    @Column(name = "cant_vc", precision = 19, scale = 4)
    private BigDecimal cantVc;

    @Column(name = "cantidad", precision = 19, scale = 4)
    private BigDecimal cantidad;
    
    @Column(name = "precio", precision = 19, scale = 4)
    private BigDecimal precio;

    @Column(name = "monto_clp", precision = 19, scale = 4)
    private BigDecimal montoClp;

    @Column(name = "monto_usd", precision = 19, scale = 4)
    private BigDecimal montoUsd;

    @Column(name = "moneda", length = 10)
    private String moneda;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instrumento_id", nullable = false)
    private InstrumentoEntity instrumento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "custodio_id", nullable = false)
    private CustodioEntity custodio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private EmpresaEntity empresa;

}