package uniquindio.edu.co.inmobiliaria.models.entities;
import uniquindio.edu.co.inmobiliaria.models.enums.TipoEventoHistorial;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entrada de historial sobre interacciones con un inmueble (consultas, visitas, etc.).
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor

public class EventoHistorial {

    /** Inmueble al que refiere el evento. */
    private Inmueble inmueble;
    /** Clasificación del evento. */
    private TipoEventoHistorial tipoEvento;
    /** Momento en que ocurrió el evento. */
    private LocalDateTime fechaEvento;

    /**
     * Construye un evento con {@link #fechaEvento} en el instante actual.
     *
     * @param inmueble inmueble relacionado
     * @param tipo tipo de evento
     */
    public EventoHistorial(Inmueble inmueble, TipoEventoHistorial tipo) {
        this.inmueble = inmueble;
        this.tipoEvento = tipo;
        this.fechaEvento = LocalDateTime.now();
    }

}
