package uniquindio.edu.co.inmobiliaria.controllers;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uniquindio.edu.co.inmobiliaria.models.dto.AuthRequest;
import uniquindio.edu.co.inmobiliaria.models.dto.AuthResponse;
import uniquindio.edu.co.inmobiliaria.services.AdministradorService;
import uniquindio.edu.co.inmobiliaria.services.AsesorService;
import uniquindio.edu.co.inmobiliaria.services.ClienteService;

@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://127.0.0.1:5173", "http://127.0.0.1:5174", "http://localhost:3000"})
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final ClienteService clienteService;
    private final AsesorService asesorService;
    private final AdministradorService administradorService;

    public AuthController(ClienteService clienteService, AsesorService asesorService,
            AdministradorService administradorService) {
        this.clienteService = clienteService;
        this.asesorService = asesorService;
        this.administradorService = administradorService;
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest request) {
        if (request == null || request.identificacion() == null || request.identificacion().isBlank()
                || request.password() == null || request.password().isBlank()) {
            throw new IllegalArgumentException("La identificación y la contraseña son obligatorias");
        }

        return clienteService.autenticar(request.identificacion(), request.password())
                .or(() -> asesorService.autenticar(request.identificacion(), request.password()))
                .or(() -> administradorService.autenticar(request.identificacion(), request.password()))
                .orElseThrow(() -> new IllegalArgumentException("Credenciales inválidas"));
    }
}
