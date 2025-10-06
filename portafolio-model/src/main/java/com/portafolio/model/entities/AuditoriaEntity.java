package com.portafolio.model.entities;

import com.portafolio.model.utiles.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "auditorias")
@Getter
@Setter
@NoArgsConstructor 
@AllArgsConstructor
@SuperBuilder  
@EqualsAndHashCode(callSuper = true) 
public class AuditoriaEntity extends BaseEntity implements Serializable {

    @Column(name = "fecha_auditoria", nullable = false)
    private LocalDate fechaAuditoria;

    @Column(name = "fecha_archivo")
    private LocalDate fechaArchivo;

    @Column(name = "fecha_datos")
    private LocalDate fechaDatos;

    @Column(name = "tipo_entidad", length = 100)
    private String tipoEntidad;

    @Column(name = "valor_clave", length = 255)
    private String valorClave;

    @Column(name = "descripcion", length = 1000)
    private String descripcion;

    @Column(name = "archivo_origen", nullable = false, length = 500)
    private String archivoOrigen;

    @Column(name = "fila_numero", nullable = false)
    private Integer filaNumero;

    @Column(name = "motivo", length = 1000, nullable = false)
    private String motivo;

    @Column(name = "registros_insertados")
    private Integer registrosInsertados;

    @Column(name = "registros_rechazados")
    private Integer registrosRechazados;

    @Column(name = "registros_duplicados")
    private Integer registrosDuplicados;

    @PrePersist
    @Override
    protected void onCreate() {
        super.onCreate(); 
        if (this.fechaAuditoria == null) {
            this.fechaAuditoria = LocalDate.now();
        }
    }
}