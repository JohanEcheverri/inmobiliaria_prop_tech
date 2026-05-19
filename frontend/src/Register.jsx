import { useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import './App.css';
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

    const handleChange = (event) => {
        const { name, value } = event.target;
        setFormData((currentData) => ({
            ...currentData,
            [name]: value
        }));
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
                        <div className="input-group">
                            <label htmlFor="nombre">Nombre completo</label>
                            <input id="nombre" name="nombre" type="text" value={formData.nombre} onChange={handleChange} required />
                        </div>

                        <div className="input-group">
                            <label htmlFor="identificacion">Cédula / ID</label>
                            <input id="identificacion" name="identificacion" type="text" value={formData.identificacion} onChange={handleChange} required />
                        </div>

                        <div className="input-group">
                            <label htmlFor="email">Correo electrónico</label>
                            <input id="email" name="email" type="email" value={formData.email} onChange={handleChange} required />
                        </div>

                        <div className="input-group">
                            <label htmlFor="telefono">Teléfono</label>
                            <input id="telefono" name="telefono" type="tel" value={formData.telefono} onChange={handleChange} required />
                        </div>

                        <div className="input-group">
                            <label htmlFor="password">Contraseña</label>
                            <input id="password" name="password" type="password" value={formData.password} onChange={handleChange} required />
                        </div>

                        <div className="input-group">
                            <label htmlFor="fotoPerfil">Foto de perfil</label>
                            <input id="fotoPerfil" name="fotoPerfil" type="file" accept="image/*" />
                        </div>
                    </div>

                    <div className="layout-actions">
                        <button type="submit" className="btn-login">
                            Crear Cuenta
                        </button>
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
