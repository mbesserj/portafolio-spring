package com.model.utiles;

import java.io.Serializable;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.EqualsAndHashCode; 

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode 
public class Pk implements Serializable {

    private LocalDate fechaTransaccion;
    private Integer rowNum;
    private String tipoClase;
}