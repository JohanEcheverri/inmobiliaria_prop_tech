package uniquindio.edu.co.inmobiliaria.services;

import org.springframework.stereotype.Service;
import uniquindio.edu.co.inmobiliaria.models.entities.Arriendo;
import uniquindio.edu.co.inmobiliaria.models.entities.Cancelacion;
import uniquindio.edu.co.inmobiliaria.models.entities.Operacion;
import uniquindio.edu.co.inmobiliaria.models.entities.Renovacion;
import uniquindio.edu.co.inmobiliaria.models.entities.Venta;
import uniquindio.edu.co.inmobiliaria.models.entities.Visita;
import uniquindio.edu.co.inmobiliaria.models.enums.EstadoOperacion;
import uniquindio.edu.co.inmobiliaria.models.enums.Zona;
import uniquindio.edu.co.inmobiliaria.repositories.OperacionRepository;
import uniquindio.edu.co.inmobiliaria.repositories.VisitasRepository;
import uniquindio.edu.co.inmobiliaria.repositories.ClienteRepository;
import uniquindio.edu.co.inmobiliaria.repositories.InmuebleRepository;
import uniquindio.edu.co.inmobiliaria.repositories.AsesorRepository;
import uniquindio.edu.co.inmobiliaria.models.entities.Cliente;
import uniquindio.edu.co.inmobiliaria.models.entities.Inmueble;
import uniquindio.edu.co.inmobiliaria.models.entities.Asesor;
import uniquindio.edu.co.inmobiliaria.models.entities.Venta;
import uniquindio.edu.co.inmobiliaria.models.enums.EstadoOperacion;
import uniquindio.edu.co.inmobiliaria.structures.DynamicArrayList;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class OperacionService {

    private final OperacionRepository operacionRepository;
    private final VisitasRepository visitasRepository;
    private final ClienteRepository clienteRepository;
    private final InmuebleRepository inmuebleRepository;
    private final AsesorRepository asesorRepository;

    public OperacionService(OperacionRepository operacionRepository,
                            VisitasRepository visitasRepository,
                            ClienteRepository clienteRepository,
                            InmuebleRepository inmuebleRepository,
                            AsesorRepository asesorRepository) {
        this.operacionRepository = operacionRepository;
        this.visitasRepository = visitasRepository;
        this.clienteRepository = clienteRepository;
        this.inmuebleRepository = inmuebleRepository;
        this.asesorRepository = asesorRepository;
    }

    public Arriendo registerRental(Arriendo arriendo) {
        if (arriendo == null) {
            throw new IllegalArgumentException("La operación de arriendo no puede ser nula");
        }
        if (arriendo.getEstado() == null) {
            arriendo.setEstado(EstadoOperacion.EN_PROCESO);
        }
        operacionRepository.save(arriendo);
        return arriendo;
    }

    public Venta registerSale(Venta venta) {
        if (venta == null) {
            throw new IllegalArgumentException("La operación de venta no puede ser nula");
        }
        if (venta.getEstado() == null) {
            venta.setEstado(EstadoOperacion.EN_PROCESO);
        }
        operacionRepository.save(venta);
        return venta;
    }

    /**
     * Helper usado por controladores para registrar una venta completa a partir de ids y valores.
     */
    public void registrarVentaCompleta(String inmuebleCodigo, String clienteId, String asesorId, double valorAcordado, double comision) {
        if (inmuebleCodigo == null || inmuebleCodigo.isBlank()) {
            throw new IllegalArgumentException("El código del inmueble es obligatorio");
        }
        if (clienteId == null || clienteId.isBlank()) {
            throw new IllegalArgumentException("El id del cliente comprador es obligatorio");
        }

        Inmueble inmueble = inmuebleRepository.findByCodigo(inmuebleCodigo);
        if (inmueble == null) {
            throw new IllegalArgumentException("No se encontró un inmueble con el código: " + inmuebleCodigo);
        }

        Cliente comprador = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró un cliente con el id: " + clienteId));

        Asesor asesor = null;
        if (asesorId != null && !asesorId.isBlank()) {
            asesor = asesorRepository.findById(asesorId)
                    .orElse(null);
        }
        if (asesor == null) {
            asesor = inmueble.getAsesor();
        }

        Venta venta = Venta.builder()
                .codigo("VENTA-" + inmuebleCodigo + "-" + System.currentTimeMillis())
                .inmueble(inmueble)
                .cliente(comprador)
                .asesor(asesor)
                .fecha(LocalDateTime.now())
                .valorAcordado(valorAcordado)
                .comision(comision)
                .estado(EstadoOperacion.COMPLETADA)
                .build();

        operacionRepository.save(venta);
    }

    public Renovacion registerRenewal(Renovacion renovacion) {
        if (renovacion == null) {
            throw new IllegalArgumentException("La renovación no puede ser nula");
        }
        if (renovacion.getEstado() == null) {
            renovacion.setEstado(EstadoOperacion.EN_PROCESO);
        }
        operacionRepository.save(renovacion);
        return renovacion;
    }

    public Cancelacion registerTermination(Cancelacion cancelacion) {
        if (cancelacion == null) {
            throw new IllegalArgumentException("La cancelación no puede ser nula");
        }
        if (cancelacion.getEstado() == null) {
            cancelacion.setEstado(EstadoOperacion.CANCELADA);
        }
        operacionRepository.save(cancelacion);
        return cancelacion;
    }

    public Optional<Operacion> consultarOperacion(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return Optional.empty();
        }
        return operacionRepository.findByCodigo(codigo);
    }

    public DynamicArrayList<Operacion> listarOperacionesCliente(String clienteId) {
        if (clienteId == null || clienteId.isBlank()) {
            return new DynamicArrayList<>();
        }
        return operacionRepository.findByIdCliente(clienteId);
    }

    /**
     * Obtener ventas (propiedades adquiridas) por cliente.
     */
    public DynamicArrayList<Venta> obtenerPropiedadesAdquiridasCliente(String clienteId) {
        DynamicArrayList<Venta> resultado = new DynamicArrayList<>();
        if (clienteId == null || clienteId.isBlank()) {
            return resultado;
        }
        DynamicArrayList<Venta> ventas = operacionRepository.findVentas();
        for (int i = 0; i < ventas.size(); i++) {
            Venta v = ventas.get(i);
            if (v.getCliente() != null && clienteId.equals(v.getCliente().getId())) {
                resultado.add(v);
            }
        }
        return resultado;
    }

    public DynamicArrayList<Operacion> consultarOperacionesPorZona(Zona zona) {
        DynamicArrayList<Operacion> resultado = new DynamicArrayList<>();
        if (zona == null) {
            return resultado;
        }
        DynamicArrayList<Operacion> operaciones = operacionRepository.findAll();
        for (int i = 0; i < operaciones.size(); i++) {
            Operacion operacion = operaciones.get(i);
            if (operacion.getInmueble() != null
                    && operacion.getInmueble().getBarrio() != null
                    && zona.equals(operacion.getInmueble().getBarrio().getZona())) {
                resultado.add(operacion);
            }
        }
        return resultado;
    }

    public DynamicArrayList<Operacion> consultarOperacionesPorPrecio(double min, double max) {
        DynamicArrayList<Operacion> resultado = new DynamicArrayList<>();
        if (min > max) {
            return resultado;
        }
        DynamicArrayList<Operacion> operaciones = operacionRepository.findAll();
        for (int i = 0; i < operaciones.size(); i++) {
            Operacion operacion = operaciones.get(i);
            double precio = operacion.getInmueble() != null ? operacion.getInmueble().getPrecio() : operacion.getValorAcordado();
            if (precio >= min && precio <= max) {
                resultado.add(operacion);
            }
        }
        return resultado;
    }

    public DynamicArrayList<Visita> consultarVisitasPorZona(Zona zona) {
        DynamicArrayList<Visita> resultado = new DynamicArrayList<>();
        if (zona == null) {
            return resultado;
        }
        DynamicArrayList<Visita> visitas = visitasRepository.findAll();
        for (int i = 0; i < visitas.size(); i++) {
            Visita visita = visitas.get(i);
            if (visita.getInmueble() != null
                    && visita.getInmueble().getBarrio() != null
                    && zona.equals(visita.getInmueble().getBarrio().getZona())) {
                resultado.add(visita);
            }
        }
        return resultado;
    }

    public DynamicArrayList<Operacion> consultarOperacionesCerradas() {
        return operacionRepository.findByEstado(EstadoOperacion.COMPLETADA);
    }

    public DynamicArrayList<Operacion> consultarOperacionesCerradasPorZona(Zona zona) {
        DynamicArrayList<Operacion> resultado = new DynamicArrayList<>();
        if (zona == null) {
            return resultado;
        }
        DynamicArrayList<Operacion> operaciones = consultarOperacionesPorZona(zona);
        for (int i = 0; i < operaciones.size(); i++) {
            if (operaciones.get(i).getEstado() == EstadoOperacion.COMPLETADA) {
                resultado.add(operaciones.get(i));
            }
        }
        return resultado;
    }
}
