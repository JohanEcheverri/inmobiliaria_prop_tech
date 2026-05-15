package uniquindio.edu.co.inmobiliaria.models.entities;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
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
@Entity
@Inheritance(strategy = InheritanceType.JOINED)

public class Operacion {

    /** Código único de la operación. */
    @Id
    private String codigo;
    /** Inmueble objeto del negocio. */
    @ManyToOne(fetch = FetchType.LAZY)
    private Inmueble inmueble;
    /** Cliente parte de la operación. */
    @ManyToOne(fetch = FetchType.LAZY)
    private Cliente cliente;
    /** Asesor que interviene. */
    @ManyToOne(fetch = FetchType.LAZY)
    private Asesor asesor;
    /** Fecha y hora de registro o del hito principal. */
    private LocalDateTime fecha;
    /** Estado del trámite. */
    @Enumerated(EnumType.STRING)
    private EstadoOperacion estado;
    /** Comisión asociada. */
    private double comision;
    /** Valor pactado de la operación. */
    private double valorAcordado;

    @OneToOne(mappedBy = "operacion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Contrato contrato;

}
