package uniquindio.edu.co.inmobiliaria.models.entities;

import lombok.*;
import uniquindio.edu.co.inmobiliaria.models.enums.TipoContrato;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Contrato {
    private String codigo;
    private Operacion operacion;
    private String contenido;
    private LocalDateTime fechaIncio;
    private LocalDateTime fechaVencimiento;
    private boolean vigente;
    private TipoContrato tipoContrato;

}
