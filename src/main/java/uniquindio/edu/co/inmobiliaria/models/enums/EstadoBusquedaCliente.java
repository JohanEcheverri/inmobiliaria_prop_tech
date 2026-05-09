package uniquindio.edu.co.inmobiliaria.models.enums;

/**
 * Etapa del proceso de búsqueda o negociación del cliente.
 */
public enum EstadoBusquedaCliente {

        /** Aún busca opciones. */
        BUSCANDO,
        /** En conversación u oferta sobre una o más propiedades. */
        NEGOCIANDO,
        /** Proceso concluido (éxito o abandono según reglas de negocio). */
        CERRADO
}
