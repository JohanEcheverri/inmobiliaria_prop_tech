import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import logo from '../assets/logo.png';
import './Layout.css';

const Layout = ({ children, actions, contentClassName = '' }) => {
    const navigate = useNavigate();
    const [user, setUser] = useState(null);

    useEffect(() => {
        const savedSession = localStorage.getItem("user_session");
        if (savedSession) {
            setUser(JSON.parse(savedSession));
        }
    }, []);

    const handleLogout = () => {
        localStorage.removeItem("user_session");
        setUser(null);
        navigate('/');
    };

    return (
        <div className="layout">
            <header className="layout-header">
                <div className="layout-brand" onClick={() => navigate('/')}>
                    <img className="layout-logo" src={logo} alt="DomusTech" />
                    <div className="layout-title">
                        <span className="layout-title-main">DOMUS</span>
                        <span className="layout-title-accent">TECH</span>
                    </div>
                </div>

                <div className="layout-header-right">
                    {/* Si hay usuario, mostramos sus datos y el logout */}
                    {user ? (
                        <div className="user-nav-info">
                            <span className="user-welcome">Hola, <strong>{user.nombre}</strong></span>
                            <button className="btn-logout-minimal" onClick={handleLogout}>Cerrar Sesión</button>
                        </div>
                    ) : (
                        actions && <div className="layout-actions">{actions}</div>
                    )}
                </div>
            </header>

            <main className={`layout-main ${contentClassName}`}>
                {children}
            </main>

            <footer className="layout-footer">
                <p>&copy; 2026 DomusTech - Ingeniería de Sistemas UQ</p>
            </footer>
        </div>
    );
};

export default Layout;