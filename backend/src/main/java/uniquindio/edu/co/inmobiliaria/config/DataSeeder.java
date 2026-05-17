package uniquindio.edu.co.inmobiliaria.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uniquindio.edu.co.inmobiliaria.models.dto.ClienteRequest;
import uniquindio.edu.co.inmobiliaria.models.entities.Administrador;
import uniquindio.edu.co.inmobiliaria.models.entities.Asesor;
import uniquindio.edu.co.inmobiliaria.models.enums.EstadoBusquedaCliente;
import uniquindio.edu.co.inmobiliaria.models.enums.TipoCliente;
import uniquindio.edu.co.inmobiliaria.models.enums.TipoInmueble;
import uniquindio.edu.co.inmobiliaria.models.enums.Zona;
import uniquindio.edu.co.inmobiliaria.repositories.AdministradorRepository;
import uniquindio.edu.co.inmobiliaria.repositories.AsesorRepository;
import uniquindio.edu.co.inmobiliaria.repositories.ClienteRepository;
import uniquindio.edu.co.inmobiliaria.services.ClienteService;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedLoginUsers(ClienteRepository clienteRepository, ClienteService clienteService,
            AsesorRepository asesorRepository, AdministradorRepository administradorRepository) {
        return args -> {
            if (clienteRepository.findById("3030789").isEmpty()) {
                clienteService.registrarCliente(new ClienteRequest(
                        "3030789",
                        "Juan Cliente",
                        "juan.cliente@inmobiliaria.local",
                        "3001234567",
                        "cliente123",
                        null,
                        TipoCliente.COMPRADOR,
                        Zona.CENTRO,
                        250000000.0,
                        TipoInmueble.APARTAMENTO,
                        3,
                        EstadoBusquedaCliente.BUSCANDO
                ));
            }

            if (asesorRepository.findById("10949001") == null) {
                asesorRepository.save(new Asesor(
                        "Ana Asesora",
                        "10949001",
                        "ana.asesora@inmobiliaria.local",
                        "3009876543",
                        "asesor123",
                        null,
                        Zona.NORTE,
                        TipoInmueble.CASA
                ));
            }

            if (administradorRepository.findById("10000001") == null) {
                administradorRepository.save(new Administrador(
                        "Admin DomusTech",
                        "10000001",
                        "admin@inmobiliaria.local",
                        "3000000000",
                        "admin123",
                        null
                ));
            }
        };
    }
}
