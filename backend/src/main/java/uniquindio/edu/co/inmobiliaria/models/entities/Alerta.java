package uniquindio.edu.co.inmobiliaria.models.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.*;
import uniquindio.edu.co.inmobiliaria.models.enums.PrioridadAlerta;
import uniquindio.edu.co.inmobiliaria.models.enums.TipoAlerta;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

/**
 * Notificacion interna generada por reglas de seguimiento comercial.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Alerta {

    /** Identificador interno de la alerta. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String codigo;
    /** Regla de negocio que origino la alerta. */
    @Enumerated(EnumType.STRING)
    private TipoAlerta tipo;
    /** Nivel de urgencia para ordenar la atencion. */
    @Enumerated(EnumType.STRING)
    private PrioridadAlerta prioridad;
    /** Resumen corto para mostrar en listados. */
    private String titulo;
    /** Detalle de la situacion detectada. */
    private String descripcion;
    /** Momento en que se genero la alerta. */
    private LocalDateTime fechaGeneracion;
    /** Fecha sugerida para revisar o resolver la alerta. */
    private LocalDateTime fechaLimiteAtencion;
    /** Indica si la alerta ya fue atendida. */
    private boolean atendida;
    /** Momento en que se marco como atendida. */
    private LocalDateTime fechaAtencion;
    /** Inmueble relacionado, cuando aplique. */
    @ManyToOne(fetch = FetchType.LAZY)
    private Inmueble inmueble;
    /** Cliente relacionado, cuando aplique. */
    @ManyToOne(fetch = FetchType.LAZY)
    private Cliente cliente;
    /** Contrato relacionado, cuando aplique. */
    @ManyToOne(fetch = FetchType.LAZY)
    private Contrato contrato;
    /** Visita relacionada, cuando aplique. */
    @ManyToOne(fetch = FetchType.LAZY)
    private Visita visita;
    /** Operacion relacionada, cuando aplique. */
    @ManyToOne(fetch = FetchType.LAZY)
    private Operacion operacion;
    /** Cantidad de dias usada por la regla que genero la alerta. */
    private int diasReferencia;
    /** Conteo usado por reglas de demanda o acumulacion. */
    private int cantidadReferencia;

    public static Alerta contratoProximoAVencer(Contrato contrato, int diasAntes) {
        Alerta alerta = base(TipoAlerta.CONTRATO_PROXIMO_A_VENCER, PrioridadAlerta.ALTA);
        alerta.setContrato(contrato);
        alerta.setOperacion(contrato != null ? contrato.getOperacion() : null);
        alerta.setInmueble(alerta.getOperacion() != null ? alerta.getOperacion().getInmueble() : null);
        alerta.setCliente(alerta.getOperacion() != null ? alerta.getOperacion().getCliente() : null);
        alerta.setDiasReferencia(diasAntes);
        alerta.setFechaLimiteAtencion(contrato != null ? contrato.getFechaVencimiento() : null);
        alerta.setTitulo("Contrato proximo a vencer");
        alerta.setDescripcion("El contrato " + codigoContrato(contrato) + " vence en " + diasAntes + " dias o menos.");
        return alerta;
    }

    public static Alerta inmuebleSinVisitas(Inmueble inmueble, LocalDateTime fechaUltimaVisita, int diasSinVisitas) {
        Alerta alerta = base(TipoAlerta.INMUEBLE_SIN_VISITAS, PrioridadAlerta.MEDIA);
        alerta.setInmueble(inmueble);
        alerta.setDiasReferencia(diasSinVisitas);
        alerta.setFechaLimiteAtencion(LocalDateTime.now().plusDays(2));
        alerta.setTitulo("Inmueble sin visitas recientes");
        alerta.setDescripcion("El inmueble " + codigoInmueble(inmueble) + " no registra visitas desde "
                + textoFecha(fechaUltimaVisita) + ".");
        return alerta;
    }

    public static Alerta propiedadAltaDemanda(Inmueble inmueble, int cantidadSolicitudes, int umbralDemanda) {
        Alerta alerta = base(TipoAlerta.PROPIEDAD_ALTA_DEMANDA, PrioridadAlerta.ALTA);
        alerta.setInmueble(inmueble);
        alerta.setCantidadReferencia(cantidadSolicitudes);
        alerta.setFechaLimiteAtencion(LocalDateTime.now().plusDays(1));
        alerta.setTitulo("Propiedad con alta demanda");
        alerta.setDescripcion("El inmueble " + codigoInmueble(inmueble) + " tiene " + cantidadSolicitudes
                + " solicitudes, superando el umbral de " + umbralDemanda + ".");
        return alerta;
    }

    public static Alerta visitaPendientePorConfirmar(Visita visita) {
        Alerta alerta = base(TipoAlerta.VISITA_PENDIENTE_POR_CONFIRMAR, PrioridadAlerta.MEDIA);
        alerta.setVisita(visita);
        alerta.setInmueble(visita != null ? visita.getInmueble() : null);
        alerta.setCliente(visita != null ? visita.getCliente() : null);
        alerta.setFechaLimiteAtencion(visita != null && visita.getFecha() != null
                ? LocalDateTime.of(visita.getFecha(), visita.getHora() != null ? visita.getHora() : LocalTime.MIN)
                : LocalDateTime.now().plusDays(1));
        alerta.setTitulo("Visita pendiente por confirmar");
        alerta.setDescripcion("La visita " + codigoVisita(visita) + " esta pendiente por confirmar.");
        return alerta;
    }

    public static Alerta inmuebleReservadoSinCierre(Inmueble inmueble, LocalDateTime fechaReserva, int diasReservado) {
        Alerta alerta = base(TipoAlerta.INMUEBLE_RESERVADO_SIN_CIERRE, PrioridadAlerta.ALTA);
        alerta.setInmueble(inmueble);
        alerta.setDiasReferencia(diasReservado);
        alerta.setFechaLimiteAtencion(LocalDateTime.now().plusDays(1));
        alerta.setTitulo("Inmueble reservado sin cierre");
        alerta.setDescripcion("El inmueble " + codigoInmueble(inmueble) + " lleva "
                + diasTranscurridos(fechaReserva) + " dias reservado sin cierre.");
        return alerta;
    }

    public static Alerta clienteSinSeguimiento(Cliente cliente, LocalDateTime fechaUltimoSeguimiento, int diasSinSeguimiento) {
        Alerta alerta = base(TipoAlerta.CLIENTE_SIN_SEGUIMIENTO_RECIENTE, PrioridadAlerta.MEDIA);
        alerta.setCliente(cliente);
        alerta.setDiasReferencia(diasSinSeguimiento);
        alerta.setFechaLimiteAtencion(LocalDateTime.now().plusDays(2));
        alerta.setTitulo("Cliente sin seguimiento reciente");
        alerta.setDescripcion("El cliente " + nombreCliente(cliente) + " no tiene seguimiento desde "
                + textoFecha(fechaUltimoSeguimiento) + ".");
        return alerta;
    }

    public void marcarAtendida() {
        this.atendida = true;
        this.fechaAtencion = LocalDateTime.now();
    }

    private static Alerta base(TipoAlerta tipo, PrioridadAlerta prioridad) {
        Alerta alerta = new Alerta();
        alerta.setTipo(tipo);
        alerta.setPrioridad(prioridad);
        alerta.setFechaGeneracion(LocalDateTime.now());
        alerta.setAtendida(false);
        return alerta;
    }

    private static String codigoContrato(Contrato contrato) {
        return contrato != null && contrato.getCodigo() != null ? contrato.getCodigo() : "sin codigo";
    }

    private static String codigoInmueble(Inmueble inmueble) {
        return inmueble != null && inmueble.getCodigo() != null ? inmueble.getCodigo() : "sin codigo";
    }

    private static String codigoVisita(Visita visita) {
        return visita != null && visita.getCodigo() != null ? visita.getCodigo() : "sin codigo";
    }

    private static String nombreCliente(Cliente cliente) {
        if (cliente == null) {
            return "sin identificar";
        }
        String nombre = cliente.getNombre();
        return nombre != null && !nombre.isBlank() ? nombre : "sin nombre";
    }

    private static String textoFecha(LocalDateTime fecha) {
        return fecha != null ? fecha.toString() : "una fecha no registrada";
    }

    private static long diasTranscurridos(LocalDateTime fechaInicio) {
        return fechaInicio != null ? ChronoUnit.DAYS.between(fechaInicio, LocalDateTime.now()) : 0;
    }
}
