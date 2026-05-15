package uniquindio.edu.co.inmobiliaria.models.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.*;
import uniquindio.edu.co.inmobiliaria.models.enums.TipoSesion;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Representa una sesión de acceso de un cliente o asesor (metadatos de login y actividad).
 * Complementa la sesión HTTP o tokens de la aplicación según cómo se integre en capas superiores.
 */
@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Sesion {

    /** Identificador único de la sesión (por ejemplo UUID). */
    @Id
    private String idSesion;
    /** Indica si la sesión corresponde a un cliente o a un asesor. */
    @Enumerated(EnumType.STRING)
    private TipoSesion tipoSesion;
    /** Usuario autenticado; en la práctica será una instancia de {@link Cliente} o {@link Asesor}. */
    @ManyToOne(fetch = FetchType.LAZY)
    private Usuario usuario;
    /** Momento en que se inició la sesión. */
    private LocalDateTime fechaInicio;
    /** Momento del último uso registrado de la sesión. */
    private LocalDateTime fechaUltimaActividad;
    /** Momento límite de validez; {@code null} implica que no se aplica expiración por fecha. */
    private LocalDateTime fechaExpiracion;
    /** Dirección IP desde la que se abrió o usó la sesión (auditoría o seguridad). */
    private String direccionIp;
    /** Encabezado User-Agent del cliente HTTP, si aplica. */
    private String userAgent;

    /**
     * Crea una sesión nueva para un cliente, generando {@code idSesion} y sellos de tiempo iniciales.
     *
     * @param cliente usuario cliente (no nulo)
     * @param fechaExpiracion fecha y hora de caducidad, o {@code null} si no expira por tiempo
     * @param direccionIp IP del cliente, puede ser {@code null}
     * @param userAgent agente del navegador, puede ser {@code null}
     * @return sesión construida
     */
    public static Sesion crearParaCliente(Cliente cliente, LocalDateTime fechaExpiracion,
            String direccionIp, String userAgent) {
        LocalDateTime ahora = LocalDateTime.now();
        return Sesion.builder()
                .idSesion(UUID.randomUUID().toString())
                .tipoSesion(TipoSesion.CLIENTE)
                .usuario(Objects.requireNonNull(cliente, "cliente"))
                .fechaInicio(ahora)
                .fechaUltimaActividad(ahora)
                .fechaExpiracion(fechaExpiracion)
                .direccionIp(direccionIp)
                .userAgent(userAgent)
                .build();
    }

    /**
     * Crea una sesión nueva para un asesor, generando {@code idSesion} y sellos de tiempo iniciales.
     *
     * @param asesor usuario asesor (no nulo)
     * @param fechaExpiracion fecha y hora de caducidad, o {@code null} si no expira por tiempo
     * @param direccionIp IP del cliente, puede ser {@code null}
     * @param userAgent agente del navegador, puede ser {@code null}
     * @return sesión construida
     */
    public static Sesion crearParaAsesor(Asesor asesor, LocalDateTime fechaExpiracion,
            String direccionIp, String userAgent) {
        LocalDateTime ahora = LocalDateTime.now();
        return Sesion.builder()
                .idSesion(UUID.randomUUID().toString())
                .tipoSesion(TipoSesion.ASESOR)
                .usuario(Objects.requireNonNull(asesor, "asesor"))
                .fechaInicio(ahora)
                .fechaUltimaActividad(ahora)
                .fechaExpiracion(fechaExpiracion)
                .direccionIp(direccionIp)
                .userAgent(userAgent)
                .build();
    }

    /** Actualiza {@link #fechaUltimaActividad} al instante actual. */
    public void registrarActividad() {
        this.fechaUltimaActividad = LocalDateTime.now();
    }

    /**
     * @return {@code true} si existe {@link #fechaExpiracion} y ya pasó; {@code false} si no hay expiración o aún es válida
     */
    public boolean estaExpirada() {
        if (fechaExpiracion == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(fechaExpiracion);
    }

    /** @return {@code true} si {@link #tipoSesion} es {@link TipoSesion#CLIENTE} */
    public boolean esCliente() {
        return tipoSesion == TipoSesion.CLIENTE;
    }

    /** @return {@code true} si {@link #tipoSesion} es {@link TipoSesion#ASESOR} */
    public boolean esAsesor() {
        return tipoSesion == TipoSesion.ASESOR;
    }

    /**
     * @return el {@link #usuario} como {@link Cliente}, o {@code null} si no lo es
     */
    public Cliente getCliente() {
        return usuario instanceof Cliente c ? c : null;
    }

    /**
     * @return el {@link #usuario} como {@link Asesor}, o {@code null} si no lo es
     */
    public Asesor getAsesor() {
        return usuario instanceof Asesor a ? a : null;
    }
}
