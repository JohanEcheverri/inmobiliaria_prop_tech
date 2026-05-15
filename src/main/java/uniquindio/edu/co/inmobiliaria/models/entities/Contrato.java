package uniquindio.edu.co.inmobiliaria.models.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.*;
import uniquindio.edu.co.inmobiliaria.models.enums.TipoContrato;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Contrato {
    @Id
    private String codigo;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operacion_codigo")
    private Operacion operacion;
    private String contenido;
    private LocalDateTime fechaIncio;
    private LocalDateTime fechaVencimiento;
    private boolean vigente;

    @Enumerated(EnumType.STRING)
    private TipoContrato tipoContrato;

}
