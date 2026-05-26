package uniquindio.edu.co.inmobiliaria.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uniquindio.edu.co.inmobiliaria.models.dto.LoginRequest;
import uniquindio.edu.co.inmobiliaria.models.dto.SesionDTO;
import uniquindio.edu.co.inmobiliaria.services.AutenticacionService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Para que tu frontend pueda conectar sin líos de CORS
public class AutenticacionController {

    private final AutenticacionService autenticacionService;

    /**
     * Endpoint de autenticación que delega al servicio y devuelve los datos de sesión.
     *
     * @param request DTO con credenciales de login
     * @return ResponseEntity con SesionDTO en caso de éxito o mensaje de error en fallo
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            SesionDTO sesion = autenticacionService.login(request);
            return ResponseEntity.ok(sesion);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
