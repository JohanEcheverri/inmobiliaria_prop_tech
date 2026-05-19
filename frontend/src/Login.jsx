import { useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import './App.css';
import Layout from './components/Layout';

function Login() {
    const [identificacion, setIdentificacion] = useState('');
    const [contrasenia, setContrasenia] = useState('');
    const [error, setError] = useState('');
    const [showPassword, setShowPassword] = useState(false);
    const navigate = useNavigate();

    const handleLogin = async (e) => {
        e.preventDefault();
        setError('');

        try {
            // Envío correcto según tu última captura
            const response = await axios.post('http://localhost:8080/api/auth/login', {
                identificacion,
                contrasenia
            });

            localStorage.setItem("user_session", JSON.stringify(response.data));

            const { rol } = response.data;
            const normalizedRol = String(rol || '').toUpperCase();

            if (normalizedRol === 'ADMINISTRADOR') {
                navigate('/admin-dashboard');
            } else if (normalizedRol === 'ASESOR') {
                navigate('/asesor-dashboard');
            } else {
                navigate('/cliente-dashboard');
            }

        } catch (err) {
            // Si el Payload es correcto pero falla, el error viene de AutenticacionService
            setError(err.response?.data?.error || "Credenciales incorrectas");
        }
    };

    return (
        <Layout contentClassName="login-container">
            <div className="login-card">
                <h2>DomusTech</h2>
                <p>Ingresa tus credenciales para continuar</p>

                <form onSubmit={handleLogin}>
                    <div className="input-group">
                        <label htmlFor="identificacion">ID / Usuario</label>
                        <input
                            id="identificacion"
                            type="text"
                            placeholder="Ej: 1095208966"
                            value={identificacion}
                            onChange={(e) => setIdentificacion(e.target.value)}
                            required
                        />
                    </div>

                    <div className="input-group">
                        <label htmlFor="password">Contraseña</label>
                        <div className="password-wrapper">
                            <input
                                id="password"
                                type={showPassword ? "text" : "password"}
                                placeholder="••••••••"
                                value={contrasenia}
                                onChange={(e) => setContrasenia(e.target.value)}
                                required
                            />
                            <button
                                type="button"
                                className="toggle-password"
                                onClick={() => setShowPassword(!showPassword)}
                            >
                                {showPassword ? "👁️" : "🙈"}
                            </button>
                        </div>
                    </div>

                    <div className="layout-actions">
                        <button type="submit" className="btn-login">
                            Iniciar Sesión
                        </button>
                    </div>


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