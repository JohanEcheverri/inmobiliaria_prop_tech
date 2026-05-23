import { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Layout from '../components/Layout';
import AdminTables from '../components/AdminTables';
import AdminModal from '../components/AdminModal';
import './AdminDashboard.css';

const API_BASE_URL = "http://localhost:8080/api"; // Centralizamos la URL del backend

function AdminDashboard() {
    const navigate = useNavigate();
    const [seccionActiva, setSeccionActiva] = useState('inmuebles');
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [elementoAEditar, setElementoAEditar] = useState(null);

    const [inmuebles, setInmuebles] = useState([]);
    const [clientes, setClientes] = useState([]);
    const [asesores, setAsesores] = useState([]);


    // ==========================================================================
    // PETICIONES HTTP (FETCH ASYNC/AWAIT)
    // ==========================================================================

    const fetchClientes = useCallback(async () => {
        try {
            const response = await fetch(`${API_BASE_URL}/clientes`);
            if (!response.ok) throw new Error("Error al obtener el listado de clientes");
            const data = await response.json();
            setClientes(data); // Setea el array de ClienteResponse en el estado
        } catch (error) {
            console.error("Error en fetchClientes:", error);
        }
    }, []);

    const fetchAsesores = useCallback(async () => {
        try {
            const response = await fetch(`${API_BASE_URL}/asesores`);
            if (!response.ok) throw new Error("Error al obtener el listado de asesores");
            const data = await response.json();
            setAsesores(data); // Setea el array de AsesorResponse en el estado
        } catch (error) {
            console.error("Error en fetchAsesores:", error);
        }
    }, []);

    const fetchInmuebles = useCallback(async () => {
        try {
            const response = await fetch(`${API_BASE_URL}/inmuebles`);
            if (!response.ok) throw new Error("Error al obtener el listado de inmuebles");
            const data = await response.json();
            setInmuebles(data);
        } catch (error) {
            console.error("Error en fetchInmuebles:", error);
        }
    }, []);

    useEffect(() => {
        const session = JSON.parse(localStorage.getItem("user_session"));
        if (!session || session.rol !== 'ADMINISTRADOR') {
            navigate('/login');
        } else {
            const timer = window.setTimeout(() => {
                fetchClientes();
                fetchInmuebles();
                fetchAsesores();
            }, 0);
            return () => window.clearTimeout(timer);
        }
    }, [fetchAsesores, fetchClientes, fetchInmuebles, navigate]);

    const handleDelete = async (id, tipo) => {
        if (window.confirm(`¿Seguro que deseas eliminar permanentemente este registro en ${tipo}?`)) {
            try {
                const response = await fetch(`${API_BASE_URL}/${tipo}/${id}`, {
                    method: 'DELETE'
                });

                if (!response.ok) {
                    throw new Error(`No se pudo eliminar el registro con ID ${id}`);
                }

                // Sincronización inmediata del estado de React según la sección
                if (tipo === 'clientes') {
                    // Comparamos contra item.id (que mapea la cédula del DTO)
                    setClientes(prev => prev.filter(item => item.id !== id));
                } else if (tipo === 'inmuebles') {
                    setInmuebles(prev => prev.filter(item => item.codigo !== id));
                } else if (tipo === 'asesores') {
                    setAsesores(prev => prev.filter(item => item.id !== id));
                }

                alert("Registro eliminado correctamente");
            } catch (error) {
                alert(`Error al eliminar: ${error.message}`);
            }
        }
    };

    // ==========================================================================
    // CONTROL DE MODALES Y SELECCIÓN DE ESTADO
    // ==========================================================================
    const abrirModalRegistro = () => { setElementoAEditar(null); setIsModalOpen(true); };
    const abrirModalEdicion = (elemento) => { setElementoAEditar(elemento); setIsModalOpen(true); };

    const obtenerColeccionActiva = () => {
        if (seccionActiva === 'inmuebles') return inmuebles;
        if (seccionActiva === 'clientes') return clientes;
        return asesores;
    };

    const handleModalSuccess = () => {
        setIsModalOpen(false);
        if (seccionActiva === 'clientes') fetchClientes();
        if (seccionActiva === 'asesores') fetchAsesores(); // <--- AGREGAR ESTA LÍNEA
        if (seccionActiva === 'inmuebles') fetchInmuebles();
    };

    return (
        <Layout contentClassName="admin-layout-container">
            <div className="dashboard-wrapper">

                {/* SIDEBAR REFINADO CON ICONOS VECTORIALES SVG COHERENTES */}
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
                    </nav>
                </aside>

                <main className="dashboard-content">
                    <div className="content-view-header">
                        <div>
                            <h1>
                                {seccionActiva === 'inmuebles' && 'Catálogo de Inmuebles'}
                                {seccionActiva === 'clientes' && 'Control de Clientes'}
                                {seccionActiva === 'asesores' && 'Equipo de Asesores'}
                            </h1>
                            <p>Conexión directa con ecosistema base de datos DomusTech.</p>
                        </div>
                        <button className="btn-add-registry" onClick={abrirModalRegistro}>
                            + Registrar {seccionActiva === 'inmuebles' ? 'Inmueble' : seccionActiva === 'clientes' ? 'Cliente' : 'Asesor'}
                        </button>
                    </div>

                    <div className="table-responsive-wrapper">
                        <AdminTables
                            seccion={seccionActiva}
                            data={obtenerColeccionActiva()}
                            onEdit={abrirModalEdicion}
                            onDelete={handleDelete}
                        />
                    </div>
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
