package uniquindio.edu.co.inmobiliaria.models.entities;
import uniquindio.edu.co.inmobiliaria.models.enums.Zona;
import lombok.*;

/**
 * Barrio o sector urbano asociado a una {@link Ciudad} y a una {@link Zona} macro.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor

public class Barrio {
    
    /** Zona cardinal o agrupación geográfica (norte, sur, etc.). */
    private Zona zona;
    /** Nombre del barrio. */
    private String nombre;
    /** Ciudad donde se ubica el barrio. */
    private Ciudad ciudad;

}
