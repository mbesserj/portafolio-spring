package com.portafolio.model.dto;

import com.portafolio.model.entities.CustodioEntity;
import java.time.LocalDate;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CustodioDto {

    private Long id;
    private String nombreCustodio;
    private LocalDate fechaCreado;

    public CustodioDto(Long id, String nombreCustodio, LocalDate fechaCreado) {
        this.id = id;
        this.nombreCustodio = nombreCustodio;
        this.fechaCreado = fechaCreado;
    }

    /**
     * Crea CustodioEntity a partir del DAO
     * @return CustodioEntity
     */
    public CustodioEntity toEntity() {
        CustodioEntity entity = new CustodioEntity();
        entity.setNombreCustodio(this.nombreCustodio != null ? this.nombreCustodio : "");
        return entity;
    }
}