import React, { useState, useEffect, useRef, useMemo } from 'react';
import { apiUrl } from '../api';
import './RelationsGraphVisualizer.css';

const NODE_COLORS = {
    CLIENTE: '#4fc3f7', // Cyan
    INMUEBLE: '#ba68c8', // Violeta
    ZONA: '#81c784', // Verde
    OPERACION: '#ffb74d' // Naranja
};

export default function RelationsGraphVisualizer() {
    const [graphData, setGraphData] = useState({ nodes: [], edges: [] });
    const [originalGraphData, setOriginalGraphData] = useState({ nodes: [], edges: [] });
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    // Listado de clientes e inmuebles para filtros y búsquedas de ruta
    const [clientes, setClientes] = useState([]);
    const [inmuebles, setInmuebles] = useState([]);
    const [selectedClienteId, setSelectedClienteId] = useState('');
    const [selectedInmuebleId, setSelectedInmuebleId] = useState('');
    const [shortestPathIds, setShortestPathIds] = useState(new Set());
    const [calculatingPath, setCalculatingPath] = useState(false);

    // Filtros de visibilidad
    const [visibleTypes, setVisibleTypes] = useState({
        CLIENTE: true,
        INMUEBLE: true,
        ZONA: true,
        OPERACION: true
    });

    // Control de selección e información
    const [hoveredNode, setHoveredNode] = useState(null);
    const [selectedNode, setSelectedNode] = useState(null);

    // Estado del canvas interactivo
    const [zoom, setZoom] = useState(1);
    const [pan, setPan] = useState({ x: 0, y: 0 });
    const [isPanning, setIsPanning] = useState(false);
    const panStart = useRef({ x: 0, y: 0 });

    // Referencias física y SVG
    const svgRef = useRef(null);
    const nodesRef = useRef([]);
    const edgesRef = useRef([]);
    const [positions, setPositions] = useState({});
    const dragNodeRef = useRef(null);

    // Cargar grafo principal y catálogo de clientes/inmuebles
    const fetchData = async () => {
        setLoading(true);
        setError(null);
        setShortestPathIds(new Set());
        setSelectedNode(null);

        try {
            // Cargar Grafo Completo
            const graphRes = await fetch(apiUrl('/grafo/movilidad'));
            if (!graphRes.ok) throw new Error('Error al obtener el grafo de movilidad');
            const graphJson = await graphRes.json();

            // Cargar Catálogos para desplegables de ruta corta
            const clientesRes = await fetch(apiUrl('/clientes'));
            const inmueblesRes = await fetch(apiUrl('/inmuebles'));
            
            if (clientesRes.ok) {
                const cData = await clientesRes.json();
                setClientes(cData);
            }
            if (inmueblesRes.ok) {
                const iData = await inmueblesRes.json();
                setInmuebles(iData);
            }

            setOriginalGraphData(graphJson);
            applyFilters(graphJson, visibleTypes);
        } catch (err) {
            setError(err.message);
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchData();
    }, []);

    // Aplicar filtros de tipos de nodo
    const applyFilters = (rawGraph, typesFilter) => {
        const filteredNodes = rawGraph.nodes.filter(n => typesFilter[n.type]);
        const nodeIds = new Set(filteredNodes.map(n => n.id));
        const filteredEdges = rawGraph.edges.filter(
            e => nodeIds.has(e.source) && nodeIds.has(e.target)
        );

        setGraphData({ nodes: filteredNodes, edges: filteredEdges });

        // Inicializar posiciones aleatorias/distribuidas
        const newPos = {};
        const centerX = 400;
        const centerY = 300;
        const radius = 220;

        filteredNodes.forEach((node, idx) => {
            // Posicionar inicialmente en círculos concéntricos por tipo
            let angle = (idx / filteredNodes.length) * 2 * Math.PI;
            let currentRadius = radius;

            if (node.type === 'ZONA') currentRadius = 90;
            else if (node.type === 'CLIENTE') currentRadius = 180;
            else if (node.type === 'INMUEBLE') currentRadius = 260;
            else if (node.type === 'OPERACION') currentRadius = 320;

            newPos[node.id] = {
                x: centerX + Math.cos(angle) * currentRadius + (Math.random() - 0.5) * 40,
                y: centerY + Math.sin(angle) * currentRadius + (Math.random() - 0.5) * 40,
                vx: 0,
                vy: 0
            };
        });

        setPositions(newPos);
    };

    // Manejar cambio en filtros de visualización
    const handleFilterToggle = (type) => {
        const updated = { ...visibleTypes, [type]: !visibleTypes[type] };
        setVisibleTypes(updated);
        applyFilters(originalGraphData, updated);
        setShortestPathIds(new Set());
    };

    // Calcular la ruta más corta
    const handleCalcularRuta = async () => {
        if (!selectedClienteId || !selectedInmuebleId) return;
        setCalculatingPath(true);
        setShortestPathIds(new Set());
        setSelectedNode(null);

        try {
            const res = await fetch(
                apiUrl(`/grafo/ruta?clienteId=${selectedClienteId}&codigoInmueble=${selectedInmuebleId}`)
            );
            if (!res.ok) throw new Error('No se pudo encontrar una ruta entre el cliente y el inmueble.');
            const pathNodes = await res.json();
            
            if (pathNodes.length === 0) {
                alert('No existe ninguna conexión indirecta o directa entre el cliente y el inmueble en el grafo actual.');
                return;
            }

            const pathSet = new Set(pathNodes.map(n => n.id));
            setShortestPathIds(pathSet);

            // Centrar la vista en el primer nodo de la ruta
            const firstNodeId = pathNodes[0].id;
            if (positions[firstNodeId]) {
                setPan({
                    x: 400 - positions[firstNodeId].x * zoom,
                    y: 300 - positions[firstNodeId].y * zoom
                });
            }
        } catch (err) {
            alert(err.message);
        } finally {
            setCalculatingPath(false);
        }
    };

    // Limpiar ruta calculada
    const handleLimpiarRuta = () => {
        setShortestPathIds(new Set());
        setSelectedClienteId('');
        setSelectedInmuebleId('');
    };

    // Filtrar relaciones de un cliente específico (BFS)
    const handleFiltroRelacionesCliente = async (clienteId) => {
        if (!clienteId) {
            fetchData();
            return;
        }
        setLoading(true);
        setShortestPathIds(new Set());
        try {
            const res = await fetch(apiUrl(`/grafo/relaciones/${clienteId}`));
            if (!res.ok) throw new Error('Error al consultar relaciones del cliente');
            const relNodes = await res.json();

            // Construir grafo local de ese cliente
            const clienteNodeId = `CLIENTE-${clienteId}`;
            const targetNodeIds = new Set(relNodes.map(n => n.id));
            targetNodeIds.add(clienteNodeId);

            const filteredNodes = originalGraphData.nodes.filter(n => targetNodeIds.has(n.id));
            const filteredEdges = originalGraphData.edges.filter(
                e => targetNodeIds.has(e.source) && targetNodeIds.has(e.target)
            );

            setGraphData({ nodes: filteredNodes, edges: filteredEdges });

            // Reinicializar posiciones para el subset
            const newPos = {};
            filteredNodes.forEach((node, idx) => {
                let angle = (idx / filteredNodes.length) * 2 * Math.PI;
                let rad = node.id === clienteNodeId ? 0 : 180;
                newPos[node.id] = {
                    x: 400 + Math.cos(angle) * rad,
                    y: 300 + Math.sin(angle) * rad,
                    vx: 0,
                    vy: 0
                };
            });
            setPositions(newPos);
        } catch (err) {
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    // Simulación de física simple (Spring Embedder / Force-Directed)
    useEffect(() => {
        if (graphData.nodes.length === 0) return;

        let animationFrameId;
        const width = 800;
        const height = 600;

        const runSimulationStep = () => {
            setPositions(prev => {
                const next = { ...prev };
                const nodeIds = Object.keys(next);
                if (nodeIds.length === 0) return prev;

                // 1. Fuerza de repulsión entre todos los nodos (Coulomb's Law)
                const kRepulsion = 12000;
                for (let i = 0; i < nodeIds.length; i++) {
                    const idA = nodeIds[i];
                    if (dragNodeRef.current === idA) continue; // No aplicar física al arrastrado

                    const nodeA = next[idA];
                    let fx = 0;
                    let fy = 0;

                    for (let j = 0; j < nodeIds.length; j++) {
                        if (i === j) continue;
                        const idB = nodeIds[j];
                        const nodeB = next[idB];

                        const dx = nodeA.x - nodeB.x;
                        const dy = nodeA.y - nodeB.y;
                        const distSq = dx * dx + dy * dy + 0.1;
                        const dist = Math.sqrt(distSq);

                        if (dist < 280) { // Radio de influencia
                            const force = kRepulsion / distSq;
                            fx += (dx / dist) * force;
                            fy += (dy / dist) * force;
                        }
                    }

                    nodeA.vx += fx;
                    nodeA.vy += fy;
                }

                // 2. Fuerza de atracción por aristas (Hooke's Law)
                const kAttraction = 0.04;
                const restLength = 120;

                graphData.edges.forEach(edge => {
                    const nodeA = next[edge.source];
                    const nodeB = next[edge.target];

                    if (!nodeA || !nodeB) return;

                    const dx = nodeB.x - nodeA.x;
                    const dy = nodeB.y - nodeA.y;
                    const dist = Math.sqrt(dx * dx + dy * dy) + 0.1;

                    // Ajustar fuerza según el peso comercial de la arista
                    const currentRestLength = restLength / (edge.weight || 1);
                    const force = kAttraction * (dist - currentRestLength);

                    const fx = (dx / dist) * force;
                    const fy = (dy / dist) * force;

                    if (dragNodeRef.current !== edge.source) {
                        nodeA.vx += fx;
                        nodeA.vy += fy;
                    }
                    if (dragNodeRef.current !== edge.target) {
                        nodeB.vx -= fx;
                        nodeB.vy -= fy;
                    }
                });

                // 3. Gravedad hacia el centro y fricción
                const kGravity = 0.012;
                const friction = 0.82;

                nodeIds.forEach(id => {
                    const node = next[id];
                    if (dragNodeRef.current === id) return;

                    // Fuerza central
                    const dx = 400 - node.x;
                    const dy = 300 - node.y;
                    node.vx += dx * kGravity;
                    node.vy += dy * kGravity;

                    // Aplicar velocidad con amortiguamiento
                    node.x += node.vx;
                    node.y += node.vy;

                    node.vx *= friction;
                    node.vy *= friction;

                    // Límites suaves
                    node.x = Math.max(40, Math.min(width - 40, node.x));
                    node.y = Math.max(40, Math.min(height - 40, node.y));
                });

                return next;
            });

            animationFrameId = requestAnimationFrame(runSimulationStep);
        };

        animationFrameId = requestAnimationFrame(runSimulationStep);
        return () => cancelAnimationFrame(animationFrameId);
    }, [graphData]);

    // Interactividad: Zoom
    const handleZoom = (amount) => {
        setZoom(prev => Math.max(0.3, Math.min(3, prev + amount)));
    };

    // Interactividad: Drag en canvas para Pan
    const handleMouseDown = (e) => {
        if (e.target.tagName === 'svg' || e.target.tagName === 'rect' || e.target.id === 'grid-bg') {
            setIsPanning(true);
            panStart.current = { x: e.clientX - pan.x, y: e.clientY - pan.y };
        }
    };

    const handleMouseMove = (e) => {
        if (isPanning) {
            setPan({
                x: e.clientX - panStart.current.x,
                y: e.clientY - panStart.current.y
            });
        } else if (dragNodeRef.current && positions[dragNodeRef.current]) {
            // Arrastrar Nodo
            const rect = svgRef.current.getBoundingClientRect();
            const rawX = e.clientX - rect.left;
            const rawY = e.clientY - rect.top;

            // Convertir coordenadas del mouse según zoom y pan
            const x = (rawX - pan.x) / zoom;
            const y = (rawY - pan.y) / zoom;

            setPositions(prev => ({
                ...prev,
                [dragNodeRef.current]: {
                    ...prev[dragNodeRef.current],
                    x: Math.max(20, Math.min(780, x)),
                    y: Math.max(20, Math.min(580, y)),
                    vx: 0,
                    vy: 0
                }
            }));
        }
    };

    const handleMouseUp = () => {
        setIsPanning(false);
        dragNodeRef.current = null;
    };

    // Determinar si una arista forma parte de la ruta más corta
    const isEdgeInShortestPath = (edge) => {
        if (shortestPathIds.size === 0) return false;
        return shortestPathIds.has(edge.source) && shortestPathIds.has(edge.target);
    };

    // Obtener aristas conectadas al nodo seleccionado
    const connectedEdges = useMemo(() => {
        if (!selectedNode) return new Set();
        const set = new Set();
        graphData.edges.forEach(e => {
            if (e.source === selectedNode.id || e.target === selectedNode.id) {
                set.add(e.id);
            }
        });
        return set;
    }, [selectedNode, graphData.edges]);

    return (
        <div className="relations-dashboard-container">
            <div className="graph-sidebar-controls">
                <div className="sidebar-control-card">
                    <h4>Filtros de Red</h4>
                    <p className="control-desc">Muestra/oculta elementos según su rol estructural</p>
                    <div className="filter-checkbox-group">
                        {Object.keys(visibleTypes).map(type => (
                            <button
                                key={type}
                                className={`filter-toggle-btn ${visibleTypes[type] ? 'active' : ''}`}
                                onClick={() => handleFilterToggle(type)}
                                style={{
                                    borderLeft: `4px solid ${NODE_COLORS[type]}`
                                }}
                            >
                                <span className="type-color-dot" style={{ backgroundColor: NODE_COLORS[type] }}></span>
                                {type}s
                            </button>
                        ))}
                    </div>
                </div>

                <div className="sidebar-control-card">
                    <h4>Análisis de Ruta Corta</h4>
                    <p className="control-desc">Deduce dependencias o caminos de contacto utilizando Dijkstra</p>
                    
                    <div className="route-select-group">
                        <label>Cliente:</label>
                        <select 
                            value={selectedClienteId} 
                            onChange={(e) => {
                                setSelectedClienteId(e.target.value);
                                handleFiltroRelacionesCliente(e.target.value);
                            }}
                        >
                            <option value="">-- Todos los Clientes --</option>
                            {clientes.map(c => (
                                <option key={c.id} value={c.id}>
                                    {c.nombre} ({c.id})
                                </option>
                            ))}
                        </select>
                    </div>

                    <div className="route-select-group">
                        <label>Inmueble de Interés:</label>
                        <select 
                            value={selectedInmuebleId} 
                            onChange={(e) => setSelectedInmuebleId(e.target.value)}
                        >
                            <option value="">-- Selecciona Inmueble --</option>
                            {inmuebles.map(i => (
                                <option key={i.codigo} value={i.codigo}>
                                    {i.codigo} - {i.barrio} ({i.tipoInmueble})
                                </option>
                            ))}
                        </select>
                    </div>

                    <div className="route-actions">
                        <button 
                            className="btn-action-route-calc" 
                            disabled={!selectedClienteId || !selectedInmuebleId || calculatingPath}
                            onClick={handleCalcularRuta}
                        >
                            {calculatingPath ? 'Buscando...' : 'Ver Ruta Óptima'}
                        </button>
                        {shortestPathIds.size > 0 && (
                            <button className="btn-action-route-clear" onClick={handleLimpiarRuta}>
                                Restablecer
                            </button>
                        )}
                    </div>
                </div>

                <div className="sidebar-control-card info-display-card">
                    <h4>Detalle de Selección</h4>
                    {hoveredNode || selectedNode ? (
                        <div className="selected-node-info">
                            <span 
                                className="info-node-badge"
                                style={{ 
                                    backgroundColor: NODE_COLORS[(hoveredNode || selectedNode).type] + '22',
                                    color: NODE_COLORS[(hoveredNode || selectedNode).type],
                                    borderColor: NODE_COLORS[(hoveredNode || selectedNode).type]
                                }}
                            >
                                {(hoveredNode || selectedNode).type}
                            </span>
                            <h5>{(hoveredNode || selectedNode).label}</h5>
                            <p className="node-id">ID Interno: {(hoveredNode || selectedNode).id}</p>
                            <p className="node-summary">{(hoveredNode || selectedNode).summary}</p>
                        </div>
                    ) : (
                        <div className="info-placeholder">
                            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" className="placeholder-icon"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="16" x2="12" y2="12"></line><line x1="12" y1="8" x2="12.01" y2="8"></line></svg>
                            <p>Pasa el cursor o haz clic en un nodo para analizar su información relacional.</p>
                        </div>
                    )}
                </div>
            </div>

            <div className="graph-display-panel">
                <div className="graph-panel-header">
                    <div className="header-meta">
                        <h3>Red de Movilidad Comercial</h3>
                        <span>Nodos: {graphData.nodes.length} | Aristas: {graphData.edges.length}</span>
                    </div>
                    <div className="zoom-controls">
                        <button onClick={() => handleZoom(0.1)} title="Acercar">+</button>
                        <button onClick={() => handleZoom(-0.1)} title="Alejar">-</button>
                        <button onClick={() => { setZoom(1); setPan({ x: 0, y: 0 }); }} title="Restaurar Vista">⟲</button>
                        <button onClick={fetchData} className="refresh-graph-btn" title="Actualizar Red">
                            ↻
                        </button>
                    </div>
                </div>

                <div className="graph-canvas-container">
                    {loading ? (
                        <div className="graph-loading-overlay">
                            <div className="loading-spinner"></div>
                            <p>Procesando topología relacional en memoria...</p>
                        </div>
                    ) : error ? (
                        <div className="graph-error-overlay">
                            <p>Error de conexión: {error}</p>
                            <button onClick={fetchData}>Reintentar</button>
                        </div>
                    ) : (
                        <svg
                            ref={svgRef}
                            className="graph-svg"
                            onMouseDown={handleMouseDown}
                            onMouseMove={handleMouseMove}
                            onMouseUp={handleMouseUp}
                            onMouseLeave={handleMouseUp}
                        >
                            <defs>
                                <pattern id="cyber-grid" width="40" height="40" patternUnits="userSpaceOnUse">
                                    <path d="M 40 0 L 0 0 0 40" fill="none" stroke="#252131" strokeWidth="1" />
                                </pattern>
                                <filter id="glow" x="-20%" y="-20%" width="140%" height="140%">
                                    <feGaussianBlur stdDeviation="8" result="blur" />
                                    <feComposite in="SourceGraphic" in2="blur" operator="over" />
                                </filter>
                                <filter id="glow-highlight" x="-30%" y="-30%" width="160%" height="160%">
                                    <feGaussianBlur stdDeviation="12" result="blur" />
                                    <feComposite in="SourceGraphic" in2="blur" operator="over" />
                                </filter>
                            </defs>

                            {/* Rejilla de Fondo */}
                            <rect id="grid-bg" width="100%" height="100%" fill="url(#cyber-grid)" />

                            {/* Capa de Vista Principal con Zoom y Pan */}
                            <g transform={`translate(${pan.x}, ${pan.y}) scale(${zoom})`}>
                                {/* ARISTAS (EDGES) */}
                                <g className="edges-layer">
                                    {graphData.edges.map(edge => {
                                        const sourcePos = positions[edge.source];
                                        const targetPos = positions[edge.target];

                                        if (!sourcePos || !targetPos) return null;

                                        const inPath = isEdgeInShortestPath(edge);
                                        const isHighlighted = selectedNode && (edge.source === selectedNode.id || edge.target === selectedNode.id);
                                        const isDimmed = selectedNode && !isHighlighted;
                                        const isShortestPathActive = shortestPathIds.size > 0;

                                        return (
                                            <g key={edge.id} className="edge-group">
                                                {/* Línea Principal de la Arista */}
                                                <line
                                                    x1={sourcePos.x}
                                                    y1={sourcePos.y}
                                                    x2={targetPos.x}
                                                    y2={targetPos.y}
                                                    className={`graph-edge-line ${inPath ? 'shortest-path' : ''} ${isHighlighted ? 'highlighted' : ''} ${isDimmed ? 'dimmed' : ''}`}
                                                    strokeWidth={inPath ? 4.5 : isHighlighted ? 3 : 1.5}
                                                    stroke={
                                                        inPath 
                                                            ? '#ffd700' // Gold para la ruta óptima
                                                            : isHighlighted 
                                                                ? NODE_COLORS[selectedNode.type] 
                                                                : '#3d384e'
                                                    }
                                                />

                                                {/* Efecto de partículas animadas fluyendo por conexión */}
                                                {(inPath || (isHighlighted && !isShortestPathActive)) && (
                                                    <circle
                                                        r={inPath ? 4 : 2.5}
                                                        fill={inPath ? '#ffd700' : NODE_COLORS[selectedNode.type]}
                                                        filter="url(#glow)"
                                                    >
                                                        <animateMotion
                                                            dur={`${4 / (edge.weight || 1)}s`}
                                                            repeatCount="indefinite"
                                                            path={`M ${sourcePos.x} ${sourcePos.y} L ${targetPos.x} ${targetPos.y}`}
                                                        />
                                                    </circle>
                                                )}
                                            </g>
                                        );
                                    })}
                                </g>

                                {/* NODOS (NODES) */}
                                <g className="nodes-layer">
                                    {graphData.nodes.map(node => {
                                        const pos = positions[node.id];
                                        if (!pos) return null;

                                        const isSelected = selectedNode && selectedNode.id === node.id;
                                        const inPath = shortestPathIds.has(node.id);
                                        const isHighlighted = selectedNode && (node.id === selectedNode.id || connectedEdges.has(node.id));
                                        
                                        // Atenuar nodos que no participan de la selección o ruta activa
                                        let opacity = 1;
                                        let isDimmed = false;
                                        
                                        if (shortestPathIds.size > 0) {
                                            if (!inPath) {
                                                opacity = 0.25;
                                                isDimmed = true;
                                            }
                                        } else if (selectedNode) {
                                            const isConnected = graphData.edges.some(
                                                e => (e.source === selectedNode.id && e.target === node.id) ||
                                                     (e.target === selectedNode.id && e.source === node.id)
                                            );
                                            if (!isSelected && !isConnected) {
                                                opacity = 0.25;
                                                isDimmed = true;
                                            }
                                        }

                                        return (
                                            <g
                                                key={node.id}
                                                transform={`translate(${pos.x}, ${pos.y})`}
                                                className={`node-group ${isSelected ? 'selected' : ''} ${isDimmed ? 'dimmed' : ''}`}
                                                style={{ opacity }}
                                                onMouseEnter={() => setHoveredNode(node)}
                                                onMouseLeave={() => setHoveredNode(null)}
                                                onMouseDown={(e) => {
                                                    e.stopPropagation();
                                                    dragNodeRef.current = node.id;
                                                    setSelectedNode(node);
                                                }}
                                            >
                                                {/* Resplandor de fondo glow */}
                                                {(isSelected || inPath) && (
                                                    <circle
                                                        r={node.type === 'ZONA' ? 24 : 18}
                                                        fill={inPath ? '#ffd700' : NODE_COLORS[node.type]}
                                                        opacity="0.4"
                                                        filter="url(#glow-highlight)"
                                                    />
                                                )}

                                                {/* Círculo base del nodo */}
                                                <circle
                                                    r={node.type === 'ZONA' ? 18 : 13}
                                                    fill="#1a1625"
                                                    stroke={inPath ? '#ffd700' : NODE_COLORS[node.type]}
                                                    strokeWidth={isSelected || inPath ? 3.5 : 2}
                                                    className="node-circle"
                                                    filter="url(#glow)"
                                                />

                                                {/* Decoración del centro según tipo */}
                                                <circle
                                                    r={node.type === 'ZONA' ? 8 : 5}
                                                    fill={inPath ? '#ffd700' : NODE_COLORS[node.type]}
                                                    className="node-center-dot"
                                                />

                                                {/* Etiqueta del Nodo */}
                                                <text
                                                    y={node.type === 'ZONA' ? 34 : 26}
                                                    textAnchor="middle"
                                                    className={`node-label ${isSelected || inPath ? 'active' : ''}`}
                                                    fill={inPath ? '#ffe875' : isSelected ? '#ffffff' : '#b2afc2'}
                                                >
                                                    {node.label}
                                                </text>
                                            </g>
                                        );
                                    })}
                                </g>
                            </g>
                        </svg>
                    )}
                </div>
            </div>
        </div>
    );
}
