import { useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import './App.css';
import Layout from './components/Layout';

function Login() {
    const [identificacion, setIdentificacion] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const navigate = useNavigate();

    const handleLogin = async (e) => {
        e.preventDefault();
        setError('');

        try {
            const response = await axios.post('http://localhost:8080/api/auth/login', {
                identificacion, // Enviamos el nuevo campo al backend
                password
            });

            const { rol, nombre } = response.data;
            const normalizedRol = String(rol || '').toUpperCase();
            console.log(`Bienvenido ${nombre} (${rol})`);

            // Redirección lógica por roles
            if (normalizedRol === 'ADMIN' || normalizedRol === 'ADMINISTRADOR') {
                navigate('/admin-dashboard');
            } else if (normalizedRol === 'ASESOR') {
                navigate('/asesor-dashboard');
            } else {
                navigate('/cliente-dashboard');
            }

        } catch (err) {
            if (err.response) {
                setError(err.response.data.error || "Error al iniciar sesión");
            } else {
                setError("No se pudo conectar con el servidor");
            }
        }
    };

    return (
        <Layout contentClassName="login-container">
            <div className="login-card">
                <h2>Prop-Tech</h2>
                <p>Ingresa tus credenciales para continuar</p>

                <form onSubmit={handleLogin}>
                    <div className="input-group">
                        <label htmlFor="identificacion">ID / Usuario</label>
                        <input
                            id="identificacion"
                            type="text"
                            placeholder="Ej: 1094123"
                            value={identificacion}
                            onChange={(e) => setIdentificacion(e.target.value)}
                            required
                        />
                    </div>

                    <div className="input-group">
                        <label htmlFor="password">Contraseña</label>
                        <input
                            id="password"
                            type="password"
                            placeholder="••••••••"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            required
                        />
                    </div>

                    <button type="submit" className="btn-login">
                        Iniciar Sesión
                    </button>

                    {error && <div className="error-message">{error}</div>}
                </form>

                <button type="button" className="auth-link-button" onClick={() => navigate('/register')}>
                    ¿No tienes cuenta? Regístrate aquí
                </button>
            </div>
        </Layout>
    );
}

export default Login;
