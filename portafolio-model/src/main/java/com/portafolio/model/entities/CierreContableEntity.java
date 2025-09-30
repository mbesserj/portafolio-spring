
package com.portafolio.model.entities;

import com.portafolio.model.utiles.BaseEntity;
import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cierres_contables",
    uniqueConstraints = @UniqueConstraint(columnNames = {"ejercicio", "empresa_id", "custodio_id", "instrumento_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class CierreContableEntity extends BaseEntity implements Serializable {

    @Column(name = "ejercicio", nullable = false)
    private int ejercicio;

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
}
