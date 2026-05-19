import { useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import './App.css'; // Asegúrate de que el CSS esté vinculado
import Layout from './components/Layout';

function Register() {
    const navigate = useNavigate();
    const [formData, setFormData] = useState({
        nombre: '',
        identificacion: '',
        email: '',
        password: '',
        telefono: ''
    });
    const [error, setError] = useState('');


    const [selectedFile, setSelectedFile] = useState(null);
    const [previewImage, setPreviewImage] = useState(null);

    const handleChange = (event) => {
        const { name, value } = event.target;
        setFormData((currentData) => ({
            ...currentData,
            [name]: value
        }));
    };

    const handleFileChange = (event) => {
        const file = event.target.files[0];
        if (file) {
            setSelectedFile(file);
            setPreviewImage(URL.createObjectURL(file)); // Crea una URL temporal para la previsualización
        } else {
            setSelectedFile(null);
            setPreviewImage(null);
        }
    };

    const handleSubmit = async (event) => {
        event.preventDefault();
        setError('');

        try {

            await axios.post('http://localhost:8080/api/clientes', {
                id: formData.identificacion,
                nombre: formData.nombre,
                email: formData.email,
                telefono: formData.telefono,
                password: formData.password,
                fotoPerfil: null,
                tipoCliente: 'COMPRADOR',
                zonaInteres: 'CENTRO',
                presupuesto: null,
                tipoInmuebleDeseado: null,
                numeroHabitacionesDeseadas: 0,
                estadoBusqueda: 'BUSCANDO'
            });
            navigate('/login');
        } catch (err) {
            if (err.response) {
                setError(err.response.data.error || 'No se pudo crear la cuenta');
            } else {
                setError('No se pudo conectar con el servidor');
            }
        }
    };

    return (
        <Layout contentClassName="auth-container">
            <section className="auth-card register-card">
                <h2>Crear Cuenta</h2>
                <p>Registra tus datos para iniciar tu búsqueda inmobiliaria.</p>

                <form onSubmit={handleSubmit} className="auth-form">
                    <div className="form-grid">
                        {/* Campo 1: Nombre (Fila 1, Columna 1) */}
                        <div className="input-group">
                            <label htmlFor="nombre">Nombre completo</label>
                            <input id="nombre" name="nombre" type="text" value={formData.nombre} onChange={handleChange} required />
                        </div>

                        {/* Foto de Perfil (Fila 1, Columna 2) */}
                        <div className="input-group photo-column">
                            <label>Foto de perfil</label>
                            <label htmlFor="fotoPerfilInput" className={`photo-upload-label ${previewImage ? 'has-preview' : ''}`}>
                                {previewImage ? (
                                    <div className="avatar-preview">
                                        <img src={previewImage} alt="Previsualización" />
                                        <span>Cambiar foto</span>
                                    </div>
                                ) : (
                                    <div className="upload-placeholder">
                                        <span className="upload-icon">+</span>
                                        <span>Subir foto</span>
                                    </div>
                                )}
                            </label>
                            <input
                                id="fotoPerfilInput"
                                type="file"
                                accept="image/*"
                                onChange={handleFileChange}
                                style={{ display: 'none' }}
                            />
                        </div>

                        {/* Fila 2: Cédula y Email */}
                        <div className="input-group">
                            <label htmlFor="identificacion">Cédula / ID</label>
                            <input id="identificacion" name="identificacion" type="text" value={formData.identificacion} onChange={handleChange} required />
                        </div>

                        <div className="input-group">
                            <label htmlFor="email">Correo electrónico</label>
                            <input id="email" name="email" type="email" value={formData.email} onChange={handleChange} required />
                        </div>

                        {/* Fila 3: Teléfono y Contraseña */}
                        <div className="input-group">
                            <label htmlFor="telefono">Teléfono</label>
                            <input id="telefono" name="telefono" type="tel" value={formData.telefono} onChange={handleChange} required />
                        </div>

                        <div className="input-group">
                            <label htmlFor="password">Contraseña</label>
                            <input id="password" name="password" type="password" value={formData.password} onChange={handleChange} required />
                        </div>
                    </div>

                    <div className="layout-actions">
                        <button type="submit" className="btn-create">Crear Cuenta</button>
                    </div>
                    {error && <div className="error-message">{error}</div>}

                    <button type="button" className="auth-link-button" onClick={() => navigate('/login')}>
                        Ya tengo cuenta
                    </button>
                </form>

            </section>
        </Layout>
    );
}

export default Register;