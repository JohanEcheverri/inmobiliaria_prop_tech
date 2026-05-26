package uniquindio.edu.co.inmobiliaria.repositories;

import org.springframework.stereotype.Repository;
import uniquindio.edu.co.inmobiliaria.models.entities.Visita;
import uniquindio.edu.co.inmobiliaria.models.enums.EstadoVisita;
import uniquindio.edu.co.inmobiliaria.repositories.jpa.VisitaJpaRepository;
import uniquindio.edu.co.inmobiliaria.structures.DynamicArrayList;
import uniquindio.edu.co.inmobiliaria.structures.HashTable;
import uniquindio.edu.co.inmobiliaria.structures.Queue;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

@Repository
/**
 * Repositorio en memoria para visitas. Mantiene índices por código y una cola
 * de visitas pendientes para procesamiento FIFO. Sincroniza con VisitaJpaRepository
 * usando queries que traen relaciones para evitar problemas de carga perezosa.
 */
public class VisitasRepository {

    private final VisitaJpaRepository visitaJpaRepository;
    private final DynamicArrayList<Visita> visitas;
    private final HashTable<String, Visita> visitasPorCodigo;
    private final Queue<Visita> visitasPendientes;

    public VisitasRepository(VisitaJpaRepository visitaJpaRepository) {
        this.visitaJpaRepository = visitaJpaRepository;
        this.visitas = new DynamicArrayList<>();
        this.visitasPorCodigo = new HashTable<>();
        this.visitasPendientes = new Queue<>();
        cargarDesdeBaseDeDatos();
    }

    /**
     * Procesa (decola) la siguiente visita pendiente para su ejecución/atención.
     *
     * @return Visita desencolada o null si no hay pendientes
     */
    public Visita procesarVisita() {
        return visitasPendientes.dequeue();
    }

    /**
     * Persiste una visita nueva y la indexa en memoria. Valida que tenga código
     * único.
     *
     * @param visita entidad Visita a guardar
     */
    public void save(Visita visita) {
        if (visita == null || visita.getCodigo() == null || visita.getCodigo().isBlank()) {
            throw new IllegalArgumentException("La visita o su código no pueden ser nulos");
        }
        if (visitasPorCodigo.containsKey(visita.getCodigo())) {
            throw new IllegalArgumentException("Ya existe una visita con el código: " + visita.getCodigo());
        }
        visitaJpaRepository.save(visita);
        agregarAIndices(visita);
    }

    /**
     * Actualiza una visita existente; mantiene referencia en memoria y actualiza
     * las colas e índices según el nuevo estado.
     *
     * @param visitaActualizada visita con cambios
     */
    public void update(Visita visitaActualizada) {
        if (visitaActualizada == null || visitaActualizada.getCodigo() == null || visitaActualizada.getCodigo().isBlank()) {
            throw new IllegalArgumentException("La visita o su código no pueden ser nulos");
        }
        if (!visitasPorCodigo.containsKey(visitaActualizada.getCodigo())) {
            throw new IllegalArgumentException("No se encontró una visita con el código: " + visitaActualizada.getCodigo());
        }
        Visita existente = visitasPorCodigo.get(visitaActualizada.getCodigo());
        boolean estabaPendiente = existente.getEstado() == EstadoVisita.PENDIENTE;
        eliminarDeIndices(existente);
        existente.setCliente(visitaActualizada.getCliente());
        existente.setInmueble(visitaActualizada.getInmueble());
        existente.setFecha(visitaActualizada.getFecha());
        existente.setHora(visitaActualizada.getHora());
        existente.setEstado(visitaActualizada.getEstado());
        existente.setAsesotAsignado(visitaActualizada.getAsesotAsignado());
        existente.setObservaciones(visitaActualizada.getObservaciones());
        visitaJpaRepository.save(existente);
        agregarAIndices(existente);
        if (estabaPendiente && existente.getEstado() != EstadoVisita.PENDIENTE) {
            visitasPendientes.remove(existente);
        }
    }

    public Optional<Visita> findByCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(visitasPorCodigo.get(codigo));
    }

    public boolean existsByCodigo(String codigo) {
        return codigo != null && visitasPorCodigo.containsKey(codigo);
    }

    public void deleteByCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El código de la visita no puede estar vacío");
        }
        Visita visita = visitasPorCodigo.get(codigo);
        if (visita == null) {
            throw new IllegalArgumentException("No se encontró una visita con el código: " + codigo);
        }
        visitaJpaRepository.deleteById(codigo);
        eliminarDeIndices(visita);
    }

    public DynamicArrayList<Visita> findByClienteId(String idCliente) {
        DynamicArrayList<Visita> resultado = new DynamicArrayList<>();
        for (int i = 0; i < visitas.size(); i++) {
            Visita visita = visitas.get(i);
            if (visita.getCliente() != null && idCliente.equals(visita.getCliente().getId())) {
                resultado.add(visita);
            }
        }
        return resultado;
    }

    public DynamicArrayList<Visita> findByInmuebleCodigo(String codigoInmueble) {
        DynamicArrayList<Visita> resultado = new DynamicArrayList<>();
        for (int i = 0; i < visitas.size(); i++) {
            Visita visita = visitas.get(i);
            if (visita.getInmueble() != null && codigoInmueble.equals(visita.getInmueble().getCodigo())) {
                resultado.add(visita);
            }
        }
        return resultado;
    }

    public DynamicArrayList<Visita> findByAsesorId(String idAsesor) {
        DynamicArrayList<Visita> resultado = new DynamicArrayList<>();
        for (int i = 0; i < visitas.size(); i++) {
            Visita visita = visitas.get(i);
            if (visita.getAsesotAsignado() != null && idAsesor.equals(visita.getAsesotAsignado().getId())) {
                resultado.add(visita);
            }
        }
        return resultado;
    }

    public DynamicArrayList<Visita> findByEstado(EstadoVisita estado) {
        DynamicArrayList<Visita> resultado = new DynamicArrayList<>();
        for (int i = 0; i < visitas.size(); i++) {
            if (visitas.get(i).getEstado() == estado) {
                resultado.add(visitas.get(i));
            }
        }
        return resultado;
    }

    public DynamicArrayList<Visita> findByFecha(LocalDate fecha) {
        DynamicArrayList<Visita> resultado = new DynamicArrayList<>();
        for (int i = 0; i < visitas.size(); i++) {
            if (fecha.equals(visitas.get(i).getFecha())) {
                resultado.add(visitas.get(i));
            }
        }
        return resultado;
    }

    public DynamicArrayList<Visita> findAll() {
        return visitas;
    }

    private void cargarDesdeBaseDeDatos() {
        visitaJpaRepository.findAllConRelaciones().forEach(this::agregarAIndices);
    }

    private void agregarAIndices(Visita visita) {
        visitas.add(visita);
        visitasPorCodigo.put(visita.getCodigo(), visita);
        if (visita.getEstado() == EstadoVisita.PENDIENTE) {
            visitasPendientes.enqueue(visita);
        }
    }

    private void eliminarDeIndices(Visita visita) {
        if (visita == null) {
            return;
        }
        for (int i = 0; i < visitas.size(); i++) {
            if (Objects.equals(visitas.get(i).getCodigo(), visita.getCodigo())) {
                visitas.removeAt(i);
                break;
            }
        }
        visitasPorCodigo.remove(visita.getCodigo());
        if (visita.getEstado() == EstadoVisita.PENDIENTE) {
            visitasPendientes.remove(visita);
        }
    }
}
