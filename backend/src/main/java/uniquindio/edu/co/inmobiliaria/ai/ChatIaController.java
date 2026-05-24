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
public class ChatIaController {

    private final GeminiChatService geminiChatService;

    public ChatIaController(GeminiChatService geminiChatService) {
        this.geminiChatService = geminiChatService;
    }

    @PostMapping
    public ChatIaResponse conversar(@RequestBody ChatIaRequest request) {
        return geminiChatService.responder(request);
    }

    @ExceptionHandler(GeminiApiException.class)
    public ResponseEntity<ApiError> manejarGeminiApiException(GeminiApiException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .body(new ApiError(exception.getMessage(), LocalDateTime.now()));
    }
}
