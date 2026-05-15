package uniquindio.edu.co.inmobiliaria.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uniquindio.edu.co.inmobiliaria.models.dto.ClienteRequest;
import uniquindio.edu.co.inmobiliaria.models.enums.EstadoBusquedaCliente;
import uniquindio.edu.co.inmobiliaria.models.enums.TipoCliente;
import uniquindio.edu.co.inmobiliaria.models.enums.TipoInmueble;
import uniquindio.edu.co.inmobiliaria.models.enums.Zona;
import uniquindio.edu.co.inmobiliaria.repositories.ClienteRepository;
import uniquindio.edu.co.inmobiliaria.services.ClienteService;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedLoginUser(ClienteRepository clienteRepository, ClienteService clienteService) {
        return args -> {
            if (clienteRepository.findById("3030789").isPresent()) {
                return;
            }

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
        };
    }
}
