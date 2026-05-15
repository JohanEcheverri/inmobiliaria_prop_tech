package uniquindio.edu.co.inmobiliaria.models.dto;

import java.time.LocalDateTime;

public record ApiError(
        String mensaje,
        LocalDateTime fecha
) {
}
