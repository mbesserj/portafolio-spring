package com.model.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CostingGroupDto {
    private String claveAgrupacion;
    private LocalDate fechaCreado;
    private String instrumentoNemonico;
    private String razonSocial;
    private String cuenta;
}
