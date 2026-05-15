package uniquindio.edu.co.inmobiliaria.models.entities;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
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
@Entity

public class Visita {

    /** Identificador de la visita. */
    @Id
    private String codigo;
    /** Cliente que asiste o solicitó la visita. */
    @ManyToOne(fetch = FetchType.LAZY)
    private Cliente cliente;
    /** Inmueble a conocer. */
    @ManyToOne(fetch = FetchType.LAZY)
    private Inmueble inmueble;
    /** Día programado. */
    private LocalDate fecha;
    /** Hora programada. */
    private LocalTime hora;
    /** Estado del ciclo de vida de la visita. */
    @Enumerated(EnumType.STRING)
    private EstadoVisita estado;
    /** Asesor asignado a acompañar o coordinar la visita. */
    @ManyToOne(fetch = FetchType.LAZY)
    private Asesor asesotAsignado;
    /** Notas u observaciones. */
    private String observaciones;
    
}
