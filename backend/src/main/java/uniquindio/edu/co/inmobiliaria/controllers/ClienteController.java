package uniquindio.edu.co.inmobiliaria.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uniquindio.edu.co.inmobiliaria.models.dto.ClienteRequest;
import uniquindio.edu.co.inmobiliaria.models.dto.ClienteResponse;
import uniquindio.edu.co.inmobiliaria.services.ClienteService;

import java.util.List;

@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://127.0.0.1:5173", "http://127.0.0.1:5174", "http://localhost:3000"})
@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService clienteService;
    private final uniquindio.edu.co.inmobiliaria.services.InmuebleService inmuebleService;

    public ClienteController(ClienteService clienteService, uniquindio.edu.co.inmobiliaria.services.InmuebleService inmuebleService) {
        this.clienteService = clienteService;
        this.inmuebleService = inmuebleService;
    }

    @GetMapping
    public List<ClienteResponse> listarClientes() {
        return clienteService.listarClientes();
    }

    @GetMapping("/{id}")
    public ClienteResponse obtenerCliente(@PathVariable String id) {
        return clienteService.obtenerCliente(id);
    }

    @GetMapping("/buscar")
    public ClienteResponse buscarPorEmail(@RequestParam String email) {
        return clienteService.obtenerClientePorEmail(email);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClienteResponse registrarCliente(@RequestBody ClienteRequest request) {
        return clienteService.registrarCliente(request);
    }

    @PutMapping("/{id}")
    public ClienteResponse actualizarCliente(@PathVariable String id, @RequestBody ClienteRequest request) {
        return clienteService.actualizarCliente(id, request);
    }

    @GetMapping("/{id}/recomendados")
    public List<uniquindio.edu.co.inmobiliaria.models.dto.InmuebleResponse> recomendaciones(@PathVariable String id) {
        var inmuebles = clienteService.recomendarInmueblesPorPreferencias(id);
        List<uniquindio.edu.co.inmobiliaria.models.dto.InmuebleResponse> respuesta = new java.util.ArrayList<>();
        for (int i = 0; i < inmuebles.size(); i++) {
            respuesta.add(inmuebleService.mapear(inmuebles.get(i)));
        }
        return respuesta;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarCliente(@PathVariable String id) {
        clienteService.eliminarCliente(id);
    }
}
