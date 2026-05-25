import { useEffect, useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import Layout from './components/Layout';
import './ConfiguracionCuenta.css';
import { apiUrl } from './api';

const clienteInicial = {
    id: '',
    nombre: '',
    email: '',
    telefono: '',
    password: '',
    fotoPerfil: null,
    tipoCliente: 'COMPRADOR',
    zonaInteres: 'CENTRO',
    presupuesto: '',
    tipoInmuebleDeseado: '',
    numeroHabitacionesDeseadas: 0,
    estadoBusqueda: 'BUSCANDO'
};

function ConfiguracionCuenta() {
    const navigate = useNavigate();
    const [formData, setFormData] = useState(clienteInicial);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const [showPassword, setShowPassword] = useState(false);

    useEffect(() => {
        const session = JSON.parse(localStorage.getItem('user_session'));

        if (!session || session.rol !== 'CLIENTE') {
            navigate('/login');
            return;
        }

        const cargarCliente = async () => {
            setLoading(true);
            setError('');

            try {
                const response = await axios.get(apiUrl(`/clientes/${session.id}`));
                const cliente = response.data;
                setFormData({
                    ...clienteInicial,
                    ...cliente,
                    password: '',
                    presupuesto: cliente.presupuesto ?? '',
                    tipoInmuebleDeseado: cliente.tipoInmuebleDeseado ?? '',
                    numeroHabitacionesDeseadas: cliente.numeroHabitacionesDeseadas ?? 0
                });
            } catch (err) {
                setError(err.response?.data?.message || err.response?.data?.error || 'No se pudieron cargar tus datos.');
            } finally {
                setLoading(false);
            }
        };

        cargarCliente();
    }, [navigate]);

    const handleInputChange = (event) => {
        const { name, value } = event.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleFileChange = (event) => {
        const file = event.target.files[0];
        if (!file) {
            return;
        }

        const reader = new FileReader();
        reader.onloadend = () => {
            setFormData(prev => ({ ...prev, fotoPerfil: reader.result }));
        };
        reader.readAsDataURL(file);
    };

    const handleSubmit = async (event) => {
        event.preventDefault();
        setSaving(true);
        setError('');
        setSuccess('');

        const payload = {
            id: formData.id,
            nombre: formData.nombre,
            email: formData.email,
            telefono: formData.telefono,
            password: formData.password.trim() || null,
            fotoPerfil: formData.fotoPerfil || null,
            tipoCliente: formData.tipoCliente || 'COMPRADOR',
            zonaInteres: formData.zonaInteres || null,
            presupuesto: formData.presupuesto === '' ? null : Number(formData.presupuesto),
            tipoInmuebleDeseado: formData.tipoInmuebleDeseado || null,
            numeroHabitacionesDeseadas: Number(formData.numeroHabitacionesDeseadas) || 0,
            estadoBusqueda: formData.estadoBusqueda || 'BUSCANDO'
        };

        try {
            const response = await axios.put(apiUrl(`/clientes/${formData.id}`), payload);
            const session = JSON.parse(localStorage.getItem('user_session')) || {};
            const updatedSession = {
                ...session,
                id: response.data.id,
                nombre: response.data.nombre,
                email: response.data.email,
                telefono: response.data.telefono,
                fotoPerfil: response.data.fotoPerfil,
                rol: session.rol || 'CLIENTE'
            };

            localStorage.setItem('user_session', JSON.stringify(updatedSession));
            window.dispatchEvent(new Event('user_session_updated'));
            setFormData(prev => ({ ...prev, ...response.data, password: '', presupuesto: response.data.presupuesto ?? '' }));
            setSuccess('Datos actualizados correctamente.');
        } catch (err) {
            setError(err.response?.data?.message || err.response?.data?.error || 'No se pudieron actualizar tus datos.');
        } finally {
            setSaving(false);
        }
    };

    if (loading) {
        return (
            <Layout contentClassName="account-page">
                <section className="account-shell">
                    <p className="account-status">Cargando datos de la cuenta...</p>
                </section>
            </Layout>
        );
    }

    return (
        <Layout contentClassName="account-page">
            <section className="account-shell">
                <div className="account-header">
                    <div>
                        <p className="account-kicker">Mi cuenta</p>
                        <h1>Editar datos del cliente</h1>
                    </div>
                    <button type="button" className="account-back-button" onClick={() => navigate('/cliente-dashboard')}>
                        Volver al panel
                    </button>
                </div>

                <form className="account-form" onSubmit={handleSubmit}>
                    <aside className="account-photo-panel">
                        <div className="account-avatar">
                            {formData.fotoPerfil ? (
                                <img src={formData.fotoPerfil} alt="Foto de perfil" />
                            ) : (
                                <span>{formData.nombre ? formData.nombre.charAt(0).toUpperCase() : 'C'}</span>
                            )}
                        </div>

                        <input
                            type="file"
                            accept="image/*"
                            id="account-photo-input"
                            onChange={handleFileChange}
                        />
                        <label htmlFor="account-photo-input" className="account-photo-button">
                            Editar foto
                        </label>
                    </aside>

                    <div className="account-fields">
                        <div className="input-group">
                            <label htmlFor="id">Identificación</label>
                            <input id="id" name="id" type="text" value={formData.id} disabled />
                        </div>

                        <div className="input-group">
                            <label htmlFor="nombre">Nombre completo</label>
                            <input id="nombre" name="nombre" type="text" value={formData.nombre} onChange={handleInputChange} required />
                        </div>

                        <div className="input-group">
                            <label htmlFor="email">Correo electrónico</label>
                            <input id="email" name="email" type="email" value={formData.email} onChange={handleInputChange} required />
                        </div>

                        <div className="input-group">
                            <label htmlFor="telefono">Teléfono móvil</label>
                            <input id="telefono" name="telefono" type="tel" value={formData.telefono} onChange={handleInputChange} required />
                        </div>

                        <div className="input-group">
                            <label htmlFor="password">Nueva contraseña</label>
                            <div className="password-wrapper">
                                <input
                                    id="password"
                                    name="password"
                                    type={showPassword ? 'text' : 'password'}
                                    value={formData.password}
                                    onChange={handleInputChange}
                                    placeholder="Opcional"
                                />
                                <button
                                    type="button"
                                    className="toggle-password"
                                    onClick={() => setShowPassword(prev => !prev)}
                                    aria-label={showPassword ? 'Ocultar contraseña' : 'Mostrar contraseña'}
                                >
                                    <svg className="eye-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                                        {showPassword ? (
                                            <>
                                                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                                                <circle cx="12" cy="12" r="3" />
                                            </>
                                        ) : (
                                            <>
                                                <path d="M17.94 17.94A10.07 10.07 0 0 1 12 19c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24" />
                                                <line x1="1" y1="1" x2="23" y2="23" />
                                            </>
                                        )}
                                    </svg>
                                </button>
                            </div>
                        </div>

                        <div className="input-group">
                            <label htmlFor="tipoCliente">Tipo de cliente</label>
                            <select id="tipoCliente" name="tipoCliente" value={formData.tipoCliente} onChange={handleInputChange}>
                                <option value="COMPRADOR">Comprador</option>
                                <option value="ARRENDATARIO">Arrendatario</option>
                            </select>
                        </div>

                        <div className="input-group">
                            <label htmlFor="zonaInteres">Zona de interés</label>
                            <select id="zonaInteres" name="zonaInteres" value={formData.zonaInteres || ''} onChange={handleInputChange}>
                                <option value="">Sin definir</option>
                                <option value="NORTE">Norte</option>
                                <option value="SUR">Sur</option>
                                <option value="ESTE">Este</option>
                                <option value="OESTE">Oeste</option>
                                <option value="CENTRO">Centro</option>
                            </select>
                        </div>

                        <div className="input-group">
                            <label htmlFor="presupuesto">Presupuesto</label>
                            <input id="presupuesto" name="presupuesto" type="number" min="0" value={formData.presupuesto} onChange={handleInputChange} />
                        </div>

                        <div className="input-group">
                            <label htmlFor="tipoInmuebleDeseado">Tipo de inmueble deseado</label>
                            <select id="tipoInmuebleDeseado" name="tipoInmuebleDeseado" value={formData.tipoInmuebleDeseado || ''} onChange={handleInputChange}>
                                <option value="">Sin definir</option>
                                <option value="CASA">Casa</option>
                                <option value="APARTAMENTO">Apartamento</option>
                                <option value="LOCAL_COMERCIAL">Local comercial</option>
                                <option value="OFICINA">Oficina</option>
                                <option value="LOTE">Lote</option>
                                <option value="BODEGA">Bodega</option>
                            </select>
                        </div>

                        <div className="input-group">
                            <label htmlFor="numeroHabitacionesDeseadas">Habitaciones deseadas</label>
                            <input
                                id="numeroHabitacionesDeseadas"
                                name="numeroHabitacionesDeseadas"
                                type="number"
                                min="0"
                                value={formData.numeroHabitacionesDeseadas}
                                onChange={handleInputChange}
                            />
                        </div>

                        <div className="input-group">
                            <label htmlFor="estadoBusqueda">Estado de búsqueda</label>
                            <select id="estadoBusqueda" name="estadoBusqueda" value={formData.estadoBusqueda} onChange={handleInputChange}>
                                <option value="BUSCANDO">Buscando</option>
                                <option value="NEGOCIANDO">Negociando</option>
                                <option value="CERRADO">Cerrado</option>
                            </select>
                        </div>
                    </div>

                    {error && <div className="error-message account-message">{error}</div>}
                    {success && <div className="success-message account-message">{success}</div>}

                    <div className="account-actions">
                        <button type="button" className="account-secondary-button" onClick={() => navigate('/cliente-dashboard')}>
                            Cancelar
                        </button>
                        <button type="submit" className="account-primary-button" disabled={saving}>
                            {saving ? 'Guardando...' : 'Guardar cambios'}
                        </button>
                    </div>
                </form>
            </section>
        </Layout>
    );
}

export default ConfiguracionCuenta;
