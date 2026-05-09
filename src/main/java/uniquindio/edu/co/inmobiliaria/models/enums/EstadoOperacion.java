package uniquindio.edu.co.inmobiliaria.models.enums;

/**
 * Estado de avance de una operación comercial
 * ({@link uniquindio.edu.co.inmobiliaria.models.entities.Operacion} y subtipos).
 */
public enum EstadoOperacion {

    /** Operación finalizada con éxito. */
    COMPLETADA,
    /** Trámite en curso. */
    EN_PROCESO,
    /** Operación anulada. */
    CANCELADA
}   
