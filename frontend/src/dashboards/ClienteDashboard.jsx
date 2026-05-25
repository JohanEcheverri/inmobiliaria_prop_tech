import { useCallback, useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Layout from '../components/Layout';
import './RoleDashboard.css';
import SearchBar from '../components/SearchBar';
import FirstLoginModal from '../components/FirstLoginModal';
import MisProperties from '../components/MisProperties';

const API_BASE_URL = 'http://localhost:8080/api';

const formatCurrency = (value) => new Intl.NumberFormat('es-CO', {
    style: 'currency',
    currency: 'COP',
    maximumFractionDigits: 0
}).format(Number(value || 0));

const today = () => new Date().toISOString().slice(0, 10);

function ClienteDashboard() {
    const navigate = useNavigate();
    const [session, setSession] = useState(null);
    const [seccionActiva, setSeccionActiva] = useState('catalogo');
    const [propiedades, setPropiedades] = useState([]);
    const [showFirstLoginModal, setShowFirstLoginModal] = useState(false);    const [inmuebles, setInmuebles] = useState([]);
    const [visitas, setVisitas] = useState([]);
    const [historial, setHistorial] = useState([]);
    const [selectedInmueble, setSelectedInmueble] = useState(null);
    const [visitForm, setVisitForm] = useState({ fecha: today(), hora: '09:00', observaciones: '' });
    const [statusMessage, setStatusMessage] = useState('');
    const [showFavoritesOnly, setShowFavoritesOnly] = useState(false);
    const [carouselIndex, setCarouselIndex] = useState(0);

    const fetchInmuebles = useCallback(async (filters = {}) => {
        // If filters provided, call search endpoint
        try {
            let url = `${API_BASE_URL}/inmuebles`;
            const hasFilters = Object.keys(filters).some(k => filters[k] !== undefined && filters[k] !== null && filters[k] !== '');
            if (hasFilters) {
                const params = new URLSearchParams();
                if (filters.zona) params.append('zona', filters.zona);
                if (filters.tipo) params.append('tipo', filters.tipo);
                if (filters.minPrecio) params.append('minPrecio', filters.minPrecio);
                if (filters.maxPrecio) params.append('maxPrecio', filters.maxPrecio);
                if (filters.finalidad) params.append('finalidad', filters.finalidad);
                if (filters.minHabitaciones) params.append('minHabitaciones', filters.minHabitaciones);
                if (filters.maxHabitaciones) params.append('maxHabitaciones', filters.maxHabitaciones);
                if (filters.clienteId) params.append('clienteId', filters.clienteId);
                url = `${API_BASE_URL}/inmuebles/search?${params.toString()}`;
            } else {
                url = `${API_BASE_URL}/inmuebles`;
            }
            const response = await fetch(url);
            if (!response.ok) throw new Error('No se pudo cargar el catalogo');
            let data = await response.json();
            // Fallback client-side filter for finalidad in case backend doesn't support it yet
            if (filters.finalidad && !url.includes('finalidad=')) {
                data = data.filter(i => String(i.finalidad) === String(filters.finalidad));
            }
            setInmuebles(data);
        } catch (error) {
            throw error;
        }
    }, []);

    const fetchVisitas = useCallback(async (clienteId) => {
        const response = await fetch(`${API_BASE_URL}/visitas/cliente/${clienteId}`);
        if (!response.ok) throw new Error('No se pudieron cargar las visitas');
        setVisitas(await response.json());
    }, []);

    const fetchPropiedades = useCallback(async (clienteId) => {
        try {
            const response = await fetch(`${API_BASE_URL}/operaciones/cliente/${clienteId}/propiedades`);
            if (!response.ok) {
                setPropiedades([]);
                return;
            }
            setPropiedades(await response.json());
        } catch (e) {
            setPropiedades([]);
        }
    }, []);

    const fetchHistorial = useCallback(async (clienteId) => {
        const response = await fetch(`${API_BASE_URL}/historial/cliente/${clienteId}`);
        if (!response.ok) throw new Error('No se pudo cargar el historial');
        setHistorial(await response.json());
    }, []);

    const cargarDatos = useCallback(async (clienteId) => {
        try {
            await Promise.all([fetchInmuebles(), fetchVisitas(clienteId), fetchHistorial(clienteId), fetchPropiedades(clienteId)]);
            // Si es el primer inicio, mostrar modal
            const resp = await fetch(`${API_BASE_URL}/clientes/${clienteId}`);
            if (resp.ok) {
                const cliente = await resp.json();
                if (cliente && cliente.primerInicioCompletado === false) {
                    setShowFirstLoginModal(true);
                }
            }
        } catch (error) {
            setStatusMessage(error.message);
        }
    }, [fetchHistorial, fetchInmuebles, fetchVisitas]);

    useEffect(() => {
        const savedSession = JSON.parse(localStorage.getItem('user_session'));
        if (!savedSession || savedSession.rol !== 'CLIENTE') {
            navigate('/login');
            return;
        }
        setSession(savedSession);
        cargarDatos(savedSession.id);
    }, [cargarDatos, navigate]);

    // First-login modal handlers
    const handleCloseFirstLogin = () => setShowFirstLoginModal(false);
    const handleSavedPreferences = async () => {
        // reload data and recommendations
        if (session?.id) {
            await cargarDatos(session.id);
        }
    };

    useEffect(() => {
        if (!statusMessage) {
            return undefined;
        }
        const timer = window.setTimeout(() => setStatusMessage(''), 3200);
        return () => window.clearTimeout(timer);
    }, [statusMessage]);

    const favoritos = useMemo(() => {
        // Agrupar eventos por inmueble y obtener el último evento de cada uno
        const ultimoEventoPorInmueble = {};
        for (let i = 0; i < historial.length; i++) {
            const evento = historial[i];
            if (!ultimoEventoPorInmueble[evento.inmuebleCodigo] || 
                new Date(evento.fechaEvento) > new Date(ultimoEventoPorInmueble[evento.inmuebleCodigo].fechaEvento)) {
                ultimoEventoPorInmueble[evento.inmuebleCodigo] = evento;
            }
        }
        
        // Un inmueble es favorito si su último evento es FAVORITO
        const codigos = new Set(
            Object.values(ultimoEventoPorInmueble)
                .filter(evento => evento.tipoEvento === 'FAVORITO')
                .map(evento => evento.inmuebleCodigo)
        );
        return inmuebles.filter(inmueble => codigos.has(inmueble.codigo));
    }, [historial, inmuebles]);

    const favoriteCodes = useMemo(() => new Set(favoritos.map(inmueble => inmueble.codigo)), [favoritos]);

    // localFavorites mantiene estado optimista y sincroniza con historial
    const [localFavorites, setLocalFavorites] = useState(new Set());

    // Mantener localFavorites sincronizado cuando cambie el historial o favoritos reales
    useEffect(() => {
        setLocalFavorites(new Set(favoritos.map(inm => inm.codigo)));
    }, [favoritos]);

    const catalogoVisible = useMemo(
        () => showFavoritesOnly ? inmuebles.filter(inmueble => favoriteCodes.has(inmueble.codigo)) : inmuebles,
        [favoriteCodes, inmuebles, showFavoritesOnly]
    );

    const getImages = (inmueble) => inmueble?.imagenes?.length ? inmueble.imagenes : (inmueble?.imagen ? [inmueble.imagen] : []);

    const toggleFavorito = (inmuebleCodigo) => {
        // Optimistic update of localFavorites so both buttons reflect change immediately
        setLocalFavorites(prev => {
            const next = new Set(prev);
            const isNowFavorito = !next.has(inmuebleCodigo);
            if (next.has(inmuebleCodigo)) next.delete(inmuebleCodigo); else next.add(inmuebleCodigo);
            // Send explicit event type based on the new optimistic state
            registrarEvento(inmuebleCodigo, isNowFavorito ? 'FAVORITO' : 'DESMARCADO', isNowFavorito ? 'Inmueble marcado como favorito' : 'Inmueble quitado de favoritos');
            return next;
        });
    };

    const registrarEvento = async (inmuebleCodigo, tipoEvento, mensaje) => {
        try {
            const eventoFinal = tipoEvento; // now explicit from caller
            const mensajeFinal = mensaje || (eventoFinal === 'FAVORITO' ? 'Inmueble marcado como favorito' : eventoFinal === 'DESMARCADO' ? 'Inmueble quitado de favoritos' : 'Interacción registrada');

            const response = await fetch(`${API_BASE_URL}/historial`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    clienteId: session.id,
                    inmuebleCodigo,
                    tipoEvento: eventoFinal
                })
            });

            if (!response.ok) throw new Error('No se pudo registrar la interaccion');

            await fetchHistorial(session.id);
            setStatusMessage(mensajeFinal);

        } catch (error) {
            setStatusMessage(error.message);
        }
    };

    const abrirDetalle = async (inmueble) => {
        setSelectedInmueble(inmueble);
        setCarouselIndex(0);
        setSeccionActiva('detalle');
        await registrarEvento(inmueble.codigo, 'CONSULTA', `Consulta registrada para ${inmueble.codigo}`);
    };

    const agendarVisita = async (event) => {
        event.preventDefault();
        if (!selectedInmueble?.asesorId) {
            setStatusMessage('Este inmueble no tiene asesor asignado para agendar visita.');
            return;
        }

        try {
            const response = await fetch(`${API_BASE_URL}/visitas`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    clienteId: session.id,
                    inmuebleCodigo: selectedInmueble.codigo,
                    asesorId: selectedInmueble.asesorId,
                    fecha: visitForm.fecha,
                    hora: visitForm.hora,
                    observaciones: visitForm.observaciones
                })
            });
            if (!response.ok) {
                const error = await response.json().catch(() => null);
                throw new Error(error?.error || 'No se pudo agendar la visita');
            }
            await Promise.all([fetchVisitas(session.id), fetchHistorial(session.id)]);
            setStatusMessage('Visita solicitada correctamente. Estado inicial: pendiente.');
            setSeccionActiva('visitas');
        } catch (error) {
            setStatusMessage(error.message);
        }
    };

    const cancelarVisita = async (codigo) => {
        try {
            const response = await fetch(`${API_BASE_URL}/visitas/${codigo}/cancelar`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ observaciones: 'Cancelada por el cliente desde el dashboard' })
            });
            if (!response.ok) throw new Error('No se pudo cancelar la visita');
            await fetchVisitas(session.id);
            setStatusMessage('Visita cancelada.');
        } catch (error) {
            setStatusMessage(error.message);
        }
    };

    const renderCatalogo = () => (
        <>
        <div className="catalog-filter-bar">
            <SearchBar onSearch={(filters) => fetchInmuebles(filters)} initialClienteId={session?.id} />
            <button className={showFavoritesOnly ? 'active' : ''} onClick={() => setShowFavoritesOnly(prev => !prev)}>
                {showFavoritesOnly ? 'Ver todo el catalogo' : 'Filtrar favoritos'}
            </button>
        </div>
        <div className="property-grid">
            {catalogoVisible.map(inmueble => (
                <article className="property-card" key={inmueble.codigo}>
                    <button
                        className={`favorite-star ${localFavorites.has(inmueble.codigo) ? 'active' : ''}`}
                        onClick={() => toggleFavorito(inmueble.codigo)}
                        aria-label={localFavorites.has(inmueble.codigo) ? "Quitar de favoritos" : "Marcar favorito"}
                    >
                        ★
                    </button>
                    <div className="property-thumb">
                        {getImages(inmueble)[0] ? <img src={getImages(inmueble)[0]} alt={inmueble.direccionBarrio || inmueble.direccion} /> : <span>{inmueble.tipoInmueble}</span>}
                    </div>
                    <div className="property-body">
                        <div className="property-title-row">
                            <h3>{inmueble.direccionBarrio || inmueble.direccion}</h3>
                            <span className={`status-pill ${String(inmueble.estado || '').toLowerCase()}`}>{inmueble.estado}</span>
                        </div>
                        <p>{inmueble.ciudad} {inmueble.departamento ? `(${inmueble.departamento})` : ''} · {inmueble.zona}</p>
                        <strong>{formatCurrency(inmueble.precio)}</strong>
                        <div className="property-meta">
                            <span>{inmueble.tipoInmueble}</span>
                            <span>{inmueble.finalidad}</span>
                            <span>{inmueble.area} m2</span>
                            <span>{inmueble.habitaciones} hab.</span>
                        </div>
                    </div>
                    <div className="card-actions">
                        <button onClick={() => abrirDetalle(inmueble)}>Ver detalle</button>
                        <button onClick={() => registrarEvento(inmueble.codigo, 'DESCARTADO', 'Inmueble descartado')}>Descartar</button>
                    </div>
                </article>
            ))}
        </div>
        </>
    );

    const renderDetalle = () => {
        if (!selectedInmueble) {
            return null;
        }
        const images = getImages(selectedInmueble);
        const currentImage = images[carouselIndex] || null;

        return (
        <div className="detail-grid">
            <section className="dashboard-panel">
                <div className="property-carousel">
                    {currentImage ? <img src={currentImage} alt={selectedInmueble.direccionBarrio || selectedInmueble.direccion} /> : <div className="carousel-empty">Sin imágenes</div>}
                    {images.length > 1 && (
                        <div className="carousel-controls">
                            <button onClick={() => setCarouselIndex(prev => (prev - 1 + images.length) % images.length)}>‹</button>
                            <span>{carouselIndex + 1} / {images.length}</span>
                            <button onClick={() => setCarouselIndex(prev => (prev + 1) % images.length)}>›</button>
                        </div>
                    )}
                </div>
                <h2>{selectedInmueble.direccionBarrio || selectedInmueble.direccion}</h2>
                <p>{selectedInmueble.ciudad} {selectedInmueble.departamento ? `(${selectedInmueble.departamento})` : ''} · {selectedInmueble.zona}</p>
                <div className="metrics-grid compact">
                    <div><span>Precio</span><strong>{formatCurrency(selectedInmueble.precio)}</strong></div>
                    <div><span>Finalidad</span><strong>{selectedInmueble.finalidad}</strong></div>
                    <div><span>Asesor</span><strong>{selectedInmueble.asesorResponsable || 'Sin asignar'}</strong></div>
                    <div><span>Estado</span><strong>{selectedInmueble.estado}</strong></div>
                </div>
                <div className="inline-actions">
                    <button onClick={() => toggleFavorito(selectedInmueble.codigo)}>
                        {localFavorites.has(selectedInmueble.codigo) ? 'Quitar de favoritos' : 'Marcar favorito'}
                    </button>
                    <button onClick={() => registrarEvento(selectedInmueble.codigo, 'NEGOCIANDO', 'Intencion de compra/arriendo registrada')}>Registrar intención</button>
                </div>
            </section>
            <section className="dashboard-panel">
                <h2>Agendar visita</h2>
                <form className="dashboard-form" onSubmit={agendarVisita}>
                    <label>Fecha<input type="date" min={today()} value={visitForm.fecha} onChange={(e) => setVisitForm(prev => ({ ...prev, fecha: e.target.value }))} required /></label>
                    <label>Hora<input type="time" value={visitForm.hora} onChange={(e) => setVisitForm(prev => ({ ...prev, hora: e.target.value }))} required /></label>
                    <label>Observaciones<textarea value={visitForm.observaciones} onChange={(e) => setVisitForm(prev => ({ ...prev, observaciones: e.target.value }))} /></label>
                    <button type="submit">Solicitar visita</button>
                </form>
            </section>
        </div>
        );
    };

    const renderVisitas = () => (
        <div className="table-responsive-wrapper">
            <table className="domustech-admin-table">
                <thead><tr><th>Inmueble</th><th>Fecha</th><th>Hora</th><th>Asesor</th><th>Estado</th><th>Acciones</th></tr></thead>
                <tbody>
                    {visitas.length === 0 ? <tr><td colSpan="6" className="table-empty-row">No tienes visitas agendadas.</td></tr> : visitas.map(visita => (
                        <tr key={visita.codigo}>
                            <td>{visita.inmuebleDireccion}<br /><small>{visita.inmuebleCodigo}</small></td>
                            <td>{visita.fecha}</td>
                            <td>{visita.hora}</td>
                            <td>{visita.asesorNombre || visita.asesorId}</td>
                            <td><span className={`status-pill ${String(visita.estado).toLowerCase()}`}>{visita.estado}</span></td>
                            <td>{visita.estado !== 'CANCELADA' && visita.estado !== 'REALIZADA' && <button className="table-action-button" onClick={() => cancelarVisita(visita.codigo)}>Cancelar</button>}</td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );

    const renderHistorial = () => (
        <div className="timeline-list">
            {historial.length === 0 ? <p className="empty-text">Aun no hay interacciones registradas.</p> : historial.map(evento => (
                <article className="timeline-item" key={evento.id}>
                    <span className={`status-pill ${String(evento.tipoEvento).toLowerCase()}`}>{evento.tipoEvento}</span>
                    <div><strong>{evento.inmuebleDireccion || evento.inmuebleCodigo}</strong><p>{new Date(evento.fechaEvento).toLocaleString('es-CO')}</p></div>
                </article>
            ))}
        </div>
    );

    return (
        <Layout contentClassName="admin-layout-container">
            <div className="dashboard-wrapper">
                <aside className="dashboard-sidebar">
                    <div className="sidebar-header"><h3>Cliente Menú</h3></div>
                    <nav className="sidebar-nav">
                        <button className={`nav-item ${seccionActiva === 'catalogo' ? 'active' : ''}`} onClick={() => setSeccionActiva('catalogo')}>Catalogo completo</button>
                        <button className={`nav-item ${seccionActiva === 'visitas' ? 'active' : ''}`} onClick={() => setSeccionActiva('visitas')}>Mis visitas</button>
                        <button className={`nav-item ${seccionActiva === 'mispropiedades' ? 'active' : ''}`} onClick={() => setSeccionActiva('mispropiedades')}>Mis propiedades</button>
                        <button className={`nav-item ${seccionActiva === 'historial' ? 'active' : ''}`} onClick={() => setSeccionActiva('historial')}>Historial</button>
                    </nav>
                </aside>

                <main className="dashboard-content">
                    <div className="content-view-header">
                        <div>
                            <h1>{seccionActiva === 'catalogo' ? 'Catalogo de inmuebles' : seccionActiva === 'detalle' ? 'Detalle del inmueble' : seccionActiva === 'visitas' ? 'Visitas agendadas' : seccionActiva === 'mispropiedades' ? 'Mis propiedades' : 'Historial de interacciones'}</h1>
                            <p>{session?.nombre}, gestiona tu busqueda inmobiliaria desde un solo lugar.</p>
                        </div>
                    </div>
                    {statusMessage && <div className="dashboard-alert">{statusMessage}</div>}
                    {seccionActiva === 'catalogo' && renderCatalogo()}
                    {seccionActiva === 'detalle' && renderDetalle()}
                    {seccionActiva === 'visitas' && renderVisitas()}
                    {seccionActiva === 'mispropiedades' && <MisProperties propiedades={propiedades} onVerDetalle={(codigo) => {
                        const found = inmuebles.find(i => i.codigo === codigo);
                        if (found) abrirDetalle(found);
                    }} />}
                    {seccionActiva === 'favoritos' && (favoritos.length ? <div className="property-grid">{favoritos.map(inmueble => <article className="property-card" key={inmueble.codigo}><button className={`favorite-star active`} onClick={() => toggleFavorito(inmueble.codigo)} aria-label="Quitar de favoritos">★</button><div className="property-thumb">{getImages(inmueble)[0] ? <img src={getImages(inmueble)[0]} alt={inmueble.direccionBarrio || inmueble.direccion} /> : <span>{inmueble.tipoInmueble}</span>}</div><div className="property-body"><h3>{inmueble.direccionBarrio || inmueble.direccion}</h3><p>{inmueble.ciudad} · {inmueble.zona}</p><strong>{formatCurrency(inmueble.precio)}</strong></div><div className="card-actions"><button onClick={() => abrirDetalle(inmueble)}>Ver detalle</button></div></article>)}</div> : <p className="empty-text">No tienes favoritos registrados.</p>)}
                    {seccionActiva === 'historial' && renderHistorial()}
                {showFirstLoginModal && session?.id && <FirstLoginModal clienteId={session.id} onClose={handleCloseFirstLogin} onSaved={handleSavedPreferences} />}
                </main>
            </div>
        </Layout>
    );
}

export default ClienteDashboard;
