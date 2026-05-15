package uniquindio.edu.co.inmobiliaria.models.entities;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import uniquindio.edu.co.inmobiliaria.models.enums.Zona;
import lombok.*;

/**
 * Barrio o sector urbano asociado a una {@link Ciudad} y a una {@link Zona} macro.
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Embeddable

public class Barrio {
    
    /** Zona cardinal o agrupación geográfica (norte, sur, etc.). */
    @Enumerated(EnumType.STRING)
    private Zona zona;
    /** Nombre del barrio. */
    private String nombre;
    /** Ciudad donde se ubica el barrio. */
    @Embedded
    private Ciudad ciudad;

}
