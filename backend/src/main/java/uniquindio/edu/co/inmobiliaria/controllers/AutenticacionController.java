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
