package uniquindio.edu.co.inmobiliaria.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uniquindio.edu.co.inmobiliaria.services.GraphService;
import uniquindio.edu.co.inmobiliaria.structures.DynamicArrayList;
import uniquindio.edu.co.inmobiliaria.structures.Graph;
import uniquindio.edu.co.inmobiliaria.structures.GraphVertex;

import java.util.ArrayList;
import java.util.List;

@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://127.0.0.1:5173", "http://127.0.0.1:5174", "http://localhost:3000"})
@RestController
@RequestMapping("/api/grafo")
public class GraphController {

    private final GraphService graphService;

    public GraphController(GraphService graphService) {
        this.graphService = graphService;
    }

    // DTOs para estructurar la respuesta JSON de forma limpia
    public static class NodeDTO {
        public String id;
        public String type;
        public String label;
        public String summary;

        public NodeDTO(String id, String type, String label, String summary) {
            this.id = id;
            this.type = type;
            this.label = label;
            this.summary = summary;
        }
    }

    public static class EdgeDTO {
        public String id;
        public String source;
        public String target;
        public double weight;

        public EdgeDTO(String id, String source, String target, double weight) {
            this.id = id;
            this.source = source;
            this.target = target;
            this.weight = weight;
        }
    }

    public static class GraphDTO {
        public List<NodeDTO> nodes;
        public List<EdgeDTO> edges;

        public GraphDTO(List<NodeDTO> nodes, List<EdgeDTO> edges) {
            this.nodes = nodes;
            this.edges = edges;
        }
    }

    private GraphDTO mapToGraphDTO(Graph<GraphVertex> graph) {
        List<NodeDTO> nodes = new ArrayList<>();
        List<EdgeDTO> edges = new ArrayList<>();
        DynamicArrayList<GraphVertex> vertices = graph.getVertices();

        for (int i = 0; i < vertices.size(); i++) {
            GraphVertex v = vertices.get(i);
            String nodeId = v.getType().name() + "-" + v.getId();
            nodes.add(new NodeDTO(nodeId, v.getType().name(), v.getLabel(), v.getSummary()));

            try {
                DynamicArrayList<Graph.Edge<GraphVertex>> neighbors = graph.getNeighbors(v);
                for (int j = 0; j < neighbors.size(); j++) {
                    Graph.Edge<GraphVertex> edge = neighbors.get(j);
                    GraphVertex target = edge.getTarget();
                    String targetId = target.getType().name() + "-" + target.getId();

                    // Para grafos no dirigidos, evitamos duplicar aristas comparando los IDs
                    if (nodeId.compareTo(targetId) < 0) {
                        String edgeId = nodeId + "_" + targetId;
                        edges.add(new EdgeDTO(edgeId, nodeId, targetId, edge.getWeight()));
                    }
                }
            } catch (Exception e) {
                // Si algún vértice no tiene vecinos registrados aún
            }
        }

        return new GraphDTO(nodes, edges);
    }

    @GetMapping("/movilidad")
    public ResponseEntity<GraphDTO> getMovilidadGraph() {
        Graph<GraphVertex> graph = graphService.construirGrafoDeMovilidadComercial();
        return ResponseEntity.ok(mapToGraphDTO(graph));
    }

    @GetMapping("/similares")
    public ResponseEntity<List<NodeDTO>> getSimilares() {
        DynamicArrayList<GraphVertex> similares = graphService.detectarPropiedadesSimilaresConsultadasPorMultiplesClientes();
        List<NodeDTO> nodes = new ArrayList<>();
        for (int i = 0; i < similares.size(); i++) {
            GraphVertex v = similares.get(i);
            nodes.add(new NodeDTO(v.getType().name() + "-" + v.getId(), v.getType().name(), v.getLabel(), v.getSummary()));
        }
        return ResponseEntity.ok(nodes);
    }

    @GetMapping("/relaciones/{clienteId}")
    public ResponseEntity<List<NodeDTO>> getRelacionesCliente(@PathVariable String clienteId) {
        try {
            DynamicArrayList<GraphVertex> relaciones = graphService.consultarRelacionesClienteInmuebles(clienteId);
            List<NodeDTO> nodes = new ArrayList<>();
            for (int i = 0; i < relaciones.size(); i++) {
                GraphVertex v = relaciones.get(i);
                nodes.add(new NodeDTO(v.getType().name() + "-" + v.getId(), v.getType().name(), v.getLabel(), v.getSummary()));
            }
            return ResponseEntity.ok(nodes);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/ruta")
    public ResponseEntity<List<NodeDTO>> getRutaClienteAInmueble(
            @RequestParam String clienteId,
            @RequestParam String codigoInmueble) {
        try {
            DynamicArrayList<GraphVertex> ruta = graphService.consultarRutaClienteAInmueble(clienteId, codigoInmueble);
            List<NodeDTO> nodes = new ArrayList<>();
            for (int i = 0; i < ruta.size(); i++) {
                GraphVertex v = ruta.get(i);
                nodes.add(new NodeDTO(v.getType().name() + "-" + v.getId(), v.getType().name(), v.getLabel(), v.getSummary()));
            }
            return ResponseEntity.ok(nodes);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/patrones/{clienteId}")
    public ResponseEntity<List<String>> getPatronesMovilidad(@PathVariable String clienteId) {
        DynamicArrayList<String> patrones = graphService.obtenerPatronesDeMovilidadComercial(clienteId);
        List<String> list = new ArrayList<>();
        for (int i = 0; i < patrones.size(); i++) {
            list.add(patrones.get(i));
        }
        return ResponseEntity.ok(list);
    }
}
