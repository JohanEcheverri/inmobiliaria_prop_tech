package uniquindio.edu.co.inmobiliaria.models.entities;
import uniquindio.edu.co.inmobiliaria.models.enums.EstadoVisita;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Cita para que un {@link Cliente} visite un {@link Inmueble}, con estado del flujo y asesor asignado.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor

public class Visita {

    /** Identificador de la visita. */
    private String codigo;
    /** Cliente que asiste o solicitó la visita. */
    private Cliente cliente;
    /** Inmueble a conocer. */
    private Inmueble inmueble;
    /** Día programado. */
    private LocalDate fecha;
    /** Hora programada. */
    private LocalTime hora;
    /** Estado del ciclo de vida de la visita. */
    private EstadoVisita estado;
    /** Asesor asignado a acompañar o coordinar la visita. */
    private Asesor asesotAsignado;
    /** Notas u observaciones. */
    private String observaciones;
    
}
