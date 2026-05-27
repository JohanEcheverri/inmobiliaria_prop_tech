package uniquindio.edu.co.inmobiliaria.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import uniquindio.edu.co.inmobiliaria.models.dto.ClienteResponse;
import uniquindio.edu.co.inmobiliaria.services.ClienteService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
/**
 * Servicio de integración con la API de Google Gemini para habilitar el asistente conversacional.
 * Construye dinámicamente el contexto del usuario (preferencias, presupuesto, etc.) para
 * personalizar las respuestas de la IA según los datos reales de la plataforma inmobiliaria.
 */
public class GeminiChatService {

    private static final String SYSTEM_PROMPT = """
            Eres el asistente IA de una plataforma inmobiliaria.
            Ayudas a clientes a entender inmuebles, presupuestos, zonas, visitas y recomendaciones.
            Responde en espanol claro, breve y orientado a acciones.
            No inventes datos internos del sistema. Si falta informacion, pide el dato puntual.
            No des asesoria legal o financiera definitiva; sugiere validar con un asesor humano cuando aplique.
            """;

    private final RestClient restClient;
    private final ClienteService clienteService;

    private final String apiKey;

    private final String model;
    private final double temperature;
    private final int maxOutputTokens;

    public GeminiChatService(
            ClienteService clienteService,
            @Value("${gemini.api-key:}") String apiKey,
            @Value("${gemini.model:gemini-2.5-flash}") String model,
            @Value("${gemini.temperature:0.4}") double temperature,
            @Value("${gemini.max-output-tokens:700}") int maxOutputTokens
    ) {
        this.clienteService = clienteService;
        this.apiKey = apiKey;
        this.model = model;
        this.temperature = temperature;
        this.maxOutputTokens = maxOutputTokens;
        this.restClient = RestClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com")
                .build();
    }

    public ChatIaResponse responder(ChatIaRequest request) {
        validarRequest(request);
        validarConfiguracion();

        String contextoCliente = construirContextoCliente(request.clienteId());
        Map<String, Object> payload = crearPayload(contextoCliente, request.mensaje().trim());

        GeminiClientResponse response = restClient.post()
                .uri("/v1beta/models/{model}:generateContent", model)
                .header("x-goog-api-key", apiKey)
                .body(payload)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    throw new GeminiApiException("Gemini no pudo procesar la solicitud. Estado HTTP: " + res.getStatusCode());
                })
                .body(GeminiClientResponse.class);

        String texto = extraerTexto(response);
        return new ChatIaResponse(texto, model, LocalDateTime.now());
    }

    private void validarRequest(ChatIaRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("La solicitud del chat no puede ser nula");
        }
        if (request.mensaje() == null || request.mensaje().isBlank()) {
            throw new IllegalArgumentException("El mensaje para la IA es obligatorio");
        }
        if (request.mensaje().length() > 2000) {
            throw new IllegalArgumentException("El mensaje no puede superar los 2000 caracteres");
        }
    }

    private void validarConfiguracion() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new GeminiApiException("La API key de Gemini no esta configurada");
        }
    }

    private String construirContextoCliente(String clienteId) {
        if (clienteId == null || clienteId.isBlank()) {
            return "Cliente no identificado en la solicitud.";
        }

        try {
            ClienteResponse cliente = clienteService.obtenerCliente(clienteId);
            return """
                    Cliente autenticado:
                    - Nombre: %s
                    - Tipo de cliente: %s
                    - Zona de interes: %s
                    - Presupuesto: %s
                    - Tipo de inmueble deseado: %s
                    - Habitaciones deseadas: %s
                    - Estado de busqueda: %s
                    """.formatted(
                    valor(cliente.nombre()),
                    valor(cliente.tipoCliente()),
                    valor(cliente.zonaInteres()),
                    valor(cliente.presupuesto()),
                    valor(cliente.tipoInmuebleDeseado()),
                    cliente.numeroHabitacionesDeseadas(),
                    valor(cliente.estadoBusqueda())
            );
        } catch (IllegalArgumentException exception) {
            return "No se encontro informacion del cliente con id " + clienteId + ".";
        }
    }

    private Map<String, Object> crearPayload(String contextoCliente, String mensaje) {
        String mensajeConContexto = contextoCliente + System.lineSeparator()
                + "Pregunta del cliente: " + mensaje;

        return Map.of(
                "systemInstruction", Map.of(
                        "parts", List.of(Map.of("text", SYSTEM_PROMPT))
                ),
                "contents", List.of(Map.of(
                        "role", "user",
                        "parts", List.of(Map.of("text", mensajeConContexto))
                )),
                "generationConfig", Map.of(
                        "temperature", temperature,
                        "maxOutputTokens", maxOutputTokens
                )
        );
    }

    private String extraerTexto(GeminiClientResponse response) {
        if (response == null || response.candidates() == null || response.candidates().isEmpty()) {
            throw new GeminiApiException("Gemini no retorno una respuesta util");
        }

        GeminiClientResponse.Content content = response.candidates().getFirst().content();
        if (content == null || content.parts() == null || content.parts().isEmpty()) {
            throw new GeminiApiException("Gemini retorno una respuesta sin contenido");
        }

        StringBuilder builder = new StringBuilder();
        for (GeminiClientResponse.Part part : content.parts()) {
            if (part != null && part.text() != null) {
                builder.append(part.text());
            }
        }

        String texto = builder.toString().trim();
        if (texto.isBlank()) {
            throw new GeminiApiException("Gemini retorno una respuesta vacia");
        }
        return texto;
    }

    private String valor(Object value) {
        return value == null ? "No especificado" : value.toString();
    }
}
