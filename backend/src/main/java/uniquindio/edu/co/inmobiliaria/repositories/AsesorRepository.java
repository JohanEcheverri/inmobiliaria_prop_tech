package uniquindio.edu.co.inmobiliaria.repositories;

import lombok.Getter;
import org.springframework.stereotype.Repository;
import uniquindio.edu.co.inmobiliaria.models.entities.Asesor;
import uniquindio.edu.co.inmobiliaria.repositories.jpa.AsesorJpaRepository;
import uniquindio.edu.co.inmobiliaria.structures.DynamicArrayList;
import uniquindio.edu.co.inmobiliaria.structures.HashTable;
import uniquindio.edu.co.inmobiliaria.structures.Tree;

import java.util.Objects;
import java.util.Optional;

@Repository
/**
 * Repositorio en memoria para asesores. Mantiene índices por id y email y un
 * árbol que ordena por numero de cierres para consultas de ranking.
 */
public class AsesorRepository {

    private final AsesorJpaRepository asesorJpaRepository;

    @Getter
    public final Tree<Asesor> numeroDeCierres;
    @Getter
    public final HashTable<String, Asesor> asesoresPorId;
    @Getter
    public final HashTable<String, Asesor> asesoresPorEmail;

    public AsesorRepository(AsesorJpaRepository asesorJpaRepository) {
        this.asesorJpaRepository = asesorJpaRepository;
        this.numeroDeCierres = new Tree<>(this::compararPorCierresEId);
        this.asesoresPorId = new HashTable<>();
        this.asesoresPorEmail = new HashTable<>();
        cargarDesdeBaseDeDatos();
    }

    /**
     * Guarda un asesor en la base y lo indexa en memoria (id, email y árbol de cierres).
     *
     * @param asesor entidad Asesor a guardar
     */
    public void save(Asesor asesor) {
        validarAsesorConId(asesor);
        if (asesoresPorId.containsKey(asesor.getId())) {
            throw new IllegalArgumentException("Ya existe un asesor con el id: " + asesor.getId());
        }
        if (asesor.getEmail() != null && asesoresPorEmail.containsKey(asesor.getEmail())) {
            throw new IllegalArgumentException("Ya existe un asesor con el email: " + asesor.getEmail());
        }
        asesorJpaRepository.save(asesor);
        agregarAIndices(asesor);
    }

    /**
     * Actualiza un asesor existente y sincroniza índices en memoria.
     *
     * @param asesorActualizado asesor con id existente
     */
    public void update(Asesor asesorActualizado) {
        validarAsesorConId(asesorActualizado);
        if (!asesoresPorId.containsKey(asesorActualizado.getId())) {
            throw new IllegalArgumentException("No se encontró un asesor con el id: " + asesorActualizado.getId());
        }

        Asesor anterior = asesoresPorId.get(asesorActualizado.getId());
        if (asesorActualizado.getEmail() != null
                && asesoresPorEmail.containsKey(asesorActualizado.getEmail())
                && !Objects.equals(anterior.getId(), asesoresPorEmail.get(asesorActualizado.getEmail()).getId())) {
            throw new IllegalArgumentException("Ya existe un asesor con el email: " + asesorActualizado.getEmail());
        }

        asesorJpaRepository.save(asesorActualizado);
        eliminarDeIndices(anterior);
        agregarAIndices(asesorActualizado);
    }

    public boolean deleteById(String id) {
        Optional<Asesor> asesor = findById(id);
        if (asesor.isEmpty()) {
            return false;
        }
        asesorJpaRepository.deleteById(id);
        eliminarDeIndices(asesor.get());
        return true;
    }

    /**
     * Busca un asesor por id usando el índice en memoria.
     *
     * @param id identificador del asesor
     * @return Optional con el asesor si existe
     */
    public Optional<Asesor> findById(String id) {
        if (id == null || !asesoresPorId.containsKey(id)) {
            return Optional.empty();
        }
        return Optional.of(asesoresPorId.get(id));
    }

    public Optional<Asesor> findByEmail(String email) {
        if (email == null || !asesoresPorEmail.containsKey(email)) {
            return Optional.empty();
        }
        return Optional.of(asesoresPorEmail.get(email));
    }

    public DynamicArrayList<Asesor> findAll() {
        return asesoresPorId.values();
    }

    private void cargarDesdeBaseDeDatos() {
        asesorJpaRepository.findAll().forEach(this::agregarAIndices);
    }

    private void agregarAIndices(Asesor asesor) {
        asesoresPorId.put(asesor.getId(), asesor);
        if (asesor.getEmail() != null) {
            asesoresPorEmail.put(asesor.getEmail(), asesor);
        }
        numeroDeCierres.insert(asesor);
    }

    private void eliminarDeIndices(Asesor asesor) {
        asesoresPorId.remove(asesor.getId());
        if (asesor.getEmail() != null && asesoresPorEmail.containsKey(asesor.getEmail())) {
            asesoresPorEmail.remove(asesor.getEmail());
        }
        numeroDeCierres.remove(asesor);
    }

    private void validarAsesorConId(Asesor asesor) {
        if (asesor == null || asesor.getId() == null || asesor.getId().isBlank()) {
            throw new IllegalArgumentException("El asesor o su id no pueden ser nulos");
        }
    }

    private int compararPorCierresEId(Asesor a1, Asesor a2) {
        int cierres1 = a1.getNumeroDeCierres() != null ? a1.getNumeroDeCierres() : 0;
        int cierres2 = a2.getNumeroDeCierres() != null ? a2.getNumeroDeCierres() : 0;
        int comparacionCierres = Integer.compare(cierres2, cierres1);
        if (comparacionCierres != 0) {
            return comparacionCierres;
        }
        if (Objects.equals(a1.getId(), a2.getId())) {
            return 0;
        }
        if (a1.getId() == null) {
            return -1;
        }
        if (a2.getId() == null) {
            return 1;
        }
        return a1.getId().compareTo(a2.getId());
    }
}
