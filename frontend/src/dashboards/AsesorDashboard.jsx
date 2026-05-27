import { useCallback, useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Layout from '../components/Layout';
import './RoleDashboard.css';
import VentaModal from '../components/VentaModal';
import ArriendoModal from '../components/ArriendoModal';

const API_BASE_URL = 'http://localhost:8080/api';

const formatCurrency = (value) => new Intl.NumberFormat('es-CO', {
    style: 'currency',
    currency: 'COP',
    maximumFractionDigits: 0
}).format(Number(value || 0));

function AsesorDashboard() {
    const navigate = useNavigate();
    const [session, setSession] = useState(null);
    const [seccionActiva, setSeccionActiva] = useState('resumen');
    const [asesor, setAsesor] = useState(null);
    const [inmuebles, setInmuebles] = useState([]);
    const [visitas, setVisitas] = useState([]);
    const [solicitudesCancelacion, setSolicitudesCancelacion] = useState([]);
    const [statusMessage, setStatusMessage] = useState('');

    const fetchAsesor = useCallback(async (asesorId) => {
        const response = await fetch(`${API_BASE_URL}/asesores/${asesorId}`);
        if (!response.ok) throw new Error('No se pudo cargar el perfil del asesor');
        setAsesor(await response.json());
    }, []);

    const fetchInmuebles = useCallback(async () => {
        const response = await fetch(`${API_BASE_URL}/inmuebles`);
        if (!response.ok) throw new Error('No se pudo cargar el catalogo de inmuebles');
        setInmuebles(await response.json());
    }, []);

    const fetchVisitas = useCallback(async (asesorId) => {
        const response = await fetch(`${API_BASE_URL}/visitas/asesor/${asesorId}`);
        if (!response.ok) throw new Error('No se pudieron cargar las visitas');
        setVisitas(await response.json());
    }, []);

    const fetchSolicitudesCancelacion = useCallback(async (asesorId) => {
        if (!asesorId) {
            setSolicitudesCancelacion([]);
            return;
        }
        try {
            const response = await fetch(`${API_BASE_URL}/operaciones/asesor/${asesorId}/solicitudes-cancelacion`);
            if (!response.ok) {
                setSolicitudesCancelacion([]);
                return;
            }
            const data = await response.json();
            setSolicitudesCancelacion(Array.isArray(data) ? data : []);
        } catch {
            setSolicitudesCancelacion([]);
        }
    }, []);

    const cargarDatos = useCallback(async (asesorId) => {
        try {
            await Promise.all([
                fetchAsesor(asesorId),
                fetchInmuebles(),
                fetchVisitas(asesorId),
                fetchSolicitudesCancelacion(asesorId)
            ]);
        } catch (error) {
            setStatusMessage(error.message);
        }
    }, [fetchAsesor, fetchInmuebles, fetchVisitas, fetchSolicitudesCancelacion]);

    useEffect(() => {
        const savedSession = JSON.parse(localStorage.getItem('user_session'));
        if (!savedSession || savedSession.rol !== 'ASESOR') {
            navigate('/login');
            return;
        }
        setSession(savedSession);
        cargarDatos(savedSession.id);
    }, [cargarDatos, navigate]);

    useEffect(() => {
        if (!statusMessage) {
            return undefined;
        }
        const timer = window.setTimeout(() => setStatusMessage(''), 3200);
        return () => window.clearTimeout(timer);
    }, [statusMessage]);

    const inmueblesAsignados = useMemo(
        () => inmuebles.filter(inmueble => inmueble.asesorId === session?.id),
        [inmuebles, session]
    );

    const visitasActivas = useMemo(
        () => visitas.filter(visita => ['PENDIENTE', 'CONFIRMADA', 'REPROGRAMADA'].includes(visita.estado)),
        [visitas]
    );

    const actualizarEstadoVisita = async (codigo, accion, observaciones = '') => {
        try {
            const response = await fetch(`${API_BASE_URL}/visitas/${codigo}/${accion}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ observaciones })
            });
            if (!response.ok) {
                const error = await response.json().catch(() => null);
                throw new Error(error?.error || 'No se pudo actualizar la visita');
            }
            await fetchVisitas(session.id);
            setStatusMessage('Visita actualizada correctamente.');
        } catch (error) {
            setStatusMessage(error.message);
        }
    };

    const getImages = (inmueble) => inmueble?.imagenes?.length ? inmueble.imagenes : (inmueble?.imagen ? [inmueble.imagen] : []);

    const [ventaModal, setVentaModal] = useState({ open: false, codigo: null });
    const [arriendoModal, setArriendoModal] = useState({ open: false, codigo: null });

    const actualizarEstadoInmueble = async (codigo, estado) => {
        try {
            if (estado === 'VENDIDO') {
                setVentaModal({ open: true, codigo });
                return;
            }
            if (estado === 'ARRENDADO') {
                setArriendoModal({ open: true, codigo });
                return;
            }

            const response = await fetch(`${API_BASE_URL}/inmuebles/${codigo}/estado`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ estado })
            });
            if (!response.ok) {
                const error = await response.json().catch(() => null);
                throw new Error(error?.error || 'No se pudo actualizar el inmueble');
            }
            await Promise.all([fetchInmuebles(), fetchAsesor(session.id)]);
            setStatusMessage('Estado del inmueble actualizado.');
        } catch (error) {
            setStatusMessage(error.message);
        }
    };

    const procesarCancelacion = async (operacionCodigo) => {
        try {
            const response = await fetch(
                `${API_BASE_URL}/operaciones/arriendos/${operacionCodigo}/procesar-cancelacion?asesorId=${session.id}`,
                { method: 'PUT' }
            );
            if (!response.ok) {
                const err = await response.json().catch(() => null);
                throw new Error(err?.mensaje || err?.error || 'No se pudo procesar la cancelación');
            }
            await Promise.all([fetchInmuebles(), fetchSolicitudesCancelacion(session.id)]);
            setStatusMessage('Cancelación de arriendo procesada. El inmueble quedó disponible.');
        } catch (error) {
            setStatusMessage(error.message);
        }
    };

    const renderSolicitudesCancelacion = () => (
        <div className="table-responsive-wrapper">
            <table className="domustech-admin-table">
                <thead>
                    <tr><th>Operación</th><th>Detalle</th><th>Fecha</th><th>Acciones</th></tr>
                </thead>
                <tbody>
                    {solicitudesCancelacion.length === 0 ? (
                        <tr><td colSpan="4" className="table-empty-row">No hay solicitudes de cancelación pendientes.</td></tr>
                    ) : solicitudesCancelacion.map(alerta => (
                        <tr key={alerta.codigo}>
                            <td>{alerta.referenciaId}</td>
                            <td>{alerta.descripcion}</td>
                            <td>{alerta.fechaGeneracion ? new Date(alerta.fechaGeneracion).toLocaleString('es-CO') : '-'}</td>
                            <td>
                                <button
                                    className="table-action-button"
                                    onClick={() => procesarCancelacion(alerta.referenciaId)}
                                >
                                    Procesar cancelación
                                </button>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );

    const renderResumen = () => (
        <div className="role-summary">
            <section className="dashboard-panel">
                <h2>Perfil comercial</h2>
                <div className="metrics-grid compact">
                    <div><span>Zona asignada</span><strong>{asesor?.zonaAsignada || 'Sin zona'}</strong></div>
                    <div><span>Especialidad</span><strong>{asesor?.especialidad || 'Sin especialidad'}</strong></div>
                    <div><span>Inmuebles asignados</span><strong>{inmueblesAsignados.length}</strong></div>
                    <div><span>Cierres realizados</span><strong>{asesor?.numeroDeCierres ?? 0}</strong></div>
                </div>
            </section>
            <section className="dashboard-panel">
                <h2>Agenda activa</h2>
                <div className="metrics-grid compact">
                    <div><span>Pendientes</span><strong>{visitas.filter(v => v.estado === 'PENDIENTE').length}</strong></div>
                    <div><span>Confirmadas</span><strong>{visitas.filter(v => v.estado === 'CONFIRMADA').length}</strong></div>
                    <div><span>Realizadas</span><strong>{visitas.filter(v => v.estado === 'REALIZADA').length}</strong></div>
                    <div><span>Activas</span><strong>{visitasActivas.length}</strong></div>
                </div>
            </section>
        </div>
    );

    const renderInmuebles = () => (
        <div className="property-grid">
            {inmueblesAsignados.length === 0 ? <p className="empty-text">No tienes inmuebles asignados.</p> : inmueblesAsignados.map(inmueble => (
                <article className="property-card" key={inmueble.codigo}>
                    <div className="property-thumb">{getImages(inmueble)[0] ? <img src={getImages(inmueble)[0]} alt={inmueble.direccionBarrio || inmueble.direccion} /> : <span>{inmueble.tipoInmueble}</span>}</div>
                    <div className="property-body">
                        <div className="property-title-row">
                            <h3>{inmueble.direccionBarrio || inmueble.direccion}</h3>
                            <span className={`status-pill ${String(inmueble.estado).toLowerCase()}`}>{inmueble.estado}</span>
                        </div>
                        <p>{inmueble.ciudad} {inmueble.departamento ? `(${inmueble.departamento})` : ''} · {inmueble.zona}</p>
                        <strong>{formatCurrency(inmueble.precio)}</strong>
                        <div className="property-meta">
                            <span>{inmueble.tipoInmueble}</span>
                            <span>{inmueble.finalidad}</span>
                            <span>{inmueble.area} m2</span>
                        </div>
                    </div>
                    <div className="card-actions">
                        <button onClick={() => actualizarEstadoInmueble(inmueble.codigo, 'DISPONIBLE')}>Disponible</button>
                        <button onClick={() => actualizarEstadoInmueble(inmueble.codigo, 'RESERVADO')}>Reservar</button>
                        <button onClick={() => actualizarEstadoInmueble(inmueble.codigo, inmueble.finalidad === 'ARRENDAMIENTO' ? 'ARRENDADO' : 'VENDIDO')}>
                            Cerrar {inmueble.finalidad === 'ARRENDAMIENTO' ? 'arriendo' : 'venta'}
                        </button>
                    </div>
                </article>
            ))}
        </div>
    );

    const renderVisitas = () => (
        <div className="table-responsive-wrapper">
            <table className="domustech-admin-table">
                <thead><tr><th>Cliente</th><th>Inmueble</th><th>Fecha</th><th>Hora</th><th>Estado</th><th>Observaciones</th><th>Acciones</th></tr></thead>
                <tbody>
                    {visitas.length === 0 ? <tr><td colSpan="7" className="table-empty-row">No hay visitas asignadas.</td></tr> : visitas.map(visita => (
                        <tr key={visita.codigo}>
                            <td>{visita.clienteNombre}<br /><small>{visita.clienteId}</small></td>
                            <td>{visita.inmuebleDireccion}<br /><small>{visita.inmuebleCodigo}</small></td>
                            <td>{visita.fecha}</td>
                            <td>{visita.hora}</td>
                            <td><span className={`status-pill ${String(visita.estado).toLowerCase()}`}>{visita.estado}</span></td>
                            <td>{visita.observaciones || 'Sin observaciones'}</td>
                            <td>
                                <div className="table-actions-stack">
                                    {visita.estado === 'PENDIENTE' && <button className="table-action-button" onClick={() => actualizarEstadoVisita(visita.codigo, 'confirmar', 'Confirmada por el asesor')}>Confirmar</button>}
                                    {visita.estado !== 'REALIZADA' && visita.estado !== 'CANCELADA' && <button className="table-action-button" onClick={() => actualizarEstadoVisita(visita.codigo, 'realizar', 'Visita marcada como realizada')}>Realizada</button>}
                                    {visita.estado !== 'REALIZADA' && visita.estado !== 'CANCELADA' && <button className="table-action-button danger" onClick={() => actualizarEstadoVisita(visita.codigo, 'cancelar', 'Cancelada por el asesor')}>Cancelar</button>}
                                </div>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );

    return (
        <Layout contentClassName="admin-layout-container">
            <div className="dashboard-wrapper">
                <aside className="dashboard-sidebar">
                    <div className="sidebar-header"><h3>Asesor Menú</h3></div>
                    <nav className="sidebar-nav">
                        <button className={`nav-item ${seccionActiva === 'resumen' ? 'active' : ''}`} onClick={() => setSeccionActiva('resumen')}>Resumen</button>
                        <button className={`nav-item ${seccionActiva === 'inmuebles' ? 'active' : ''}`} onClick={() => setSeccionActiva('inmuebles')}>Inmuebles asignados</button>
                        <button className={`nav-item ${seccionActiva === 'visitas' ? 'active' : ''}`} onClick={() => setSeccionActiva('visitas')}>Visitas agendadas</button>
                        <button className={`nav-item ${seccionActiva === 'cierres' ? 'active' : ''}`} onClick={() => setSeccionActiva('cierres')}>Cierres</button>
                        <button className={`nav-item ${seccionActiva === 'cancelaciones' ? 'active' : ''}`} onClick={() => { setSeccionActiva('cancelaciones'); fetchSolicitudesCancelacion(session?.id); }}>
                            Cancelaciones
                            {solicitudesCancelacion.length > 0 && <span className="nav-badge">{solicitudesCancelacion.length}</span>}
                        </button>
                    </nav>
                </aside>

                <main className="dashboard-content">
                    <div className="content-view-header">
                        <div>
                            <h1>{
                                seccionActiva === 'resumen' ? 'Panel del asesor'
                                    : seccionActiva === 'inmuebles' ? 'Inmuebles asignados'
                                    : seccionActiva === 'visitas' ? 'Visitas agendadas'
                                    : seccionActiva === 'cancelaciones' ? 'Solicitudes de cancelación'
                                    : 'Cierres realizados'
                            }</h1>
                            <p>{session?.nombre}, consulta tu zona, especialidad, agenda e indicadores comerciales.</p>
                        </div>
                    </div>
                    {statusMessage && <div className="dashboard-alert">{statusMessage}</div>}
                    {seccionActiva === 'resumen' && renderResumen()}
                    {seccionActiva === 'inmuebles' && renderInmuebles()}
                    {seccionActiva === 'visitas' && renderVisitas()}
                    {seccionActiva === 'cierres' && <section className="dashboard-panel"><h2>Cierres realizados</h2><div className="metric-hero">{asesor?.numeroDeCierres ?? 0}</div><p>Este contador proviene del perfil del asesor registrado en el sistema.</p></section>}
                    {seccionActiva === 'cancelaciones' && renderSolicitudesCancelacion()}
                </main>
            </div>
            {ventaModal.open && (
                <VentaModal
                    inmuebleCodigo={ventaModal.codigo}
                    asesorId={session?.id}
                    onClose={() => setVentaModal({ open: false, codigo: null })}
                    onCompleted={() => {
                        fetchInmuebles();
                        fetchAsesor(session.id);
                        setStatusMessage('Venta registrada correctamente');
                    }}
                />
            )}
            {arriendoModal.open && (
                <ArriendoModal
                    inmuebleCodigo={arriendoModal.codigo}
                    asesorId={session?.id}
                    onClose={() => setArriendoModal({ open: false, codigo: null })}
                    onCompleted={() => {
                        fetchInmuebles();
                        fetchAsesor(session.id);
                        setStatusMessage('Arriendo registrado correctamente');
                    }}
                />
            )}
        </Layout>
    );
}

export default AsesorDashboard;
