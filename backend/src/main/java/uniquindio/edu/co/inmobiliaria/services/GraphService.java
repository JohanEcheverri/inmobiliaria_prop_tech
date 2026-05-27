package uniquindio.edu.co.inmobiliaria.services;

import org.springframework.stereotype.Service;
import uniquindio.edu.co.inmobiliaria.alerts.AlertaService;
import uniquindio.edu.co.inmobiliaria.models.entities.Cliente;
import uniquindio.edu.co.inmobiliaria.models.entities.EventoHistorial;
import uniquindio.edu.co.inmobiliaria.models.entities.Inmueble;
import uniquindio.edu.co.inmobiliaria.models.entities.Operacion;
import uniquindio.edu.co.inmobiliaria.models.entities.Visita;
import uniquindio.edu.co.inmobiliaria.models.enums.PrioridadAlerta;
import uniquindio.edu.co.inmobiliaria.models.enums.TipoAlerta;
import uniquindio.edu.co.inmobiliaria.models.enums.TipoEventoHistorial;
import uniquindio.edu.co.inmobiliaria.models.enums.Zona;
import uniquindio.edu.co.inmobiliaria.repositories.ClienteRepository;
import uniquindio.edu.co.inmobiliaria.repositories.EventoHistorialRepository;
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
/**
 * Servicio especializado en el análisis de datos mediante estructuras de grafos.
 * Permite descubrir patrones de movilidad comercial, relaciones ocultas entre clientes e inmuebles,
 * rutas de interacción y detectar propiedades similares para el sistema de recomendaciones y alertas.
 */
public class GraphService {

    private static final int MIN_CLIENTES_COMPARTIDOS = 2;
    private static final int TRANSICIONES_ZONA_ALERTA = 3;

    private final ClienteRepository clienteRepository;
    private final InmuebleRepository inmuebleRepository;
    private final VisitasRepository visitasRepository;
    private final OperacionRepository operacionRepository;
    private final EventoHistorialRepository eventoHistorialRepository;
    private final AlertaService alertaService;

    public GraphService(
            ClienteRepository clienteRepository,
            InmuebleRepository inmuebleRepository,
            VisitasRepository visitasRepository,
            OperacionRepository operacionRepository,
            EventoHistorialRepository eventoHistorialRepository,
            AlertaService alertaService) {
        this.clienteRepository = clienteRepository;
        this.inmuebleRepository = inmuebleRepository;
        this.visitasRepository = visitasRepository;
        this.operacionRepository = operacionRepository;
        this.eventoHistorialRepository = eventoHistorialRepository;
        this.alertaService = alertaService;
    }

    /**
     * Construye un grafo representando la movilidad comercial entre clientes, inmuebles,
     * zonas y operaciones a partir de datos persistidos. Las aristas contienen pesos que
     * representan la intensidad de la relación (visitas, consultas, operaciones).
     *
     * @return grafo no dirigido con vértices y aristas listos para análisis y consulta
     */
    public Graph<GraphVertex> construirGrafoDeMovilidadComercial() {
        Graph<GraphVertex> grafo = new Graph<>(false);
        DynamicArrayList<Cliente> clientes = clienteRepository.findAll();
        DynamicArrayList<Inmueble> inmuebles = inmuebleRepository.findAll();
        DynamicArrayList<Operacion> operaciones = operacionRepository.findAll();

        for (int i = 0; i < clientes.size(); i++) {
            Cliente cliente = clientes.get(i);
            grafo.addVertex(GraphVertex.ofCliente(cliente));
        }

        for (int i = 0; i < inmuebles.size(); i++) {
            Inmueble inmueble = inmuebles.get(i);
            GraphVertex inmuebleVertice = GraphVertex.ofInmueble(inmueble);
            grafo.addVertex(inmuebleVertice);
            Zona zona = inmueble.getBarrio() != null ? inmueble.getBarrio().getZona() : null;
            if (zona != null) {
                GraphVertex zonaVertice = GraphVertex.ofZona(zona);
                grafo.addVertex(zonaVertice);
                addOrUpdateEdge(grafo, inmuebleVertice, zonaVertice, 1.0);
            }
        }

        for (int i = 0; i < operaciones.size(); i++) {
            Operacion operacion = operaciones.get(i);
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

        DynamicArrayList<Visita> visitas = visitasRepository.findAll();
        for (int i = 0; i < visitas.size(); i++) {
            Visita visita = visitas.get(i);
            if (visita.getCliente() == null || visita.getInmueble() == null) {
                continue;
            }
            GraphVertex clienteVertice = GraphVertex.ofCliente(visita.getCliente());
            GraphVertex inmuebleVertice = GraphVertex.ofInmueble(visita.getInmueble());
            addOrUpdateEdge(grafo, clienteVertice, inmuebleVertice, 1.5);

            Zona zona = visita.getInmueble().getBarrio() != null
                    ? visita.getInmueble().getBarrio().getZona()
                    : null;
            if (zona != null) {
                addOrUpdateEdge(grafo, clienteVertice, GraphVertex.ofZona(zona), 1.0);
            }
        }

        DynamicArrayList<EventoHistorial> eventos = eventoHistorialRepository.findAll();
        for (int i = 0; i < eventos.size(); i++) {
            EventoHistorial evento = eventos.get(i);
            if (evento.getCliente() == null || evento.getInmueble() == null) {
                continue;
            }
            GraphVertex clienteVertice = GraphVertex.ofCliente(evento.getCliente());
            GraphVertex inmuebleVertice = GraphVertex.ofInmueble(evento.getInmueble());
            double peso = calcularPesoPorEvento(evento.getTipoEvento());
            if (peso <= 0) {
                continue;
            }
            addOrUpdateEdge(grafo, clienteVertice, inmuebleVertice, peso);

            Zona zona = evento.getInmueble().getBarrio() != null
                    ? evento.getInmueble().getBarrio().getZona()
                    : null;
            if (zona != null) {
                addOrUpdateEdge(grafo, clienteVertice, GraphVertex.ofZona(zona), 0.8);
            }
        }

        return grafo;
    }

    /**
     * Detecta inmuebles que han sido consultados por múltiples clientes en conjunto
     * y que además cumplen criterios de similitud (tipo, zona, rango de precio).
     * Se usa para identificar propiedades con demanda compartida.
     *
     * @return lista de vértices de inmuebles considerados similares y con consultas compartidas
     */
    public DynamicArrayList<GraphVertex> detectarPropiedadesSimilaresConsultadasPorMultiplesClientes() {
        DynamicArrayList<GraphVertex> similares = new DynamicArrayList<>();
        DynamicArrayList<Inmueble> inmuebles = inmuebleRepository.findAll();
        DynamicArrayList<EventoHistorial> eventos = eventoHistorialRepository.findAll();
        HashTable<String, Inmueble> inmueblesPorCodigo = new HashTable<>();
        for (int i = 0; i < inmuebles.size(); i++) {
            inmueblesPorCodigo.put(inmuebles.get(i).getCodigo(), inmuebles.get(i));
        }

        for (int i = 0; i < inmuebles.size(); i++) {
            Inmueble inmueble = inmuebles.get(i);
            HashTable<String, Integer> visitanteCompartido = new HashTable<>();
            HashTable<String, Boolean> clienteCandidatoContado = new HashTable<>();

            for (int eventoBaseIndex = 0; eventoBaseIndex < eventos.size(); eventoBaseIndex++) {
                EventoHistorial eventoBase = eventos.get(eventoBaseIndex);
                if (!esConsultaDeInmueble(eventoBase, inmueble.getCodigo())) {
                    continue;
                }

                String clienteId = eventoBase.getCliente().getId();
                for (int candidatoIndex = 0; candidatoIndex < eventos.size(); candidatoIndex++) {
                    EventoHistorial eventoCandidato = eventos.get(candidatoIndex);
                    if (!esConsultaValida(eventoCandidato)
                            || !Objects.equals(clienteId, eventoCandidato.getCliente().getId())) {
                        continue;
                    }

                    String codigoCandidato = eventoCandidato.getInmueble().getCodigo();
                    if (Objects.equals(inmueble.getCodigo(), codigoCandidato)) {
                        continue;
                    }

                    String llaveConteo = clienteId + "|" + codigoCandidato;
                    if (clienteCandidatoContado.containsKey(llaveConteo)) {
                        continue;
                    }
                    clienteCandidatoContado.put(llaveConteo, true);

                    int frecuencia = visitanteCompartido.containsKey(codigoCandidato)
                            ? visitanteCompartido.get(codigoCandidato)
                            : 0;
                    visitanteCompartido.put(codigoCandidato, frecuencia + 1);
                }
            }

            HashTable<String, Integer> entradas = visitanteCompartido;
            DynamicArrayList<String> claves = entradas.keys();
            for (int claveIndex = 0; claveIndex < claves.size(); claveIndex++) {
                String otroCodigo = claves.get(claveIndex);
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

    /**
     * Obtiene patrones de movilidad para un cliente concreto, por ejemplo la secuencia
     * de zonas visitadas ordenadas cronológicamente.
     *
     * @param clienteId identificador del cliente
     * @return lista dinámica de nombres de zona representando el patrón detectado
     */
    public DynamicArrayList<String> obtenerPatronesDeMovilidadComercial(String clienteId) {
        List<Visita> visitas = new ArrayList<>();
        for (int i = 0; i < visitasRepository.findByClienteId(clienteId).size(); i++) {
            visitas.add(visitasRepository.findByClienteId(clienteId).get(i));
        }
        visitas.sort(Comparator.comparing(Visita::getFecha).thenComparing(Visita::getHora));

        DynamicArrayList<String> zonas = new DynamicArrayList<>();
        Zona anterior = null;
        for (int i = 0; i < visitas.size(); i++) {
            Visita visita = visitas.get(i);
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

    /**
     * Analiza el grafo y retorna las zonas (vértices tipo ZONA) que tienen conexiones
     * activas con clientes u operaciones — útil para identificar zonas con tránsito comercial.
     *
     * @return lista de vértices ZONA con conexiones
     */
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

    /**
     * Consulta las relaciones alcanzables desde un cliente en el grafo (inmuebles, zonas, operaciones).
     * Realiza un recorrido BFS desde el vértice del cliente y filtra sólo los tipos relevantes.
     *
     * @param clienteId identificador del cliente
     * @return lista dinámica de vértices relacionados con el cliente
     */
    public DynamicArrayList<GraphVertex> consultarRelacionesClienteInmuebles(String clienteId) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado: " + clienteId));
        Graph<GraphVertex> grafo = construirGrafoDeMovilidadComercial();
        GraphVertex inicio = GraphVertex.ofCliente(cliente);
        if (!grafo.containsVertex(inicio)) {
            return new DynamicArrayList<>();
        }

        DynamicArrayList<GraphVertex> recorrido = grafo.bfs(inicio);
        DynamicArrayList<GraphVertex> relaciones = new DynamicArrayList<>();
        for (int i = 0; i < recorrido.size(); i++) {
            GraphVertex vertice = recorrido.get(i);
            if (vertice.equals(inicio)) {
                continue;
            }
            if (vertice.getType() == GraphVertex.Type.INMUEBLE
                    || vertice.getType() == GraphVertex.Type.ZONA
                    || vertice.getType() == GraphVertex.Type.OPERACION) {
                relaciones.add(vertice);
            }
        }
        return relaciones;
    }

    /**
     * Encuentra la ruta más corta en el grafo desde un cliente hasta un inmueble.
     * Valida existencia de los vértices y devuelve una lista vacía si no es posible.
     *
     * @param clienteId identificador del cliente origen
     * @param codigoInmueble código del inmueble destino
     * @return ruta como lista dinámica de vértices o lista vacía
     */
    public DynamicArrayList<GraphVertex> consultarRutaClienteAInmueble(String clienteId, String codigoInmueble) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado: " + clienteId));
        if (codigoInmueble == null || codigoInmueble.isBlank()) {
            return new DynamicArrayList<>();
        }
        Inmueble inmueble = inmuebleRepository.findByCodigo(codigoInmueble);
        if (inmueble == null) {
            return new DynamicArrayList<>();
        }
        Graph<GraphVertex> grafo = construirGrafoDeMovilidadComercial();
        GraphVertex inicio = GraphVertex.ofCliente(cliente);
        GraphVertex destino = GraphVertex.ofInmueble(inmueble);
        if (!grafo.containsVertex(inicio) || !grafo.containsVertex(destino)) {
            return new DynamicArrayList<>();
        }
        return grafo.shortestPath(inicio, destino);
    }

    /**
     * Calcula el peso que aporta un tipo de evento del historial a la relación cliente-inmueble.
     * Los pesos determinan la fuerza de la arista en el grafo.
     *
     * @param tipoEvento tipo de evento de historial
     * @return peso asociado (0 o positivo)
     */
    private double calcularPesoPorEvento(TipoEventoHistorial tipoEvento) {
        if (tipoEvento == null) {
            return 0.0;
        }
        return switch (tipoEvento) {
            case FAVORITO -> 3.0;
            case GUARDADO -> 2.0;
            case VISITA -> 2.5;
            case NEGOCIANDO -> 2.8;
            case CONSULTA -> 1.2;
            case DESCARTADO -> 0.0;
            case DESMARCADO -> 1.0;
        };
    }

    /**
     * Añade una arista al grafo entre dos vértices o actualiza el peso si ya existe.
     * Garantiza que ambos vértices estén presentes en el grafo antes de operar.
     *
     * @param grafo grafo donde se añade/actualiza la arista
     * @param from vértice origen
     * @param to vértice destino
     * @param weight peso a sumar o establecer
     */
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

    /**
     * Determina si dos inmuebles son razonablemente similares en tipo, zona y rango de precio.
     * Se usan reglas sencillas: misma tipología y zona, o diferencia de precio relativa <= 25%.
     *
     * @param a inmueble A
     * @param b inmueble B
     * @return true si se consideran similares, false en caso contrario
     */
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

    /**
     * Verifica si un evento de historial es una consulta válida y corresponde
     * al código de inmueble indicado.
     *
     * @param evento evento de historial
     * @param codigoInmueble código de inmueble a comparar
     * @return true si el evento es una consulta sobre el inmueble dado
     */
    private boolean esConsultaDeInmueble(EventoHistorial evento, String codigoInmueble) {
        return esConsultaValida(evento)
                && Objects.equals(evento.getInmueble().getCodigo(), codigoInmueble);
    }

    /**
     * Comprueba si un EventoHistorial representa una consulta válida (tipo CONSULTA)
     * y tiene cliente e inmueble correctamente referenciados.
     *
     * @param evento evento a validar
     * @return true si es una consulta válida, false en otro caso
     */
    private boolean esConsultaValida(EventoHistorial evento) {
        return evento != null
                && evento.getTipoEvento() == TipoEventoHistorial.CONSULTA
                && evento.getCliente() != null
                && evento.getCliente().getId() != null
                && evento.getInmueble() != null
                && evento.getInmueble().getCodigo() != null;
    }

    /**
     * Genera alertas automáticas de negocio para inmuebles que muestran alta demanda
     * (según la detección de propiedades similares consultadas por múltiples clientes).
     * Crea una alerta para cada inmueble detectado.
     */
    public void generarAlertasDeMovilidadComercial() {
        DynamicArrayList<GraphVertex> similares = detectarPropiedadesSimilaresConsultadasPorMultiplesClientes();
        for (int i = 0; i < similares.size(); i++) {
            GraphVertex inmuebleVertice = similares.get(i);
            alertaService.generarAlerta(
                    TipoAlerta.PROPIEDAD_ALTA_DEMANDA,
                    PrioridadAlerta.MEDIA,
                    "Propiedades similares con demanda compartida",
                    "El inmueble " + inmuebleVertice.getId() + " está conectado por consultas de múltiples clientes.",
                    inmuebleVertice.getId()
            );
        }
    }
}
