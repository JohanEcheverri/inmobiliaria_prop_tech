package uniquindio.edu.co.inmobiliaria.services;

import org.springframework.stereotype.Service;
import uniquindio.edu.co.inmobiliaria.models.entities.Cliente;
import uniquindio.edu.co.inmobiliaria.models.entities.EventoHistorial;
import uniquindio.edu.co.inmobiliaria.models.entities.Inmueble;
import uniquindio.edu.co.inmobiliaria.models.enums.TipoEventoHistorial;
import uniquindio.edu.co.inmobiliaria.repositories.EventoHistorialRepository;
import uniquindio.edu.co.inmobiliaria.structures.DynamicArrayList;

@Service
public class EventoHistorialService {

    private final EventoHistorialRepository eventoHistorialRepository;

    public EventoHistorialService(EventoHistorialRepository eventoHistorialRepository) {
        this.eventoHistorialRepository = eventoHistorialRepository;
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

    public EventoHistorial marcarFavorito(Cliente cliente, Inmueble inmueble) {
        if (cliente == null || inmueble == null) {
            throw new IllegalArgumentException("El cliente y el inmueble son obligatorios para marcar favorito");
        }
        EventoHistorial favorito = new EventoHistorial(inmueble, TipoEventoHistorial.FAVORITO, cliente);
        return eventoHistorialRepository.save(favorito);
    }

    public DynamicArrayList<EventoHistorial> consultarHistorialCliente(String clienteId) {
        return eventoHistorialRepository.findByClienteId(clienteId);
    }

    public DynamicArrayList<EventoHistorial> consultarFavoritosCliente(String clienteId) {
        return eventoHistorialRepository.findByClienteIdAndTipo(clienteId, TipoEventoHistorial.FAVORITO);
    }
}
