package uniquindio.edu.co.inmobiliaria.repositories;

import org.springframework.stereotype.Repository;
import uniquindio.edu.co.inmobiliaria.models.entities.Visita;
import uniquindio.edu.co.inmobiliaria.models.enums.EstadoVisita;
import uniquindio.edu.co.inmobiliaria.repositories.jpa.VisitaJpaRepository;
import uniquindio.edu.co.inmobiliaria.structures.DynamicArrayList;
import uniquindio.edu.co.inmobiliaria.structures.HashTable;

import java.time.LocalDate;

@Repository
public class VisitasRepository {

    private final VisitaJpaRepository visitaJpaRepository;
    private final DynamicArrayList<Visita> visitas;
    private final HashTable<String, Visita> visitasPorCodigo;

    
    private final Queue<Visita> visitasPendientes; // Para gestionar visitas que aún no se han realizado, ordenadas por fecha

    public VisitasRepository(VisitaJpaRepository visitaJpaRepository) {
        this.visitaJpaRepository = visitaJpaRepository;
        this.visitas = new DynamicArrayList<>();
        this.visitasPorCodigo = new HashTable<>();
        cargarDesdeBaseDeDatos();
    }

    public Visita procesarVisita(){
        return visitasPendientes.dequeue(); // Devuelve la visita más próxima a realizarse y la elimina de la cola
    }

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

    private void cargarDesdeBaseDeDatos() {
        visitaJpaRepository.findAll().forEach(this::agregarAIndices);
    }

    private void agregarAIndices(Visita visita) {
        visitas.add(visita);
        visitasPorCodigo.put(visita.getCodigo(), visita);
    }
}
