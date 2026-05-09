package uniquindio.edu.co.inmobiliaria.models.entities;
import lombok.*;

/**
 * Ciudad dentro de un departamento; usada para ubicar barrios e inmuebles.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor

public class Ciudad {
    /** Nombre de la ciudad. */
    private String nombre;
    /** Departamento o estado al que pertenece la ciudad. */
    private String departamento;
}
