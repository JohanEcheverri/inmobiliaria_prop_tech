package uniquindio.edu.co.inmobiliaria.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uniquindio.edu.co.inmobiliaria.models.LoginRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        // Obtenemos los datos del DTO adaptado
        String id = request.getIdentificacion();
        String pass = request.getPassword();

        // Simulación de validación basada en Identificación y Roles
        // IMPORTANTE: Estos datos son quemados para la prueba inicial

        if ("1094123".equals(id) && "admin123".equals(pass)) {
            return ResponseEntity.ok(Map.of(
                    "mensaje", "Acceso concedido: Administrador del Sistema",
                    "rol", "ADMIN",
                    "nombre", "Andrés Administrador"
            ));
        }

        else if ("2026456".equals(id) && "asesor123".equals(pass)) {
            return ResponseEntity.ok(Map.of(
                    "mensaje", "Acceso concedido: Panel de Asesor Inmobiliario",
                    "rol", "ASESOR",
                    "nombre", "Carlos Asesor"
            ));
        }

        else if ("3030789".equals(id) && "cliente123".equals(pass)) {
            return ResponseEntity.ok(Map.of(
                    "mensaje", "Acceso concedido: Catálogo de Clientes",
                    "rol", "CLIENTE",
                    "nombre", "Juan Cliente"
            ));
        }

        // Si no coincide ninguno
        else {
            return ResponseEntity.status(401).body(Map.of(
                    "error", "Identificación o contraseña incorrectas"
            ));
        }
    }
}