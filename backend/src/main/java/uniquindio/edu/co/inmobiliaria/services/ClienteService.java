package uniquindio.edu.co.inmobiliaria.services;

import org.springframework.stereotype.Service;
import uniquindio.edu.co.inmobiliaria.models.dto.ClienteRequest;
import uniquindio.edu.co.inmobiliaria.models.dto.ClienteResponse;
import uniquindio.edu.co.inmobiliaria.models.dto.AuthResponse;
import uniquindio.edu.co.inmobiliaria.models.entities.Cliente;
import uniquindio.edu.co.inmobiliaria.models.entities.EventoHistorial;
import uniquindio.edu.co.inmobiliaria.models.entities.Inmueble;
import uniquindio.edu.co.inmobiliaria.models.enums.TipoEventoHistorial;
import uniquindio.edu.co.inmobiliaria.repositories.ClienteRepository;
import uniquindio.edu.co.inmobiliaria.repositories.EventoHistorialRepository;
import uniquindio.edu.co.inmobiliaria.repositories.InmuebleRepository;
import uniquindio.edu.co.inmobiliaria.structures.DynamicArrayList;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final InmuebleRepository inmuebleRepository;
    private final EventoHistorialRepository eventoHistorialRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public ClienteService(ClienteRepository clienteRepository,
                          InmuebleRepository inmuebleRepository,
                          EventoHistorialRepository eventoHistorialRepository,
                          BCryptPasswordEncoder passwordEncoder) {
        this.clienteRepository = clienteRepository;
        this.inmuebleRepository = inmuebleRepository;
        this.eventoHistorialRepository = eventoHistorialRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public ClienteResponse registrarCliente(ClienteRequest request) {
        validarCliente(request, true);
        if (clienteRepository.existsById(request.id())) {
            throw new IllegalArgumentException("Ya existe un cliente con la cédula: " + request.id());
        }
        if (clienteRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un cliente con el email: " + request.email());
        }

        Cliente cliente = construirCliente(request);
        clienteRepository.save(cliente);
        return mapear(cliente);
    }

    public Optional<AuthResponse> autenticar(String id, String password) {
        if (estaVacio(id) || estaVacio(password)) {
            return Optional.empty();
        }
        return clienteRepository.findById(id)
                .filter(cliente -> passwordCoincide(cliente.getContrasenia(), password))
                .map(this::mapearAuth);
    }

    public void registrarCliente(String cedula, String nombre, String telefono, String email) {
        registrarCliente(new ClienteRequest(
                cedula,
                nombre,
                email,
                telefono,
                null,
                null,
                null,
                null,
                null,
                null,
                0,
                null
        ));
    }

    public DynamicArrayList<Inmueble> recomendarInmueblesPorPreferencias(String clienteId) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró un cliente con el id: " + clienteId));

        DynamicArrayList<Inmueble> inmuebles = inmuebleRepository.findAll();
        DynamicArrayList<EventoHistorial> eventos = eventoHistorialRepository.findByClienteId(clienteId);

        List<InmuebleScore> puntajes = new ArrayList<>();
        for (int i = 0; i < inmuebles.size(); i++) {
            Inmueble inmueble = inmuebles.get(i);
            int score = calcularPuntajeInmueble(cliente, inmueble, eventos);
            if (score > 0) {
                puntajes.add(new InmuebleScore(inmueble, score));
            }
        }
        puntajes.sort(Comparator.comparingInt(InmuebleScore::getScore).reversed());

        DynamicArrayList<Inmueble> recomendados = new DynamicArrayList<>();
        for (InmuebleScore item : puntajes) {
            recomendados.add(item.getInmueble());
        }
        return recomendados;
    }

    private int calcularPuntajeInmueble(Cliente cliente, Inmueble inmueble, DynamicArrayList<EventoHistorial> eventos) {
        int score = 0;
        if (cliente.getZonaInteres() != null
                && inmueble.getBarrio() != null
                && cliente.getZonaInteres().equals(inmueble.getBarrio().getZona())) {
            score += 30;
        }
        if (cliente.getTipoInmuebleDeseado() != null
                && cliente.getTipoInmuebleDeseado().equals(inmueble.getTipoInmueble())) {
            score += 25;
        }
        if (cliente.getPresupuesto() != null && cliente.getPresupuesto() > 0) {
            if (inmueble.getPrecio() <= cliente.getPresupuesto()) {
                score += 20;
                double distanciaPresupuesto = cliente.getPresupuesto() - inmueble.getPrecio();
                if (distanciaPresupuesto >= 0 && distanciaPresupuesto <= cliente.getPresupuesto() * 0.10) {
                    score += 10;
                }
            }
        }
        if (cliente.getNumeroHabitacionesDeseadas() > 0) {
            int diferenciaHabitaciones = Math.abs(cliente.getNumeroHabitacionesDeseadas() - inmueble.getNumeroHabitaciones());
            if (diferenciaHabitaciones == 0) {
                score += 15;
            } else if (diferenciaHabitaciones == 1) {
                score += 8;
            }
        }
        for (int i = 0; i < eventos.size(); i++) {
            EventoHistorial evento = eventos.get(i);
            if (evento.getInmueble() == null || !evento.getInmueble().getCodigo().equals(inmueble.getCodigo())) {
                continue;
            }
            score += obtenerPuntajePorEvento(evento.getTipoEvento());
        }
        return score;
    }

    private int obtenerPuntajePorEvento(TipoEventoHistorial tipo) {
        return switch (tipo) {
            case FAVORITO -> 50;
            case GUARDADO -> 20;
            case VISITA -> 25;
            case NEGOCIANDO -> 30;
            case CONSULTA -> 10;
            case DESCARTADO -> -25;
        };
    }

    private static final class InmuebleScore {
        private final Inmueble inmueble;
        private final int score;

        public InmuebleScore(Inmueble inmueble, int score) {
            this.inmueble = inmueble;
            this.score = score;
        }

        public Inmueble getInmueble() {
            return inmueble;
        }

        public int getScore() {
            return score;
        }
    }

    public ClienteResponse actualizarCliente(String id, ClienteRequest request) {
        if (estaVacio(id)) {
            throw new IllegalArgumentException("El id del cliente es obligatorio");
        }
        validarCliente(request, false);
        Cliente clienteExistente = clienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró un cliente con el id: " + id));

        clienteRepository.findByEmail(request.email())
                .filter(cliente -> !cliente.getId().equals(id))
                .ifPresent(cliente -> {
                    throw new IllegalArgumentException("Ya existe un cliente con el email: " + request.email());
                });

        Cliente clienteActualizado = Cliente.builder()
                .id(id)
                .nombre(request.nombre())
                .email(request.email())
                .telefono(request.telefono())

                .contrasenia(!estaVacio(request.password()) ? passwordEncoder.encode(request.password()) : clienteExistente.getContrasenia())

                .fotoPerfil(request.fotoPerfil())
                .tipoCliente(request.tipoCliente())
                .zonaInteres(request.zonaInteres())
                .presupuesto(request.presupuesto())
                .tipoInmuebleDeseado(request.tipoInmuebleDeseado())
                .numeroHabitacionesDeseadas(request.numeroHabitacionesDeseadas())
                .estadoBusqueda(request.estadoBusqueda())
                .build();

        clienteRepository.update(clienteActualizado);
        return mapear(clienteActualizado);
    }

    public ClienteResponse obtenerCliente(String id) {
        return clienteRepository.findById(id)
                .map(this::mapear)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró un cliente con el id: " + id));
    }

    public ClienteResponse obtenerClientePorEmail(String email) {
        return clienteRepository.findByEmail(email)
                .map(this::mapear)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró un cliente con el email: " + email));
    }

    public List<ClienteResponse> listarClientes() {
        List<ClienteResponse> respuesta = new ArrayList<>();
        DynamicArrayList<Cliente> clientes = clienteRepository.findAll();
        for (int i = 0; i < clientes.size(); i++) {
            respuesta.add(mapear(clientes.get(i)));
        }
        return respuesta;
    }

    public void eliminarCliente(String id) {
        if (!clienteRepository.deleteById(id)) {
            throw new IllegalArgumentException("No se encontró un cliente con el id: " + id);
        }
    }

    private Cliente construirCliente(ClienteRequest request) {
        return Cliente.builder()
                .id(request.id())
                .nombre(request.nombre())
                .email(request.email())
                .telefono(request.telefono())
                .contrasenia(passwordEncoder.encode(request.password()))
                .fotoPerfil(request.fotoPerfil())
                .tipoCliente(request.tipoCliente())
                .zonaInteres(request.zonaInteres())
                .presupuesto(request.presupuesto())
                .tipoInmuebleDeseado(request.tipoInmuebleDeseado())
                .numeroHabitacionesDeseadas(request.numeroHabitacionesDeseadas())
                .estadoBusqueda(request.estadoBusqueda())
                .build();
    }

    private ClienteResponse mapear(Cliente cliente) {
        return new ClienteResponse(
                cliente.getId(),
                cliente.getNombre(),
                cliente.getEmail(),
                cliente.getTelefono(),
                cliente.getFotoPerfil(),
                cliente.getTipoCliente(),
                cliente.getZonaInteres(),
                cliente.getPresupuesto(),
                cliente.getTipoInmuebleDeseado(),
                cliente.getNumeroHabitacionesDeseadas(),
                cliente.getEstadoBusqueda()
        );
    }

    private AuthResponse mapearAuth(Cliente cliente) {
        return new AuthResponse(
                cliente.getId(),
                cliente.getNombre(),
                cliente.getEmail(),
                cliente.getTelefono(),
                cliente.getFotoPerfil(),
                "CLIENTE"
        );
    }

    private void validarCliente(ClienteRequest request, boolean validarId) {
        if (request == null) {
            throw new IllegalArgumentException("Los datos del cliente son obligatorios");
        }
        if (validarId && estaVacio(request.id())) {
            throw new IllegalArgumentException("La cédula del cliente no puede estar vacía");
        }
        if (estaVacio(request.nombre())) {
            throw new IllegalArgumentException("El nombre del cliente no puede estar vacío");
        }
        if (estaVacio(request.telefono())) {
            throw new IllegalArgumentException("El teléfono del cliente no puede estar vacío");
        }
        if (estaVacio(request.email())) {
            throw new IllegalArgumentException("El email del cliente no puede estar vacío");
        }
        if (request.presupuesto() != null && request.presupuesto() < 0) {
            throw new IllegalArgumentException("El presupuesto no puede ser negativo");
        }
        if (request.numeroHabitacionesDeseadas() < 0) {
            throw new IllegalArgumentException("El número de habitaciones no puede ser negativo");
        }
    }

    private boolean estaVacio(String valor) {
        return valor == null || valor.isBlank();
    }

    private boolean passwordCoincide(String passwordGuardada, String passwordIngresada) {
        return passwordGuardada != null && passwordEncoder.matches(passwordIngresada, passwordGuardada);
    }
}
