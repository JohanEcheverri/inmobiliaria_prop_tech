package uniquindio.edu.co.inmobiliaria.services;

import org.springframework.stereotype.Service;
import uniquindio.edu.co.inmobiliaria.models.entities.Cliente;
import uniquindio.edu.co.inmobiliaria.models.entities.Inmueble;
import uniquindio.edu.co.inmobiliaria.models.entities.Operacion;
import uniquindio.edu.co.inmobiliaria.models.entities.Visita;
import uniquindio.edu.co.inmobiliaria.models.enums.PrioridadAlerta;
import uniquindio.edu.co.inmobiliaria.models.enums.TipoAlerta;
import uniquindio.edu.co.inmobiliaria.models.enums.Zona;
import uniquindio.edu.co.inmobiliaria.repositories.ClienteRepository;
import uniquindio.edu.co.inmobiliaria.repositories.InmuebleRepository;
import uniquindio.edu.co.inmobiliaria.repositories.OperacionRepository;
import uniquindio.edu.co.inmobiliaria.repositories.VisitasRepository;
import uniquindio.edu.co.inmobiliaria.structures.DynamicArrayList;
import uniquindio.edu.co.inmobiliaria.structures.Graph;
import uniquindio.edu.co.inmobiliaria.structures.GraphVertex;
import uniquindio.edu.co.inmobiliaria.structures.HashTable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class MovilidadComercialGraphService {

    private static final int MIN_CLIENTES_COMPARTIDOS = 2;
    private static final int TRANSICIONES_ZONA_ALERTA = 3;

    private final ClienteRepository clienteRepository;
    private final InmuebleRepository inmuebleRepository;
    private final VisitasRepository visitasRepository;
    private final OperacionRepository operacionRepository;
    private final AlertaService alertaService;

    public MovilidadComercialGraphService(
            ClienteRepository clienteRepository,
            InmuebleRepository inmuebleRepository,
            VisitasRepository visitasRepository,
            OperacionRepository operacionRepository,
            AlertaService alertaService) {
        this.clienteRepository = clienteRepository;
        this.inmuebleRepository = inmuebleRepository;
        this.visitasRepository = visitasRepository;
        this.operacionRepository = operacionRepository;
        this.alertaService = alertaService;
    }

    public Graph<GraphVertex> construirGrafoDeMovilidadComercial() {
        Graph<GraphVertex> grafo = new Graph<>(false);
        DynamicArrayList<Cliente> clientes = clienteRepository.findAll();
        DynamicArrayList<Inmueble> inmuebles = inmuebleRepository.findAll();
        DynamicArrayList<Operacion> operaciones = operacionRepository.findAll();

        for (Cliente cliente : clientes) {
            grafo.addVertex(GraphVertex.ofCliente(cliente));
        }

        for (Inmueble inmueble : inmuebles) {
            GraphVertex inmuebleVertice = GraphVertex.ofInmueble(inmueble);
            grafo.addVertex(inmuebleVertice);
            Zona zona = inmueble.getBarrio() != null ? inmueble.getBarrio().getZona() : null;
            if (zona != null) {
                GraphVertex zonaVertice = GraphVertex.ofZona(zona);
                grafo.addVertex(zonaVertice);
                addOrUpdateEdge(grafo, inmuebleVertice, zonaVertice, 1.0);
            }
        }

        for (Operacion operacion : operaciones) {
            GraphVertex operacionVertice = GraphVertex.ofOperacion(operacion);
            grafo.addVertex(operacionVertice);

            if (operacion.getCliente() != null) {
                addOrUpdateEdge(grafo,
                        GraphVertex.ofOperacion(operacion),
                        GraphVertex.ofCliente(operacion.getCliente()),
                        1.0);
            }
            if (operacion.getInmueble() != null) {
                addOrUpdateEdge(grafo,
                        GraphVertex.ofOperacion(operacion),
                        GraphVertex.ofInmueble(operacion.getInmueble()),
                        1.0);
            }
        }

        for (Visita visita : visitasRepository.findAll()) {
            if (visita.getCliente() == null || visita.getInmueble() == null) {
                continue;
            }
            GraphVertex clienteVertice = GraphVertex.ofCliente(visita.getCliente());
            GraphVertex inmuebleVertice = GraphVertex.ofInmueble(visita.getInmueble());
            addOrUpdateEdge(grafo, clienteVertice, inmuebleVertice, 1.0);

            Zona zona = visita.getInmueble().getBarrio() != null
                    ? visita.getInmueble().getBarrio().getZona()
                    : null;
            if (zona != null) {
                addOrUpdateEdge(grafo, clienteVertice, GraphVertex.ofZona(zona), 1.0);
            }
        }

        return grafo;
    }

    public DynamicArrayList<GraphVertex> detectarPropiedadesSimilaresConsultadasPorMultiplesClientes() {
        Graph<GraphVertex> grafo = construirGrafoDeMovilidadComercial();
        DynamicArrayList<GraphVertex> similares = new DynamicArrayList<>();
        DynamicArrayList<Inmueble> inmuebles = inmuebleRepository.findAll();
        HashTable<String, Inmueble> inmueblesPorCodigo = new HashTable<>();
        for (int i = 0; i < inmuebles.size(); i++) {
            inmueblesPorCodigo.put(inmuebles.get(i).getCodigo(), inmuebles.get(i));
        }

        for (Inmueble inmueble : inmuebles) {
            GraphVertex inmuebleVertice = GraphVertex.ofInmueble(inmueble);
            HashTable<String, Integer> visitanteCompartido = new HashTable<>();

            for (int i = 0; i < grafo.getNeighbors(inmuebleVertice).size(); i++) {
                Graph.Edge<GraphVertex> arista = grafo.getNeighbors(inmuebleVertice).get(i);
                if (arista.getTarget().getType() != GraphVertex.Type.CLIENTE) {
                    continue;
                }
                GraphVertex clienteVertice = arista.getTarget();
                DynamicArrayList<Graph.Edge<GraphVertex>> vecinosCliente = grafo.getNeighbors(clienteVertice);
                for (int j = 0; j < vecinosCliente.size(); j++) {
                    Graph.Edge<GraphVertex> clienteEdge = vecinosCliente.get(j);
                    GraphVertex candidato = clienteEdge.getTarget();
                    if (candidato.equals(inmuebleVertice) || candidato.getType() != GraphVertex.Type.INMUEBLE) {
                        continue;
                    }
                    int frecuencia = visitanteCompartido.containsKey(candidato.getId())
                            ? visitanteCompartido.get(candidato.getId())
                            : 0;
                    visitanteCompartido.put(candidato.getId(), frecuencia + 1);
                }
            }

            HashTable<String, Integer> entradas = visitanteCompartido;
            DynamicArrayList<String> claves = entradas.keys();
            for (int i = 0; i < claves.size(); i++) {
                String otroCodigo = claves.get(i);
                int cuenta = entradas.get(otroCodigo);
                if (cuenta >= MIN_CLIENTES_COMPARTIDOS) {
                    Inmueble otroInmueble = inmueblesPorCodigo.get(otroCodigo);
                    if (otroInmueble != null && inmueblesSimilares(inmueble, otroInmueble)) {
                        GraphVertex verticeSimilar = GraphVertex.ofInmueble(otroInmueble);
                        if (!similares.contains(verticeSimilar)) {
                            similares.add(verticeSimilar);
                        }
                    }
                }
            }
        }
        return similares;
    }

    public DynamicArrayList<String> obtenerPatronesDeMovilidadComercial(String clienteId) {
        List<Visita> visitas = new ArrayList<>();
        for (int i = 0; i < visitasRepository.findByClienteId(clienteId).size(); i++) {
            visitas.add(visitasRepository.findByClienteId(clienteId).get(i));
        }
        visitas.sort(Comparator.comparing(Visita::getFecha).thenComparing(Visita::getHora));

        DynamicArrayList<String> zonas = new DynamicArrayList<>();
        Zona anterior = null;
        for (Visita visita : visitas) {
            if (visita.getInmueble() == null || visita.getInmueble().getBarrio() == null) {
                continue;
            }
            Zona actual = visita.getInmueble().getBarrio().getZona();
            if (actual != null && !actual.equals(anterior)) {
                zonas.add(actual.name());
                anterior = actual;
            }
        }
        return zonas;
    }

    public DynamicArrayList<GraphVertex> analizarConexionesEntreZonasClientesYOperaciones() {
        Graph<GraphVertex> grafo = construirGrafoDeMovilidadComercial();
        DynamicArrayList<GraphVertex> zonasConConexion = new DynamicArrayList<>();
        DynamicArrayList<GraphVertex> vertices = grafo.getVertices();
        for (int i = 0; i < vertices.size(); i++) {
            GraphVertex vertice = vertices.get(i);
            if (vertice.getType() != GraphVertex.Type.ZONA) {
                continue;
            }
            if (!grafo.getNeighbors(vertice).isEmpty()) {
                zonasConConexion.add(vertice);
            }
        }
        return zonasConConexion;
    }

    private void addOrUpdateEdge(Graph<GraphVertex> grafo,
                                 GraphVertex from,
                                 GraphVertex to,
                                 double weight) {
        if (!grafo.containsVertex(from)) {
            grafo.addVertex(from);
        }
        if (!grafo.containsVertex(to)) {
            grafo.addVertex(to);
        }

        DynamicArrayList<Graph.Edge<GraphVertex>> edges = grafo.getNeighbors(from);
        for (int i = 0; i < edges.size(); i++) {
            Graph.Edge<GraphVertex> edge = edges.get(i);
            if (edge.getTarget().equals(to)) {
                double nuevoPeso = edge.getWeight() + weight;
                grafo.removeEdge(from, to);
                grafo.addEdge(from, to, nuevoPeso);
                return;
            }
        }
        grafo.addEdge(from, to, weight);
    }

    private boolean inmueblesSimilares(Inmueble a, Inmueble b) {
        if (a == null || b == null) {
            return false;
        }
        if (a.getTipoInmueble() == b.getTipoInmueble() && a.getBarrio() != null && b.getBarrio() != null
                && Objects.equals(a.getBarrio().getZona(), b.getBarrio().getZona())) {
            return true;
        }
        if (a.getPrecio() <= 0 || b.getPrecio() <= 0) {
            return false;
        }
        double diferenciaRelativa = Math.abs(a.getPrecio() - b.getPrecio()) / Math.max(a.getPrecio(), b.getPrecio());
        return diferenciaRelativa <= 0.25;
    }

    public void generarAlertasDeMovilidadComercial() {
        DynamicArrayList<GraphVertex> similares = detectarPropiedadesSimilaresConsultadasPorMultiplesClientes();
        for (int i = 0; i < similares.size(); i++) {
            GraphVertex inmuebleVertice = similares.get(i);
            alertaService.generarAlerta(
                    TipoAlerta.PROPIEDAD_ALTA_DEMANDA,
                    PrioridadAlerta.MEDIA,
                    "Propiedades similares con demanda compartida",
                    "El inmueble " + inmuebleVertice.getId() + " está conectado con consultoras de múltiples clientes.",
                    inmuebleVertice.getId()
            );
        }
    }
}
