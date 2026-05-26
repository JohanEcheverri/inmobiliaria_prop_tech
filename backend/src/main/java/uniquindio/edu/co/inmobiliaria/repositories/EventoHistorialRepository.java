package uniquindio.edu.co.inmobiliaria.repositories;

import org.springframework.stereotype.Repository;
import uniquindio.edu.co.inmobiliaria.models.entities.EventoHistorial;
import uniquindio.edu.co.inmobiliaria.models.enums.TipoEventoHistorial;
import uniquindio.edu.co.inmobiliaria.repositories.jpa.EventoHistorialJpaRepository;
import uniquindio.edu.co.inmobiliaria.structures.DynamicArrayList;
import uniquindio.edu.co.inmobiliaria.structures.HashTable;

import java.util.Objects;

@Repository
/**
 * Repositorio en memoria para eventos de historial. Mantiene una lista y un
 * índice por id para búsquedas rápidas, sincronizándose con EventoHistorialJpaRepository.
 */
public class EventoHistorialRepository {

    private final EventoHistorialJpaRepository eventoHistorialJpaRepository;
    private final DynamicArrayList<EventoHistorial> eventos;
    private final HashTable<Long, EventoHistorial> eventosPorId;

    public EventoHistorialRepository(EventoHistorialJpaRepository eventoHistorialJpaRepository) {
        this.eventoHistorialJpaRepository = eventoHistorialJpaRepository;
        this.eventos = new DynamicArrayList<>();
        this.eventosPorId = new HashTable<>();
        cargarDesdeBaseDeDatos();
    }

    /**
     * Persiste un evento de historial en la base de datos y lo añade al cache en memoria.
     * Valida que el evento tenga cliente e inmueble asociados.
     *
     * @param evento EventoHistorial a guardar
     * @return evento persistido con id asignado
     */
    public EventoHistorial save(EventoHistorial evento) {
        if (evento == null) {
            throw new IllegalArgumentException("El evento de historial no puede ser nulo");
        }
        if (evento.getCliente() == null) {
            throw new IllegalArgumentException("El evento debe tener un cliente asociado");
        }
        if (evento.getInmueble() == null) {
            throw new IllegalArgumentException("El evento debe tener un inmueble asociado");
        }
        EventoHistorial guardado = eventoHistorialJpaRepository.save(evento);
        if (guardado.getId() != null) {
            eventos.add(guardado);
            eventosPorId.put(guardado.getId(), guardado);
        }
        return guardado;
    }

    /**
     * Obtiene los eventos de historial asociados a un cliente por su id.
     *
     * @param clienteId id del cliente
     * @return lista de eventos asociados
     */
    public DynamicArrayList<EventoHistorial> findByClienteId(String clienteId) {
        DynamicArrayList<EventoHistorial> resultado = new DynamicArrayList<>();
        for (int i = 0; i < eventos.size(); i++) {
            EventoHistorial evento = eventos.get(i);
            if (evento.getCliente() != null && Objects.equals(evento.getCliente().getId(), clienteId)) {
                resultado.add(evento);
            }
        }
        return resultado;
    }

    /**
     * Filtra eventos por cliente y tipo de evento (VISITA, FAVORITO, etc.).
     *
     * @param clienteId id del cliente
     * @param tipo tipo de evento historial
     * @return lista de eventos que coinciden
     */
    public DynamicArrayList<EventoHistorial> findByClienteIdAndTipo(String clienteId, TipoEventoHistorial tipo) {
        DynamicArrayList<EventoHistorial> resultado = new DynamicArrayList<>();
        for (int i = 0; i < eventos.size(); i++) {
            EventoHistorial evento = eventos.get(i);
            if (evento.getCliente() != null
                    && Objects.equals(evento.getCliente().getId(), clienteId)
                    && evento.getTipoEvento() == tipo) {
                resultado.add(evento);
            }
        }
        return resultado;
    }

    public DynamicArrayList<EventoHistorial> findAll() {
        return eventos;
    }

    private void cargarDesdeBaseDeDatos() {
        eventoHistorialJpaRepository.findAll().forEach(evento -> {
            if (evento.getId() != null) {
                eventos.add(evento);
                eventosPorId.put(evento.getId(), evento);
            }
        });
    }
}
