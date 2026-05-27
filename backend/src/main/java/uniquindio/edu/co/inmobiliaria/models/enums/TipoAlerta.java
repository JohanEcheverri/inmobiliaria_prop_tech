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
    CLIENTE_SIN_SEGUIMIENTO_RECIENTE,
    /** Inmuebles con un número anormalmente alto de visitas sin cierre. */
    INMUEBLE_EXCESO_VISITAS_SIN_CIERRE,
    /** Clientes que agendan múltiples visitas en corto tiempo sin continuidad. */
    CLIENTE_MULTIPLES_VISITAS_SIN_CONTINUIDAD,
    /** Asesores con sobrecarga excesiva de atención. */
    ASESOR_SOBRECARGA_ATENCION,
    /** Propiedades cuyo precio cambia con demasiada frecuencia. */
    PROPIEDAD_PRECIO_CAMBIO_FRECUENTE,
    /** Concentración de interés en una misma zona en un tiempo reducido. */
    ZONA_CONCENTRACION_INTERES,
    /** Cliente solicita cancelar un contrato de arriendo vigente. */
    SOLICITUD_CANCELACION_CONTRATO
}
