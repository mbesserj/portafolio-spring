package com.portafolio.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmpresaDto {
    
    private Long id;
    
    @NotBlank(message = "El RUT es obligatorio")
    @Size(max = 14, message = "El RUT no puede tener más de 14 caracteres")
    private String rut;
    
    @NotBlank(message = "La Razón Social es obligatoria")
    @Size(max = 255, message = "La Razón Social no puede tener más de 255 caracteres")
    private String razonSocial;
    
    private LocalDate fechaCreado;
    
    private Long grupoEmpresaId;
    
    private String grupoEmpresaNombre;
    
    private List<Long> custodiosIds;
}