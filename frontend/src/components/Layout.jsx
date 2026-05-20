import { useEffect, useState, useRef } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import logo from '../assets/logo.png';
import './Layout.css';

const Layout = ({ children, actions, contentClassName = '' }) => {
    const navigate = useNavigate();
    const location = useLocation();
    const menuRef = useRef(null);
    const [isMenuOpen, setIsMenuOpen] = useState(false);

    // SOLUCIÓN AL PARPADEO: Inicializar directamente desde localStorage en lugar de empezar en null
    const [user, setUser] = useState(() => {
        const savedSession = localStorage.getItem("user_session");
        return savedSession ? JSON.parse(savedSession) : null;
    });

    // Sincronizar por si la sesión cambia en otra parte de la app
    useEffect(() => {
        const savedSession = localStorage.getItem("user_session");
        if (savedSession) {
            setUser(JSON.parse(savedSession));
        }

        const handleClickOutside = (event) => {
            if (menuRef.current && !menuRef.current.contains(event.target)) {
                setIsMenuOpen(false);
            }
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    const handleLogout = () => {
        localStorage.removeItem("user_session");
        setUser(null);
        setIsMenuOpen(false);
        navigate('/');
    };

    const handleGoToDashboard = () => {
        if (user?.rol === 'ADMINISTRADOR') {
            navigate('/admin-dashboard');
        } else if (user?.rol === 'ASESOR') {
            navigate('/asesor-dashboard');
        } else {
            navigate('/cliente-dashboard');
        }
    };

    // SOLUCIÓN AL BOTÓN VISIBLE EN EL PANEL: Evalúa estrictamente que NO esté en la ruta del Dashboard
    const mostrarBotonDashboardExterior = user && (
        (user.rol === 'ADMINISTRADOR' && location.pathname !== '/admin-dashboard') ||
        (user.rol === 'ASESOR' && location.pathname !== '/asesor-dashboard') ||
        (user.rol !== 'ADMINISTRADOR' && user.rol !== 'ASESOR' && location.pathname !== '/cliente-dashboard')
    );

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
                    {user ? (
                        <div className="user-nav-wrapper-global">

                            {/* Botón exterior refinado con icono SVG */}
                            {mostrarBotonDashboardExterior && (
                                <button className="btn-header-dashboard-shortcut" onClick={handleGoToDashboard}>
                                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" className="layout-svg-icon">
                                        <rect x="3" y="3" width="7" height="9"></rect>
                                        <rect x="14" y="3" width="7" height="5"></rect>
                                        <rect x="14" y="12" width="7" height="9"></rect>
                                        <rect x="3" y="16" width="7" height="5"></rect>
                                    </svg>
                                    Volver al Panel
                                </button>
                            )}

                            <div className="user-profile-dropdown-wrapper" ref={menuRef}>
                                <div
                                    className={`user-nav-trigger ${isMenuOpen ? 'active' : ''}`}
                                    onClick={() => setIsMenuOpen(!isMenuOpen)}
                                >
                                    <span className="user-welcome-text-large">
                                        Hola, <strong>{user.nombre.split(' ')[0]}</strong>
                                    </span>

                                    <div className="user-avatar-nav-large">
                                        {user.fotoPerfil ? (
                                            <img
                                                src={user.fotoPerfil}
                                                alt="Perfil"
                                                className="nav-avatar-img"
                                                onError={(e) => {
                                                    e.target.src = 'https://ui-avatars.com/api/?name=' + user.nombre;
                                                }}
                                            />
                                        ) : (
                                            <div className="nav-avatar-placeholder-large">
                                                {user.nombre.charAt(0).toUpperCase()}
                                            </div>
                                        )}
                                    </div>
                                    <span className="dropdown-chevron-icon">▾</span>
                                </div>

                                {isMenuOpen && (
                                    <div className="dropdown-floating-menu">
                                        <div className="dropdown-user-details">
                                            <p className="dropdown-user-name">{user.nombre}</p>
                                            <p className="dropdown-user-role">{user.rol || 'Cliente'}</p>
                                        </div>

                                        <hr className="dropdown-divider" />

                                        {/* Opción "Mi Cuenta" con icono SVG profesional */}
                                        <button className="dropdown-menu-item" onClick={() => { setIsMenuOpen(false); navigate('/configuracion-cuenta'); }}>
                                            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="layout-svg-icon">
                                                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                                                <circle cx="12" cy="7" r="4"></circle>
                                            </svg>
                                            Mi Cuenta
                                        </button>

                                        {/* Opción "Cerrar Sesión" con icono SVG profesional */}
                                        <button className="dropdown-menu-item btn-logout-action" onClick={handleLogout}>
                                            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="layout-svg-icon">
                                                <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
                                                <polyline points="16 17 21 12 16 7"></polyline>
                                                <line x1="21" y1="12" x2="9" y2="12"></line>
                                            </svg>
                                            Cerrar Sesión
                                        </button>
                                    </div>
                                )}
                            </div>
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