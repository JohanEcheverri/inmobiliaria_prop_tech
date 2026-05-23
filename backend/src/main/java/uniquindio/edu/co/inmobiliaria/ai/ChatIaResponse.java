package uniquindio.edu.co.inmobiliaria.ai;

import java.time.LocalDateTime;

public record ChatIaResponse(
        String respuesta,
        String modelo,
        LocalDateTime fechaRespuesta
) {
}
