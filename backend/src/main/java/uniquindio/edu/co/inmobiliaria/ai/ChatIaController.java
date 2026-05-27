package uniquindio.edu.co.inmobiliaria.ai;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uniquindio.edu.co.inmobiliaria.models.dto.ApiError;

import java.time.LocalDateTime;

@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://127.0.0.1:5173", "http://127.0.0.1:5174", "http://localhost:3000"})
@RestController
@RequestMapping("/api/ia/chat")
/**
 * Controlador REST que sirve como puente de integración con el modelo de lenguaje de IA (Gemini).
 * Permite a los usuarios finales realizar consultas en lenguaje natural sobre inmuebles y obtener
 * respuestas inteligentes contextualizadas con la base de datos de la inmobiliaria.
 */
public class ChatIaController {

    private final GeminiChatService geminiChatService;

    public ChatIaController(GeminiChatService geminiChatService) {
        this.geminiChatService = geminiChatService;
    }

    /**
     * Procesa un mensaje de usuario enviado desde el frontend y obtiene una respuesta
     * generada por el servicio de Inteligencia Artificial.
     *
     * @param request objeto con el mensaje del usuario y (opcionalmente) su identificador
     * @return ChatIaResponse con el texto de respuesta generado por la IA
     */
    @PostMapping
    public ChatIaResponse conversar(@RequestBody ChatIaRequest request) {
        return geminiChatService.responder(request);
    }

    /**
     * Interceptor local para manejar errores de conexión o procesamiento con la API de IA.
     *
     * @param exception error específico del servicio Gemini
     * @return respuesta HTTP 502 Bad Gateway con los detalles del error estructurados
     */
    @ExceptionHandler(GeminiApiException.class)
    public ResponseEntity<ApiError> manejarGeminiApiException(GeminiApiException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .body(new ApiError(exception.getMessage(), LocalDateTime.now()));
    }
}
