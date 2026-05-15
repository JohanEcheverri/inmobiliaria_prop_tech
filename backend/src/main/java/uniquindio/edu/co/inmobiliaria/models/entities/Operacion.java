package uniquindio.edu.co.inmobiliaria.models.entities;
import uniquindio.edu.co.inmobiliaria.models.enums.EstadoOperacion;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.time.LocalDateTime;

/**
 * Operación comercial genérica que vincula inmueble, cliente y asesor.
 * Las subclases especializan venta, arriendo, renovación o cancelación.
 */
@SuperBuilder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor

public class Operacion {

    /** Código único de la operación. */
    private String codigo;
    /** Inmueble objeto del negocio. */
    private Inmueble inmueble;
    /** Cliente parte de la operación. */
    private Cliente cliente;
    /** Asesor que interviene. */
    private Asesor asesor;
    /** Fecha y hora de registro o del hito principal. */
    private LocalDateTime fecha;
    /** Estado del trámite. */
    private EstadoOperacion estado;
    /** Comisión asociada. */
    private double comision;
    /** Valor pactado de la operación. */
    private double valorAcordado;

    private Contrato contrato;

}
