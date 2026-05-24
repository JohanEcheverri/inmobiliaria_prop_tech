package uniquindio.edu.co.inmobiliaria.services;

import org.springframework.stereotype.Service;
import uniquindio.edu.co.inmobiliaria.models.entities.Cliente;
import uniquindio.edu.co.inmobiliaria.models.entities.EventoHistorial;
import uniquindio.edu.co.inmobiliaria.models.entities.Inmueble;
import uniquindio.edu.co.inmobiliaria.models.enums.TipoEventoHistorial;
import uniquindio.edu.co.inmobiliaria.repositories.ClienteRepository;
import uniquindio.edu.co.inmobiliaria.repositories.EventoHistorialRepository;
import uniquindio.edu.co.inmobiliaria.repositories.InmuebleRepository;
import uniquindio.edu.co.inmobiliaria.structures.DynamicArrayList;

@Service
public class EventoHistorialService {

    private final EventoHistorialRepository eventoHistorialRepository;
    private final ClienteRepository clienteRepository;
    private final InmuebleRepository inmuebleRepository;

    public EventoHistorialService(EventoHistorialRepository eventoHistorialRepository,
                                  ClienteRepository clienteRepository,
                                  InmuebleRepository inmuebleRepository) {
        this.eventoHistorialRepository = eventoHistorialRepository;
        this.clienteRepository = clienteRepository;
        this.inmuebleRepository = inmuebleRepository;
    }

    public EventoHistorial registrarEvento(EventoHistorial evento) {
        if (evento == null) {
            throw new IllegalArgumentException("El evento de historial no puede ser nulo");
        }
        if (evento.getCliente() == null) {
            throw new IllegalArgumentException("El evento debe estar asociado a un cliente");
        }
        if (evento.getInmueble() == null) {
            throw new IllegalArgumentException("El evento debe estar asociado a un inmueble");
        }
        return eventoHistorialRepository.save(evento);
    }

    public EventoHistorial registerEvent(String clienteId, String inmuebleCodigo, TipoEventoHistorial tipoEvento) {
        Cliente cliente = obtenerCliente(clienteId);
        Inmueble inmueble = obtenerInmueble(inmuebleCodigo);
        return registrarEvento(cliente, inmueble, tipoEvento);
    }

    public EventoHistorial registrarEvento(Cliente cliente, Inmueble inmueble, TipoEventoHistorial tipoEvento) {
        if (cliente == null) {
            throw new IllegalArgumentException("El cliente del evento es obligatorio");
        }
        if (inmueble == null) {
            throw new IllegalArgumentException("El inmueble del evento es obligatorio");
        }
        if (tipoEvento == null) {
            throw new IllegalArgumentException("El tipo de evento es obligatorio");
        }
        return eventoHistorialRepository.save(new EventoHistorial(inmueble, tipoEvento, cliente));
    }

    public EventoHistorial marcarFavorito(Cliente cliente, Inmueble inmueble) {
        if (cliente == null || inmueble == null) {
            throw new IllegalArgumentException("El cliente y el inmueble son obligatorios para marcar favorito");
        }
        return registrarEvento(cliente, inmueble, TipoEventoHistorial.FAVORITO);
    }

    public EventoHistorial markFavorite(String clienteId, String inmuebleCodigo) {
        return registerEvent(clienteId, inmuebleCodigo, TipoEventoHistorial.FAVORITO);
    }

    public EventoHistorial registerConsultation(String clienteId, String inmuebleCodigo) {
        return registerEvent(clienteId, inmuebleCodigo, TipoEventoHistorial.CONSULTA);
    }

    public EventoHistorial discardProperty(String clienteId, String inmuebleCodigo) {
        return registerEvent(clienteId, inmuebleCodigo, TipoEventoHistorial.DESCARTADO);
    }

    public EventoHistorial saveProperty(String clienteId, String inmuebleCodigo) {
        return registerEvent(clienteId, inmuebleCodigo, TipoEventoHistorial.GUARDADO);
    }

    public EventoHistorial startNegotiation(String clienteId, String inmuebleCodigo) {
        return registerEvent(clienteId, inmuebleCodigo, TipoEventoHistorial.NEGOCIANDO);
    }

    public EventoHistorial registerVisitEvent(String clienteId, String inmuebleCodigo) {
        return registerEvent(clienteId, inmuebleCodigo, TipoEventoHistorial.VISITA);
    }

    public DynamicArrayList<EventoHistorial> consultarHistorialCliente(String clienteId) {
        return eventoHistorialRepository.findByClienteId(clienteId);
    }

    public DynamicArrayList<EventoHistorial> consultarFavoritosCliente(String clienteId) {
        return eventoHistorialRepository.findByClienteIdAndTipo(clienteId, TipoEventoHistorial.FAVORITO);
    }

    private Cliente obtenerCliente(String clienteId) {
        if (clienteId == null || clienteId.isBlank()) {
            throw new IllegalArgumentException("El id del cliente es obligatorio");
        }
        return clienteRepository.findById(clienteId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró un cliente con el id: " + clienteId));
    }

    private Inmueble obtenerInmueble(String inmuebleCodigo) {
        if (inmuebleCodigo == null || inmuebleCodigo.isBlank()) {
            throw new IllegalArgumentException("El código del inmueble es obligatorio");
        }
        return inmuebleRepository.findById(inmuebleCodigo)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró un inmueble con el código: " + inmuebleCodigo));
    }
}
