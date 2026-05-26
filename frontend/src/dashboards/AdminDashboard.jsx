import { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Layout from '../components/Layout';
import AdminTables from '../components/AdminTables';
import AdminModal from '../components/AdminModal';
import RelationsGraphVisualizer from '../components/RelationsGraphVisualizer';
import './AdminDashboard.css';
import { apiUrl } from '../api';

const ZONAS = ['NORTE', 'SUR', 'ESTE', 'OESTE', 'CENTRO'];

function AdminDashboard() {
    const navigate = useNavigate();
    const [seccionActiva, setSeccionActiva] = useState('inmuebles');
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [elementoAEditar, setElementoAEditar] = useState(null);

    const [inmuebles, setInmuebles] = useState([]);
    const [clientes, setClientes] = useState([]);
    const [asesores, setAsesores] = useState([]);
    const [alertas, setAlertas] = useState([]);
    const [historialAlertas, setHistorialAlertas] = useState([]);
    const [alertasView, setAlertasView] = useState('pendientes');
    const [comportamientos, setComportamientos] = useState([]);
    const [filtroNivel, setFiltroNivel] = useState('TODOS');
    const [isAnalizando, setIsAnalizando] = useState(false);

    // ── Estado para Reportes ──
    const [reporteSubView, setReporteSubView] = useState('zona');
    const [reporteZonaSeleccionada, setReporteZonaSeleccionada] = useState('NORTE');
    const [reportePrecioMin, setReportePrecioMin] = useState('');
    const [reportePrecioMax, setReportePrecioMax] = useState('');
    const [reporteOperaciones, setReporteOperaciones] = useState([]);
    const [reporteVisitas, setReporteVisitas] = useState([]);
    const [reporteCierres, setReporteCierres] = useState([]);
    const [reporteCierresZona, setReporteCierresZona] = useState('TODOS');
    const [isLoadingReporte, setIsLoadingReporte] = useState(false);

    // ==========================================================================
    // PETICIONES HTTP (FETCH ASYNC/AWAIT)
    // ==========================================================================
    const fetchClientes = useCallback(async () => {
        try {
            const response = await fetch(apiUrl('/clientes'));
            if (!response.ok) throw new Error('Error al obtener el listado de clientes');
            const data = await response.json();
            setClientes(data);
        } catch (error) {
            console.error('Error en fetchClientes:', error);
        }
    }, []);

    const fetchAsesores = useCallback(async () => {
        try {
            const response = await fetch(apiUrl('/asesores'));
            if (!response.ok) throw new Error('Error al obtener el listado de asesores');
            const data = await response.json();
            setAsesores(data);
        } catch (error) {
            console.error('Error en fetchAsesores:', error);
        }
    }, []);

    const fetchInmuebles = useCallback(async () => {
        try {
            const response = await fetch(apiUrl('/inmuebles'));
            if (!response.ok) throw new Error('Error al obtener el listado de inmuebles');
            const data = await response.json();
            setInmuebles(data);
        } catch (error) {
            console.error('Error en fetchInmuebles:', error);
        }
    }, []);

    const fetchAlertasPendientes = useCallback(async () => {
        try {
            const response = await fetch(apiUrl('/alertas/pendientes'));
            if (!response.ok) throw new Error('Error al obtener alertas pendientes');
            const data = await response.json();
            setAlertas(data);
        } catch (error) {
            console.error('Error en fetchAlertasPendientes:', error);
        }
    }, []);

    const fetchAlertasHistorial = useCallback(async () => {
        try {
            const response = await fetch(apiUrl('/alertas/historial'));
            if (!response.ok) throw new Error('Error al obtener historial de alertas');
            const data = await response.json();
            setHistorialAlertas(data);
        } catch (error) {
            console.error('Error en fetchAlertasHistorial:', error);
        }
    }, []);

    const parseComportamientoResponse = (data) => {
        if (Array.isArray(data)) {
            return data;
        }
        if (data && Array.isArray(data.elements)) {
            return data.elements;
        }
        return [];
    };

    const fetchComportamientos = useCallback(async () => {
        try {
            const response = await fetch(apiUrl('/comportamiento/registro'));
            if (!response.ok) throw new Error('Error al obtener registros de comportamiento');
            const data = await response.json();
            setComportamientos(parseComportamientoResponse(data));
        } catch (error) {
            console.error('Error en fetchComportamientos:', error);
        }
    }, []);

    const fetchComportamientoPorNivel = useCallback(async (nivel) => {
        if (nivel === 'TODOS') {
            fetchComportamientos();
            return;
        }

        try {
            const response = await fetch(apiUrl(`/comportamiento/registro/nivel/${nivel}`));
            if (!response.ok) throw new Error(`Error al filtrar por nivel ${nivel}`);
            const data = await response.json();
            setComportamientos(data);
        } catch (error) {
            console.error('Error en fetchComportamientoPorNivel:', error);
        }
    }, [fetchComportamientos]);

    // ══════════════════════════════════════════════════════════════════════════
    // FETCH FUNCTIONS PARA REPORTES (OperacionController)
    // ══════════════════════════════════════════════════════════════════════════
    const fetchOperacionesPorZona = useCallback(async (zona) => {
        setIsLoadingReporte(true);
        try {
            const response = await fetch(apiUrl(`/operaciones/zona/${zona}`));
            if (!response.ok) throw new Error('Error al consultar operaciones por zona');
            const data = await response.json();
            setReporteOperaciones(Array.isArray(data) ? data : (data.elements || []));
        } catch (error) {
            console.error('Error en fetchOperacionesPorZona:', error);
            setReporteOperaciones([]);
        } finally {
            setIsLoadingReporte(false);
        }
    }, []);

    const fetchOperacionesPorPrecio = useCallback(async (min, max) => {
        if (min === '' || max === '' || Number(min) > Number(max)) return;
        setIsLoadingReporte(true);
        try {
            const response = await fetch(apiUrl(`/operaciones/precio?min=${min}&max=${max}`));
            if (!response.ok) throw new Error('Error al consultar operaciones por precio');
            const data = await response.json();
            setReporteOperaciones(Array.isArray(data) ? data : (data.elements || []));
        } catch (error) {
            console.error('Error en fetchOperacionesPorPrecio:', error);
            setReporteOperaciones([]);
        } finally {
            setIsLoadingReporte(false);
        }
    }, []);

    const fetchVisitasPorZona = useCallback(async (zona) => {
        setIsLoadingReporte(true);
        try {
            const response = await fetch(apiUrl(`/operaciones/visitas/zona/${zona}`));
            if (!response.ok) throw new Error('Error al consultar visitas por zona');
            const data = await response.json();
            setReporteVisitas(Array.isArray(data) ? data : (data.elements || []));
        } catch (error) {
            console.error('Error en fetchVisitasPorZona:', error);
            setReporteVisitas([]);
        } finally {
            setIsLoadingReporte(false);
        }
    }, []);

    const fetchOperacionesCerradas = useCallback(async () => {
        setIsLoadingReporte(true);
        try {
            const response = await fetch(apiUrl('/operaciones/cerradas'));
            if (!response.ok) throw new Error('Error al consultar operaciones cerradas');
            const data = await response.json();
            setReporteCierres(Array.isArray(data) ? data : (data.elements || []));
        } catch (error) {
            console.error('Error en fetchOperacionesCerradas:', error);
            setReporteCierres([]);
        } finally {
            setIsLoadingReporte(false);
        }
    }, []);

    const fetchOperacionesCerradasPorZona = useCallback(async (zona) => {
        setIsLoadingReporte(true);
        try {
            const response = await fetch(apiUrl(`/operaciones/cerradas/zona/${zona}`));
            if (!response.ok) throw new Error('Error al consultar cierres por zona');
            const data = await response.json();
            setReporteCierres(Array.isArray(data) ? data : (data.elements || []));
        } catch (error) {
            console.error('Error en fetchOperacionesCerradasPorZona:', error);
            setReporteCierres([]);
        } finally {
            setIsLoadingReporte(false);
        }
    }, []);

    const handleReporteSubViewChange = (view) => {
        setReporteSubView(view);
        setReporteOperaciones([]);
        setReporteVisitas([]);
        setReporteCierres([]);
        if (view === 'zona') {
            fetchOperacionesPorZona(reporteZonaSeleccionada);
        } else if (view === 'visitas') {
            fetchVisitasPorZona(reporteZonaSeleccionada);
        } else if (view === 'cierres') {
            setReporteCierresZona('TODOS');
            fetchOperacionesCerradas();
        }
    };

    const handleReporteZonaChange = (zona) => {
        setReporteZonaSeleccionada(zona);
        if (reporteSubView === 'zona') {
            fetchOperacionesPorZona(zona);
        } else if (reporteSubView === 'visitas') {
            fetchVisitasPorZona(zona);
        }
    };

    const handleCierresZonaChange = (zona) => {
        setReporteCierresZona(zona);
        if (zona === 'TODOS') {
            fetchOperacionesCerradas();
        } else {
            fetchOperacionesCerradasPorZona(zona);
        }
    };

    const getReporteData = () => {
        if (reporteSubView === 'visitas') return reporteVisitas;
        if (reporteSubView === 'cierres') return reporteCierres;
        return reporteOperaciones;
    };

    const handleAnalizarComportamiento = async () => {
        if (!window.confirm('¿Deseas ejecutar el análisis de comportamiento atípico ahora?')) {
            return;
        }

        setIsAnalizando(true);
        try {
            const response = await fetch(apiUrl('/comportamiento/analizar'), {
                method: 'POST'
            });
            if (!response.ok) {
                throw new Error('No se pudo ejecutar el análisis de comportamiento');
            }
            await response.text();
            await Promise.all([fetchComportamientos(), fetchAlertasPendientes(), fetchAlertasHistorial()]);
            alert('Análisis ejecutado con éxito. Nuevas alertas y registros actualizados.');
        } catch (error) {
            alert(`Error en el análisis: ${error.message}`);
        } finally {
            setIsAnalizando(false);
        }
    };

    const handleResolverComportamiento = async (id) => {
        const observaciones = window.prompt('Agrega observaciones opcionales al resolver el comportamiento:');
        if (observaciones === null) return;

        try {
            const response = await fetch(apiUrl(`/comportamiento/registro/${id}/resolver?observaciones=${encodeURIComponent(observaciones)}`), {
                method: 'PUT'
            });
            if (!response.ok) {
                throw new Error('No se pudo resolver el registro de comportamiento');
            }
            await fetchComportamientos();
            alert('El registro de comportamiento se marcó como resuelto.');
        } catch (error) {
            alert(`Error al resolver registro: ${error.message}`);
        }
    };

    const handleMarkAlertaAsAttended = async (codigo) => {
        try {
            const response = await fetch(apiUrl(`/alertas/${codigo}/atender`), {
                method: 'PUT'
            });
            if (!response.ok) {
                throw new Error('No se pudo marcar la alerta como atendida');
            }
            await Promise.all([fetchAlertasPendientes(), fetchAlertasHistorial()]);
            alert('Alerta atendida exitosamente.');
        } catch (error) {
            alert(`Error al atender alerta: ${error.message}`);
        }
    };

    const handleDelete = async (id, tipo) => {
        const confirmDelete = window.confirm(`¿Seguro que deseas eliminar permanentemente este registro en ${tipo}?`);
        if (!confirmDelete) return;

        try {
            const response = await fetch(apiUrl(`/${tipo}/${id}`), {
                method: 'DELETE'
            });
            if (!response.ok) {
                throw new Error(`No se pudo eliminar el registro con ID ${id}`);
            }

            if (tipo === 'clientes') {
                setClientes(prev => prev.filter(item => item.id !== id));
            } else if (tipo === 'inmuebles') {
                setInmuebles(prev => prev.filter(item => item.codigo !== id));
            } else if (tipo === 'asesores') {
                setAsesores(prev => prev.filter(item => item.id !== id));
            } else if (tipo === 'alertas') {
                setAlertas(prev => prev.filter(item => item.codigo !== id));
                setHistorialAlertas(prev => prev.filter(item => item.codigo !== id));
            }

            alert('Registro eliminado correctamente');
        } catch (error) {
            alert(`Error al eliminar: ${error.message}`);
        }
    };

    // ==========================================================================
    // CONTROL DE MODALES Y SELECCIÓN DE ESTADO
    // ==========================================================================
    const abrirModalRegistro = () => { setElementoAEditar(null); setIsModalOpen(true); };
    const abrirModalEdicion = (elemento) => { setElementoAEditar(elemento); setIsModalOpen(true); };

    const obtenerColeccionActiva = () => {
        switch (seccionActiva) {
            case 'inmuebles':
                return inmuebles;
            case 'clientes':
                return clientes;
            case 'asesores':
                return asesores;
            case 'alertas':
                return alertasView === 'historial' ? historialAlertas : alertas;
            case 'comportamiento':
                return comportamientos;
            case 'reportes':
                return getReporteData();
            default:
                return [];
        }
    };

    const handleModalSuccess = () => {
        setIsModalOpen(false);
        if (seccionActiva === 'clientes') fetchClientes();
        if (seccionActiva === 'asesores') fetchAsesores();
        if (seccionActiva === 'inmuebles') fetchInmuebles();
    };

    useEffect(() => {
        const session = JSON.parse(localStorage.getItem('user_session'));
        if (!session || session.rol !== 'ADMINISTRADOR') {
            navigate('/login');
        } else {
            fetchClientes();
            fetchInmuebles();
            fetchAsesores();
            fetchAlertasPendientes();
            fetchAlertasHistorial();
            fetchComportamientos();
        }
    }, [fetchAlertasHistorial, fetchAlertasPendientes, fetchAsesores, fetchClientes, fetchComportamientos, fetchInmuebles, navigate]);

    const handleAlertasViewChange = (view) => {
        setAlertasView(view);
        if (view === 'historial') {
            fetchAlertasHistorial();
        } else {
            fetchAlertasPendientes();
        }
    };

    const totalAlertasPendientes = alertas.length;
    const totalAlertasHistorial = historialAlertas.length;
    const totalRegistrosComportamiento = comportamientos.length;

    return (
        <Layout contentClassName="admin-layout-container">
            <div className="dashboard-wrapper">
                <aside className="dashboard-sidebar">
                    <div className="sidebar-header">
                        <h3>Admin Menú</h3>
                    </div>
                    <nav className="sidebar-nav">
                        <button className={`nav-item ${seccionActiva === 'inmuebles' ? 'active' : ''}`} onClick={() => setSeccionActiva('inmuebles')}>
                            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="sidebar-icon-svg"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path><polyline points="9 22 9 12 15 12 15 22"></polyline></svg>
                            Gestión Inmuebles
                        </button>
                        <button className={`nav-item ${seccionActiva === 'clientes' ? 'active' : ''}`} onClick={() => setSeccionActiva('clientes')}>
                            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="sidebar-icon-svg"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="9" cy="7" r="4"></circle><path d="M23 21v-2a4 4 0 0 0-3-3.87"></path><path d="M16 3.13a4 4 0 0 1 0 7.75"></path></svg>
                            Gestión Clientes
                        </button>
                        <button className={`nav-item ${seccionActiva === 'asesores' ? 'active' : ''}`} onClick={() => setSeccionActiva('asesores')}>
                            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="sidebar-icon-svg"><rect x="2" y="7" width="20" height="14" rx="2" ry="2"></rect><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"></path></svg>
                            Gestión Asesores
                        </button>
                        <button className={`nav-item ${seccionActiva === 'alertas' ? 'active' : ''}`} onClick={() => setSeccionActiva('alertas')}>
                            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="sidebar-icon-svg"><path d="M18 8a6 6 0 0 0-12 0c0 5.25-3 6-3 6h18s-3-.75-3-6"></path><path d="M13.73 21a2 2 0 0 1-3.46 0"></path></svg>
                            Alertas
                        </button>
                        <button className={`nav-item ${seccionActiva === 'comportamiento' ? 'active' : ''}`} onClick={() => setSeccionActiva('comportamiento')}>
                            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="sidebar-icon-svg"><path d="M12 20l9-5-9-5-9 5 9 5z"></path><path d="M12 12l9-5-9-5-9 5 9 5z"></path></svg>
                            Comportamiento
                        </button>
                        <button className={`nav-item ${seccionActiva === 'reportes' ? 'active' : ''}`} onClick={() => { setSeccionActiva('reportes'); handleReporteSubViewChange('zona'); }}>
                            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="sidebar-icon-svg"><path d="M21 15V5a2 2 0 0 0-2-2H5a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h11"></path><polyline points="9 22 9 12 15 12 15 22"></polyline><line x1="9" y1="8" x2="9" y2="8.01"></line><line x1="15" y1="8" x2="15" y2="8.01"></line><path d="M17 21l2 2 4-4"></path></svg>
                            Reportes
                        </button>
                        <button className={`nav-item ${seccionActiva === 'relaciones' ? 'active' : ''}`} onClick={() => setSeccionActiva('relaciones')}>
                            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="sidebar-icon-svg"><circle cx="18" cy="5" r="3"></circle><circle cx="6" cy="12" r="3"></circle><circle cx="18" cy="19" r="3"></circle><line x1="8.59" y1="13.51" x2="15.42" y2="17.49"></line><line x1="15.41" y1="6.51" x2="8.59" y2="10.49"></line></svg>
                            Red de Relaciones
                        </button>
                    </nav>
                </aside>

                <main className="dashboard-content">
                    <div className="content-view-header">
                        <div>
                            <h1>
                                {seccionActiva === 'inmuebles' && 'Catálogo de Inmuebles'}
                                {seccionActiva === 'clientes' && 'Control de Clientes'}
                                {seccionActiva === 'asesores' && 'Equipo de Asesores'}
                                {seccionActiva === 'alertas' && 'Centro de Alertas'}
                                {seccionActiva === 'comportamiento' && 'Monitoreo de Comportamiento'}
                                {seccionActiva === 'reportes' && 'Consultar Reportes'}
                                {seccionActiva === 'relaciones' && 'Análisis Estructural (Grafos)'}
                            </h1>
                            <p>
                                {seccionActiva === 'alertas' && 'Visualiza alertas del sistema, atiende notificaciones y consulta el historial de eventos.'}
                                {seccionActiva === 'comportamiento' && 'Analiza eventos atípicos y resuelve comportamientos anormales detectados por el sistema.'}
                                {seccionActiva === 'reportes' && 'Genera reportes por zona, rango de precio, visitas y operaciones cerradas.'}
                                {seccionActiva === 'relaciones' && 'Explora de forma gráfica e interactiva las relaciones, flujos y rutas óptimas en la red de movilidad comercial.'}
                                {(seccionActiva !== 'alertas' && seccionActiva !== 'comportamiento' && seccionActiva !== 'reportes' && seccionActiva !== 'relaciones') && 'Conexión directa con ecosistema base de datos DomusTech.'}
                            </p>
                        </div>
                        {seccionActiva === 'inmuebles' && (
                            <button className="btn-add-registry" onClick={abrirModalRegistro}>
                                + Registrar Inmueble
                            </button>
                        )}
                        {seccionActiva === 'clientes' && (
                            <button className="btn-add-registry" onClick={abrirModalRegistro}>
                                + Registrar Cliente
                            </button>
                        )}
                        {seccionActiva === 'asesores' && (
                            <button className="btn-add-registry" onClick={abrirModalRegistro}>
                                + Registrar Asesor
                            </button>
                        )}
                        {seccionActiva === 'alertas' && (
                            <div className="action-button-group">
                                <button className="btn-secondary" onClick={() => handleAlertasViewChange('pendientes')}>Pendientes</button>
                                <button className="btn-secondary" onClick={() => handleAlertasViewChange('historial')}>Historial</button>
                            </div>
                        )}
                        {seccionActiva === 'comportamiento' && (
                            <button className="btn-add-registry" onClick={handleAnalizarComportamiento} disabled={isAnalizando}>
                                {isAnalizando ? 'Analizando...' : 'Ejecutar Análisis'}
                            </button>
                        )}
                    </div>

                    {(seccionActiva === 'alertas' || seccionActiva === 'comportamiento' || seccionActiva === 'reportes') && (
                        <div className="dashboard-metrics">
                            {seccionActiva === 'alertas' && (
                                <>
                                    <div className="metric-card">
                                        <p>Alertas pendientes</p>
                                        <h3>{totalAlertasPendientes}</h3>
                                    </div>
                                    <div className="metric-card">
                                        <p>Alertas en historial</p>
                                        <h3>{totalAlertasHistorial}</h3>
                                    </div>
                                </>
                            )}
                            {seccionActiva === 'comportamiento' && (
                                <div className="metric-card">
                                    <p>Registros de comportamiento</p>
                                    <h3>{totalRegistrosComportamiento}</h3>
                                </div>
                            )}
                            {seccionActiva === 'reportes' && (
                                <>
                                    <div className="metric-card metric-card-accent">
                                        <p>
                                            {reporteSubView === 'visitas' ? 'Visitas encontradas' : 'Operaciones encontradas'}
                                        </p>
                                        <h3>{isLoadingReporte ? '...' : getReporteData().length}</h3>
                                    </div>
                                    <div className="metric-card">
                                        <p>Vista activa</p>
                                        <h3 className="metric-label-small">
                                            {reporteSubView === 'zona' && 'Por Zona'}
                                            {reporteSubView === 'precio' && 'Por Precio'}
                                            {reporteSubView === 'visitas' && 'Visitas'}
                                            {reporteSubView === 'cierres' && 'Cierres'}
                                        </h3>
                                    </div>
                                </>
                            )}
                        </div>
                    )}

                    {seccionActiva === 'comportamiento' && (
                        <div className="comportamiento-filters">
                            <label>
                                Filtrar por nivel de atención:
                                <select value={filtroNivel} onChange={(e) => {
                                    setFiltroNivel(e.target.value);
                                    fetchComportamientoPorNivel(e.target.value);
                                }}>
                                    <option value="TODOS">Todos</option>
                                    <option value="BAJO">Bajo</option>
                                    <option value="MEDIO">Medio</option>
                                    <option value="ALTO">Alto</option>
                                    <option value="CRITICO">Crítico</option>
                                </select>
                            </label>
                        </div>
                    )}

                    {seccionActiva === 'reportes' && (
                        <div className="reportes-panel">
                            <div className="reportes-toolbar">
                                <div className="reportes-subnav" aria-label="Tipo de reporte">
                                    <button className={`reportes-subnav-btn ${reporteSubView === 'zona' ? 'active' : ''}`} onClick={() => handleReporteSubViewChange('zona')}>
                                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="reportes-subnav-icon"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path><circle cx="12" cy="10" r="3"></circle></svg>
                                    Por Zona
                                    </button>
                                    <button className={`reportes-subnav-btn ${reporteSubView === 'precio' ? 'active' : ''}`} onClick={() => handleReporteSubViewChange('precio')}>
                                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="reportes-subnav-icon"><line x1="12" y1="1" x2="12" y2="23"></line><path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"></path></svg>
                                    Por Precio
                                    </button>
                                    <button className={`reportes-subnav-btn ${reporteSubView === 'visitas' ? 'active' : ''}`} onClick={() => handleReporteSubViewChange('visitas')}>
                                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="reportes-subnav-icon"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect><line x1="16" y1="2" x2="16" y2="6"></line><line x1="8" y1="2" x2="8" y2="6"></line><line x1="3" y1="10" x2="21" y2="10"></line></svg>
                                    Visitas
                                    </button>
                                    <button className={`reportes-subnav-btn ${reporteSubView === 'cierres' ? 'active' : ''}`} onClick={() => handleReporteSubViewChange('cierres')}>
                                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="reportes-subnav-icon"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path><polyline points="22 4 12 14.01 9 11.01"></polyline></svg>
                                    Cierres
                                    </button>
                                </div>
                            </div>

                            <div className="reportes-filters">
                                {(reporteSubView === 'zona' || reporteSubView === 'visitas') && (
                                    <div className="reportes-filter-card">
                                        <div className="reportes-filter-copy">
                                            <span className="reportes-filter-kicker">Filtro activo</span>
                                            <strong>Zona geográfica</strong>
                                        </div>
                                        <div className="reportes-select-control">
                                            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="reportes-select-icon"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path><circle cx="12" cy="10" r="3"></circle></svg>
                                            <select value={reporteZonaSeleccionada} onChange={(e) => handleReporteZonaChange(e.target.value)}>
                                                {ZONAS.map((z) => (
                                                    <option key={z} value={z}>{z}</option>
                                                ))}
                                            </select>
                                        </div>
                                    </div>
                                )}

                                {reporteSubView === 'precio' && (
                                    <div className="reportes-filter-card reportes-filter-card-wide">
                                        <div className="reportes-filter-copy">
                                            <span className="reportes-filter-kicker">Filtro activo</span>
                                            <strong>Rango de precio</strong>
                                        </div>
                                        <div className="reportes-price-inputs">
                                            <div className="reportes-price-field">
                                                <span>Mínimo</span>
                                                <div className="price-input-wrapper">
                                                    <span className="price-currency">$</span>
                                                    <input
                                                        type="number"
                                                        placeholder="0"
                                                        value={reportePrecioMin}
                                                        onChange={(e) => setReportePrecioMin(e.target.value)}
                                                    />
                                                </div>
                                            </div>
                                            <div className="reportes-price-field">
                                                <span>Máximo</span>
                                                <div className="price-input-wrapper">
                                                    <span className="price-currency">$</span>
                                                    <input
                                                        type="number"
                                                        placeholder="500000000"
                                                        value={reportePrecioMax}
                                                        onChange={(e) => setReportePrecioMax(e.target.value)}
                                                    />
                                                </div>
                                            </div>
                                            <button
                                                className="btn-buscar-precio"
                                                onClick={() => fetchOperacionesPorPrecio(reportePrecioMin, reportePrecioMax)}
                                                disabled={reportePrecioMin === '' || reportePrecioMax === '' || Number(reportePrecioMin) > Number(reportePrecioMax)}
                                            >
                                                Buscar
                                            </button>
                                        </div>
                                    </div>
                                )}

                                {reporteSubView === 'cierres' && (
                                    <div className="reportes-filter-card">
                                        <div className="reportes-filter-copy">
                                            <span className="reportes-filter-kicker">Filtro activo</span>
                                            <strong>Cierres por zona</strong>
                                        </div>
                                        <div className="reportes-select-control">
                                            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="reportes-select-icon"><path d="M20 6 9 17l-5-5"></path></svg>
                                            <select value={reporteCierresZona} onChange={(e) => handleCierresZonaChange(e.target.value)}>
                                                <option value="TODOS">Todas las zonas</option>
                                                {ZONAS.map((z) => (
                                                    <option key={z} value={z}>{z}</option>
                                                ))}
                                            </select>
                                        </div>
                                    </div>
                                )}
                            </div>
                        </div>
                    )}

                    {seccionActiva === 'relaciones' ? (
                        <RelationsGraphVisualizer />
                    ) : (
                        <div className="table-responsive-wrapper">
                            <AdminTables
                                seccion={seccionActiva}
                                data={obtenerColeccionActiva()}
                                onEdit={abrirModalEdicion}
                                onDelete={handleDelete}
                                onAtender={handleMarkAlertaAsAttended}
                                onResolver={handleResolverComportamiento}
                                reporteSubView={reporteSubView}
                                isLoadingReporte={isLoadingReporte}
                            />
                        </div>
                    )}
                </main>
            </div>

            {isModalOpen && (
                <AdminModal
                    seccion={seccionActiva}
                    datos={elementoAEditar}
                    onClose={() => setIsModalOpen(false)}
                    onSuccess={handleModalSuccess}
                />
            )}
        </Layout>
    );
}

export default AdminDashboard;
