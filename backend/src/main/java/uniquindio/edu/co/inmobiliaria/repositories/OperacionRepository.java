package uniquindio.edu.co.inmobiliaria.repositories;

import org.springframework.stereotype.Repository;
import uniquindio.edu.co.inmobiliaria.models.entities.Arriendo;
import uniquindio.edu.co.inmobiliaria.models.entities.Cancelacion;
import uniquindio.edu.co.inmobiliaria.models.entities.Operacion;
import uniquindio.edu.co.inmobiliaria.models.entities.Renovacion;
import uniquindio.edu.co.inmobiliaria.models.entities.Venta;
import uniquindio.edu.co.inmobiliaria.models.enums.EstadoOperacion;
import uniquindio.edu.co.inmobiliaria.structures.DynamicArrayList;

import java.util.Objects;
import java.util.Optional;

@Repository
public class OperacionRepository {

    private final DynamicArrayList<Operacion> operaciones;

    public OperacionRepository() {
        this.operaciones = new DynamicArrayList<>();
    }

    public void save(Operacion operacion) {
        Objects.requireNonNull(operacion, "La operación no puede ser nula");
        if (operacion.getCodigo() == null || operacion.getCodigo().isBlank()) {
            throw new IllegalArgumentException("El código de la operación no puede estar vacío");
        }
        if (findByCodigo(operacion.getCodigo()).isPresent()) {
            throw new IllegalArgumentException(
                    "Ya existe una operación con el código: " + operacion.getCodigo());
        }
        operaciones.add(operacion);
    }

    public void update(Operacion operacionActualizada) {
        Objects.requireNonNull(operacionActualizada, "La operación no puede ser nula");
        for (int i = 0; i < operaciones.size(); i++) {
            if (Objects.equals(operaciones.get(i).getCodigo(), operacionActualizada.getCodigo())) {
                operaciones.set(i, operacionActualizada);
                return;
            }
        }
        throw new IllegalArgumentException(
                "No se encontró una operación con el código: " + operacionActualizada.getCodigo());
    }

    public boolean deleteByCodigo(String codigo) {
        for (int i = 0; i < operaciones.size(); i++) {
            if (Objects.equals(operaciones.get(i).getCodigo(), codigo)) {
                operaciones.removeAt(i);
                return true;
            }
        }
        return false;
    }

    public DynamicArrayList<Operacion> findAll() {
        return operaciones;
    }

    public int count() {
        return operaciones.size();
    }

    public boolean isEmpty() {
        return operaciones.isEmpty();
    }

    public Optional<Operacion> findByCodigo(String codigo) {
        for (int i = 0; i < operaciones.size(); i++) {
            if (Objects.equals(operaciones.get(i).getCodigo(), codigo)) {
                return Optional.of(operaciones.get(i));
            }
        }
        return Optional.empty();
    }

    public DynamicArrayList<Operacion> findByEstado(EstadoOperacion estado) {
        DynamicArrayList<Operacion> resultado = new DynamicArrayList<>();
        for (int i = 0; i < operaciones.size(); i++) {
            if (operaciones.get(i).getEstado() == estado) {
                resultado.add(operaciones.get(i));
            }
        }
        return resultado;
    }

    public DynamicArrayList<Operacion> findByIdCliente(String idCliente) {
        DynamicArrayList<Operacion> resultado = new DynamicArrayList<>();
        for (int i = 0; i < operaciones.size(); i++) {
            Operacion operacion = operaciones.get(i);
            if (operacion.getCliente() != null
                    && Objects.equals(operacion.getCliente().getId(), idCliente)) {
                resultado.add(operacion);
            }
        }
        return resultado;
    }

    public DynamicArrayList<Operacion> findByCodigoCliente(String codigoCliente) {
        return findByIdCliente(codigoCliente);
    }

    public DynamicArrayList<Operacion> findByIdAsesor(String idAsesor) {
        DynamicArrayList<Operacion> resultado = new DynamicArrayList<>();
        for (int i = 0; i < operaciones.size(); i++) {
            Operacion operacion = operaciones.get(i);
            if (operacion.getAsesor() != null
                    && Objects.equals(operacion.getAsesor().getId(), idAsesor)) {
                resultado.add(operacion);
            }
        }
        return resultado;
    }

    public DynamicArrayList<Operacion> findByCodigoInmueble(String codigoInmueble) {
        DynamicArrayList<Operacion> resultado = new DynamicArrayList<>();
        for (int i = 0; i < operaciones.size(); i++) {
            Operacion operacion = operaciones.get(i);
            if (operacion.getInmueble() != null
                    && Objects.equals(operacion.getInmueble().getCodigo(), codigoInmueble)) {
                resultado.add(operacion);
            }
        }
        return resultado;
    }

    public DynamicArrayList<Venta> findVentas() {
        return findByTipo(Venta.class);
    }

    public DynamicArrayList<Arriendo> findArriendos() {
        return findByTipo(Arriendo.class);
    }

    public DynamicArrayList<Renovacion> findRenovaciones() {
        return findByTipo(Renovacion.class);
    }

    public DynamicArrayList<Cancelacion> findCancelaciones() {
        return findByTipo(Cancelacion.class);
    }

    public <T extends Operacion> DynamicArrayList<T> findByTipo(Class<T> tipoOperacion) {
        Objects.requireNonNull(tipoOperacion, "El tipo de operación no puede ser nulo");
        DynamicArrayList<T> resultado = new DynamicArrayList<>();
        for (int i = 0; i < operaciones.size(); i++) {
            Operacion operacion = operaciones.get(i);
            if (tipoOperacion.isInstance(operacion)) {
                resultado.add(tipoOperacion.cast(operacion));
            }
        }
        return resultado;
    }
}
