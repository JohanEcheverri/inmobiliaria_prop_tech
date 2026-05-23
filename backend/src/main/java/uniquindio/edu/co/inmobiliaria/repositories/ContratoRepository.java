package uniquindio.edu.co.inmobiliaria.repositories;

import org.springframework.stereotype.Repository;
import uniquindio.edu.co.inmobiliaria.models.entities.Contrato;
import uniquindio.edu.co.inmobiliaria.repositories.jpa.ContratoJpaRepository;
import uniquindio.edu.co.inmobiliaria.structures.SinglyLinkedList;

import java.util.Objects;
import java.util.Optional;

@Repository
public class ContratoRepository {

    private final ContratoJpaRepository contratoJpaRepository;
    private final SinglyLinkedList<Contrato> contratos;

    public ContratoRepository(ContratoJpaRepository contratoJpaRepository) {
        this.contratoJpaRepository = contratoJpaRepository;
        this.contratos = new SinglyLinkedList<>();
        cargarDesdeBaseDeDatos();
    }

    public Contrato save(Contrato contrato) {
        if (contrato == null) {
            throw new IllegalArgumentException("El contrato no puede ser nulo");
        }
        Contrato guardado = contratoJpaRepository.save(contrato);
        contratos.addLast(guardado);
        return guardado;
    }

    public void update(Contrato contratoActualizado) {
        if (contratoActualizado == null || contratoActualizado.getCodigo() == null) {
            throw new IllegalArgumentException("El contrato o su código no pueden ser nulos");
        }
        contratoJpaRepository.save(contratoActualizado);
        for (Contrato c : contratos.toList()) {
            if (Objects.equals(c.getCodigo(), contratoActualizado.getCodigo())) {
                contratos.remove(c);
                contratos.addLast(contratoActualizado);
                break;
            }
        }
    }

    public Optional<Contrato> findById(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return Optional.empty();
        }
        for (Contrato c : contratos.toList()) {
            if (Objects.equals(c.getCodigo(), codigo)) {
                return Optional.of(c);
            }
        }
        return Optional.empty();
    }

    public SinglyLinkedList<Contrato> findAll() {
        return contratos;
    }

    public boolean deleteById(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return false;
        }
        Optional<Contrato> contrato = findById(codigo);
        if (contrato.isEmpty()) {
            return false;
        }
        contratoJpaRepository.deleteById(codigo);
        contratos.remove(contrato.get());
        return true;
    }

    private void cargarDesdeBaseDeDatos() {
        contratoJpaRepository.findAll().forEach(contratos::addLast);
    }
}
