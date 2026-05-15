package uniquindio.edu.co.inmobiliaria.models.enums;

/**
 * Tipo de interacción registrada en el historial de un inmueble.
 */
public enum TipoEventoHistorial {
    /** El usuario consultó la ficha o detalle. */
    CONSULTA,
    /** Se agendó o completó una visita. */
    VISITA,
    /** Descartó el inmueble de su lista. */
    DESCARTADO,
    /** Guardó o marcó favorito. */
    GUARDADO,
    /** Inició o mantiene negociación. */
    NEGOCIANDO,
}
