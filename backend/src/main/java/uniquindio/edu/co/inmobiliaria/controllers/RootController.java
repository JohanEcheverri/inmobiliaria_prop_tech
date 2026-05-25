package uniquindio.edu.co.inmobiliaria.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;
import java.util.HashMap;

@RestController
public class RootController {

    @GetMapping("/")
    public Map<String, Object> home() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ok");
        response.put("message", "Backend activo. Usa /api/auth/login o /api/clientes para la API.");
        return response;
    }
}
