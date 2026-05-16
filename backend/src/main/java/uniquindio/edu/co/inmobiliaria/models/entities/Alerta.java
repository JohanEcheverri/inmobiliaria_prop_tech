package uniquindio.edu.co.inmobiliaria.models.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.*;
import uniquindio.edu.co.inmobiliaria.models.enums.PrioridadAlerta;
import uniquindio.edu.co.inmobiliaria.models.enums.TipoAlerta;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

/**
 * Notificacion interna generada por reglas de seguimiento comercial.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Alerta {

    /** Identificador interno de la alerta. */
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
    /** Momento en que se genero la alerta. */
    private LocalDateTime fechaGeneracion;
    /** Fecha sugerida para revisar o resolver la alerta. */
    private LocalDateTime fechaLimiteAtencion;
    /** Indica si la alerta ya fue atendida. */
    private boolean atendida;
    /** Momento en que se marco como atendida. */
    private LocalDateTime fechaAtencion;
    /** Inmueble relacionado, cuando aplique. */
    @ManyToOne(fetch = FetchType.LAZY)
    private Inmueble inmueble;
    /** Cliente relacionado, cuando aplique. */
    @ManyToOne(fetch = FetchType.LAZY)
    private Cliente cliente;
    /** Contrato relacionado, cuando aplique. */
    @ManyToOne(fetch = FetchType.LAZY)
    private Contrato contrato;
    /** Visita relacionada, cuando aplique. */
    @ManyToOne(fetch = FetchType.LAZY)
    private Visita visita;
    /** Operacion relacionada, cuando aplique. */
    @ManyToOne(fetch = FetchType.LAZY)
    private Operacion operacion;
    /** Cantidad de dias usada por la regla que genero la alerta. */
    private int diasReferencia;
    /** Conteo usado por reglas de demanda o acumulacion. */
    private int cantidadReferencia;

}
