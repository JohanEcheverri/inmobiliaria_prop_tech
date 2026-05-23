import { useState } from 'react';
import axiosDirect from 'axios';
import { useNavigate } from 'react-router-dom';
import './App.css';
import Layout from './components/Layout';

function Register() {
    const navigate = useNavigate();
    const [showPassword, setShowPassword] = useState(false);
    const [error, setError] = useState('');

    // Estado unificado usando exactamente la estructura de clientes de tu AdminModal
    const [formData, setFormData] = useState({
        identificacion: '',
        nombre: '',
        email: '',
        password: '',
        telefono: '',
        fotoPerfil: null
    });

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleFileChange = (e) => {
        const file = e.target.files[0];
        if (file) {
            const reader = new FileReader();

            // Cuando el archivo se termine de leer, se ejecuta este callback
            reader.onloadend = () => {
                setFormData(prev => ({
                    ...prev,
                    fotoPerfil: reader.result // Guarda el string completo de la imagen en Base64
                }));
            };

            // Lee el archivo binario y lo transforma a un Data URL de texto plano
            reader.readAsDataURL(file);
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');

        // Payload idéntico al bloque 'clientes' de tu AdminModal
        const payload = {
            id: formData.identificacion,
            nombre: formData.nombre,
            email: formData.email,
            telefono: formData.telefono,
            password: formData.password,
            fotoPerfil: formData.fotoPerfil || null, // Enviará la cadena de texto Base64 permanente
            tipoCliente: 'COMPRADOR',
            zonaInteres: 'CENTRO',
            presupuesto: null,
            tipoInmuebleDeseado: null,
            numeroHabitacionesDeseadas: 0,
            estadoBusqueda: 'BUSCANDO'
        };

        try {
            // Envío con axios tradicional (JSON) tal como lo hace el Modal
            await axiosDirect.post('http://localhost:8080/api/clientes', payload);
            navigate('/login');
        } catch (err) {
            console.error("Error al registrar cliente:", err);
            if (err.response) {
                setError(err.response.data.error || 'No se pudo crear la cuenta');
            } else {
                setError('No se pudo conectar con el servidor backend');
            }
        }
    };

    return (
        <Layout contentClassName="auth-container">
            <section className="auth-card register-card">
                <h2>Crear Cuenta</h2>
                <p>Registra tus datos para iniciar tu búsqueda inmobiliaria.</p>

                <form onSubmit={handleSubmit} className="auth-form">

                    {/* Sección Superior: Contenedor de Foto de Perfil basado en la UI del Modal */}
                    <div className="photo-profile-container">
                        <span className="photo-title">Foto de Perfil</span>

                        <div className="avatar-picker-container">
                            {/* Un div plano para evitar que haga click en la bolita */}
                            <div className="avatar-picker-preview">
                                {formData.fotoPerfil ? (
                                    <img src={formData.fotoPerfil} alt="Previsualización" />
                                ) : (
                                    <span className="avatar-plus-icon">+</span>
                                )}
                            </div>

                            <input
                                type="file"
                                accept="image/*"
                                id="avatar-file-input"
                                onChange={handleFileChange}
                                style={{ display: 'none' }}
                            />
                            {/* El label mapeado por ID que actúa como único disparador real */}
                            <label htmlFor="avatar-file-input" className="btn-select-avatar-file">
                                {formData.fotoPerfil ? 'Cambiar foto' : 'Subir foto'}
                            </label>
                        </div>
                    </div>

                    {/* Sección Inferior: Campos de Texto usando handleInputChange */}
                    <div className="form-grid">

                        <div className="input-group">
                            <label htmlFor="identificacion">Identificación</label>
                            <input
                                id="identificacion"
                                name="identificacion"
                                type="text"
                                value={formData.identificacion}
                                onChange={handleInputChange}
                                required
                            />
                        </div>

                        <div className="input-group">
                            <label htmlFor="nombre">Nombre Completo</label>
                            <input
                                id="nombre"
                                name="nombre"
                                type="text"
                                value={formData.nombre}
                                onChange={handleInputChange}
                                required
                            />
                        </div>

                        <div className="input-group">
                            <label htmlFor="email">Correo Electrónico</label>
                            <input
                                id="email"
                                name="email"
                                type="email"
                                value={formData.email}
                                onChange={handleInputChange}
                                required
                            />
                        </div>

                        <div className="input-group">
                            <label htmlFor="telefono">Teléfono Móvil</label>
                            <input
                                id="telefono"
                                name="telefono"
                                type="tel"
                                value={formData.telefono}
                                onChange={handleInputChange}
                                required
                            />
                        </div>

                        <div className="input-group password-section">
                            <label htmlFor="password">Contraseña</label>
                            <div className="password-wrapper">
                                <input
                                    id="password"
                                    name="password"
                                    type={showPassword ? 'text' : 'password'}
                                    value={formData.password}
                                    onChange={handleInputChange}
                                    placeholder="••••••••"
                                    required
                                />
                                <button
                                    type="button"
                                    className="toggle-password"
                                    onClick={() => setShowPassword(!showPassword)}
                                    aria-label="Mostrar u ocultar contraseña"
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
                    </div>

                    {error && <div className="error-message">{error}</div>}

                    <div className="layout-actions">
                        <button type="submit" className="btn-create">Crear Cuenta</button>
                    </div>

                    <button type="button" className="auth-link-button" onClick={() => navigate('/login')}>
                        Ya tengo cuenta
                    </button>
                </form>
            </section>
        </Layout>
    );
}

export default Register;
