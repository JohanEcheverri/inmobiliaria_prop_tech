package uniquindio.edu.co.inmobiliaria.models.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;
import uniquindio.edu.co.inmobiliaria.models.enums.PrioridadAlerta;
import uniquindio.edu.co.inmobiliaria.models.enums.TipoAlerta;

import java.time.LocalDateTime;

/**
 * Notificacion interna generada por reglas de seguimiento comercial.
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Alerta {

    /** Identificador interno de la alerta. */
    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String codigo;

    /** Regla de negocio que origino la alerta. */
    @Enumerated(EnumType.STRING)
    private TipoAlerta tipo;

    /** Nivel de urgencia para ordenar la atencion. */
    @Enumerated(EnumType.STRING)
    private PrioridadAlerta prioridad;

    /** Resumen corto para mostrar en listados. */
    private String titulo;

    /** Detalle de la situacion detectada. */
    private String descripcion;

    /** Identificador de la entidad relacionada (contrato, inmueble, visita, cliente). */
    private String referenciaId;

    /** Momento en que se genero la alerta. */
    private LocalDateTime fechaGeneracion;

    /** Indica si la alerta ya fue atendida. */
    @Column(nullable = false)
    private boolean atendida;

    /** Momento en que se marco como atendida. */
    private LocalDateTime fechaAtencion;


}
