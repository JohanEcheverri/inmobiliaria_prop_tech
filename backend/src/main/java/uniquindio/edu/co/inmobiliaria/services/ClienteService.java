package uniquindio.edu.co.inmobiliaria.services;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import uniquindio.edu.co.inmobiliaria.repositories.ClienteRepository;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    @Autowired
    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public void registrarCliente(String cedula, String nombre, String telefono, String email) {
        if (cedula == null || cedula.isBlank()) {
            throw new IllegalArgumentException("La cédula del cliente no puede estar vacía");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del cliente no puede estar vacío");
        }
        if (telefono == null || telefono.isBlank()) {
            throw new IllegalArgumentException("El teléfono del cliente no puede estar vacío");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("El email del cliente no puede estar vacío");
        }
       // Cliente cliente = Cliente.builder()
        //clienteRepository.save(cedula, nombre, telefono, email);
    }

}
