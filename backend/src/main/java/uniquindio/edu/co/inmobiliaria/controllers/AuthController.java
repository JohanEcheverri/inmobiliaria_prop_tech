package uniquindio.edu.co.inmobiliaria.controllers;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uniquindio.edu.co.inmobiliaria.models.dto.AuthRequest;
import uniquindio.edu.co.inmobiliaria.models.dto.AuthResponse;
import uniquindio.edu.co.inmobiliaria.services.AsesorService;
import uniquindio.edu.co.inmobiliaria.services.ClienteService;

@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final ClienteService clienteService;
    private final AsesorService asesorService;

    public AuthController(ClienteService clienteService, AsesorService asesorService) {
        this.clienteService = clienteService;
        this.asesorService = asesorService;
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest request) {
        if (request == null || request.credencial() == null || request.credencial().isBlank()
                || request.password() == null || request.password().isBlank()) {
            throw new IllegalArgumentException("La identificación/correo y la contraseña son obligatorios");
        }

        return clienteService.autenticar(request.credencial(), request.password())
                .or(() -> asesorService.autenticar(request.credencial(), request.password()))
                .orElseThrow(() -> new IllegalArgumentException("Credenciales inválidas"));
    }
}
