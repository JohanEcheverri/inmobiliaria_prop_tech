package uniquindio.edu.co.inmobiliaria.ai;

/**
 * Excepción personalizada para envolver errores de comunicación o procesamiento
 * devueltos por la API de Google Gemini (Chat IA).
 */
public class GeminiApiException extends RuntimeException {

    public GeminiApiException(String message) {
        super(message);
    }
}
