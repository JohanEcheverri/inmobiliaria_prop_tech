package uniquindio.edu.co.inmobiliaria.models.enums;

/**
 * Reglas de negocio que pueden generar alertas internas.
 */
public enum TipoAlerta {

    /** Contratos cuya fecha de vencimiento esta cerca. */
    CONTRATO_PROXIMO_A_VENCER,
    /** Inmuebles que no han recibido visitas durante un periodo amplio. */
    INMUEBLE_SIN_VISITAS,
    /** Propiedades con solicitudes o interacciones por encima del umbral definido. */
    PROPIEDAD_ALTA_DEMANDA,
    /** Visitas agendadas que aun no han sido confirmadas. */
    VISITA_PENDIENTE_POR_CONFIRMAR,
    /** Inmuebles reservados demasiado tiempo sin cerrar la operacion. */
    INMUEBLE_RESERVADO_SIN_CIERRE,
    /** Clientes que no han recibido seguimiento comercial reciente. */
    CLIENTE_SIN_SEGUIMIENTO_RECIENTE
}
