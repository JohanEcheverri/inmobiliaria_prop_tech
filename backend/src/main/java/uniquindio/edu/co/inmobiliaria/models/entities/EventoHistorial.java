package uniquindio.edu.co.inmobiliaria.models.entities;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
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
@Entity

public class EventoHistorial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Inmueble al que refiere el evento. */
    @ManyToOne(fetch = FetchType.EAGER)
    private Inmueble inmueble;
    /** Clasificación del evento. */
    @Enumerated(EnumType.STRING)
    private TipoEventoHistorial tipoEvento;
    /** Momento en que ocurrió el evento. */
    private LocalDateTime fechaEvento;

    @ManyToOne(fetch = FetchType.EAGER)
    private Cliente cliente;

    /**
     * Construye un evento con {@link #fechaEvento} en el instante actual.
     *
     * @param inmueble inmueble relacionado
     * @param tipo tipo de evento
     */
    public EventoHistorial(Inmueble inmueble, TipoEventoHistorial tipo, Cliente cliente) {
        this.inmueble = inmueble;
        this.tipoEvento = tipo;
        this.fechaEvento = LocalDateTime.now();
        this.cliente = cliente;
    }

}
