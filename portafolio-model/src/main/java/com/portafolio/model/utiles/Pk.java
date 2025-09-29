
package com.portafolio.model.utiles;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class Pk implements Serializable {

    private LocalDate transactionDate;
    private Integer rowNum;
    private String tipoClase;
}