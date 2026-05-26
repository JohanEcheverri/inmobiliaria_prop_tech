package uniquindio.edu.co.inmobiliaria.controllers;

import java.util.Map;
import java.util.HashMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import uniquindio.edu.co.inmobiliaria.models.entities.Usuario;

@RequestMapping("/api") // Opcional, para agrupar las rutas de la API
@RestController
public class PruebaRestController {

    /**
     * Endpoint de prueba que devuelve un map con un objeto Usuario para verificar serialización.
     * Usado únicamente en desarrollo.
     *
     * @param model (no usado para respuesta JSON, incluido por compatibilidad)
     * @return mapa con la clave 'mensaje' apuntando a un Usuario de ejemplo
     */
    @GetMapping("/prueba")
    public Map<String, Object> prueba1(Model model){
        Usuario usuario = new Usuario("Johan", "Johan", "123456789", "password123", "hola" ,"foto.jpg");        
        Map<String, Object> body = new HashMap<>();
        body.put("mensaje", usuario);
        return body;
    }

}
