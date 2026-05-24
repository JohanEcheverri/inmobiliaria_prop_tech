package uniquindio.edu.co.inmobiliaria.comportamiento;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entidad para registrar de manera persistente los cambios de precio de los inmuebles.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class HistorialPrecio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String codigoInmueble;
    private double precioAnterior;
    private double precioNuevo;
    private LocalDateTime fechaCambio;
}
