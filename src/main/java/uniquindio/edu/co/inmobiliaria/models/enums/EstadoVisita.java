package uniquindio.edu.co.inmobiliaria.models.enums;

/**
 * Estado del flujo de una visita a inmueble.
 */
public enum EstadoVisita {

    /** Agendada pero sin confirmar. */
    PENDIENTE,
    /** Confirmada por las partes. */
    CONFIRMADA,
    /** Ya se realizó la visita. */
    REALIZADA,
    /** Anulada. */
    CANCELADA,
    /** Movida a otra fecha u hora. */
    REPROGRAMADA
}
