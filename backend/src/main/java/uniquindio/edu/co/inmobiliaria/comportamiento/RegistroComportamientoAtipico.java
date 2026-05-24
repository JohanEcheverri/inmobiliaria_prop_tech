package uniquindio.edu.co.inmobiliaria.comportamiento;

import jakarta.persistence.*;
import lombok.*;
import uniquindio.edu.co.inmobiliaria.models.enums.TipoAlerta;
import java.time.LocalDateTime;

/**
 * Registro persistente de eventos de comportamiento atípico detectados en la inmobiliaria.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class RegistroComportamientoAtipico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TipoAlerta tipoComportamiento;

    private String descripcion;
    private String referenciaId;
    private LocalDateTime fechaDeteccion;

    @Enumerated(EnumType.STRING)
    private NivelAtencion nivelAtencion;

    private boolean resuelto;

    @Column(length = 1000)
    private String observaciones;
}
