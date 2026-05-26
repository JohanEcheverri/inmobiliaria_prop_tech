import { useEffect, useState, useRef } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import logo from '../assets/logo.png';
import './Layout.css';
import { apiUrl } from '../api';

const Layout = ({ children, actions, contentClassName = '' }) => {
    const navigate = useNavigate();
    const location = useLocation();
    const menuRef = useRef(null);
    const chatRef = useRef(null);
    const [isMenuOpen, setIsMenuOpen] = useState(false);
    const [isChatOpen, setIsChatOpen] = useState(false);
    const [chatMessage, setChatMessage] = useState('');
    const [chatMessages, setChatMessages] = useState([
        {
            role: 'assistant',
            text: 'Hola, soy el asistente IA de DomusTech. Puedes preguntarme por inmuebles, zonas, precios o procesos de compra y arriendo.'
        }
    ]);
    const [isChatLoading, setIsChatLoading] = useState(false);

    // SOLUCIÓN AL PARPADEO: Inicializar directamente desde localStorage en lugar de empezar en null
    const [user, setUser] = useState(() => {
        const savedSession = localStorage.getItem("user_session");
        return savedSession ? JSON.parse(savedSession) : null;
    });

    useEffect(() => {
        const refreshUserSession = () => {
            const savedSession = localStorage.getItem("user_session");
            setUser(savedSession ? JSON.parse(savedSession) : null);
        };

        window.addEventListener('storage', refreshUserSession);
        window.addEventListener('user_session_updated', refreshUserSession);
        return () => {
            window.removeEventListener('storage', refreshUserSession);
            window.removeEventListener('user_session_updated', refreshUserSession);
        };
    }, []);

    useEffect(() => {
        const handleClickOutside = (event) => {
            if (menuRef.current && !menuRef.current.contains(event.target)) {
                setIsMenuOpen(false);
            }
            if (chatRef.current && !chatRef.current.contains(event.target)) {
                setIsChatOpen(false);
            }
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    const handleChatSubmit = async (event) => {
        event.preventDefault();
        const mensaje = chatMessage.trim();

        if (!mensaje || isChatLoading) {
            return;
        }

        setChatMessages(prev => [...prev, { role: 'user', text: mensaje }]);
        setChatMessage('');
        setIsChatLoading(true);

        try {
            const response = await fetch(apiUrl('/ia/chat'), {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    clienteId: user?.id || null,
                    mensaje
                })
            });

            if (!response.ok) {
                throw new Error('No se pudo obtener respuesta del asistente IA');
            }

            const data = await response.json();
            setChatMessages(prev => [
                ...prev,
                { role: 'assistant', text: data.respuesta || 'No recibí una respuesta disponible.' }
            ]);
        } catch (error) {
            setChatMessages(prev => [
                ...prev,
                { role: 'assistant', text: 'No pude conectar con el asistente IA en este momento. Intenta de nuevo más tarde.' }
            ]);
        } finally {
            setIsChatLoading(false);
        }
    };

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

            <div className="ai-chat-widget" ref={chatRef}>
                {isChatOpen && (
                    <section className="ai-chat-panel" aria-label="Chat IA DomusTech">
                        <div className="ai-chat-header">
                            <div>
                                <p className="ai-chat-kicker">DomusTech</p>
                                <h2>Chat IA</h2>
                            </div>
                            <button
                                type="button"
                                className="ai-chat-close"
                                onClick={() => setIsChatOpen(false)}
                                aria-label="Cerrar chat IA"
                            >
                                ×
                            </button>
                        </div>

                        <div className="ai-chat-messages">
                            {chatMessages.map((message, index) => (
                                <div key={`${message.role}-${index}`} className={`ai-chat-message ${message.role}`}>
                                    {message.text}
                                </div>
                            ))}
                            {isChatLoading && (
                                <div className="ai-chat-message assistant loading">Escribiendo...</div>
                            )}
                        </div>

                        <form className="ai-chat-form" onSubmit={handleChatSubmit}>
                            <input
                                type="text"
                                value={chatMessage}
                                onChange={(event) => setChatMessage(event.target.value)}
                                placeholder="Pregunta al asistente"
                                aria-label="Mensaje para el chat IA"
                                disabled={isChatLoading}
                            />
                            <button type="submit" disabled={!chatMessage.trim() || isChatLoading} aria-label="Enviar mensaje">
                                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round" className="layout-svg-icon">
                                    <path d="M22 2 11 13"></path>
                                    <path d="m22 2-7 20-4-9-9-4 20-7Z"></path>
                                </svg>
                            </button>
                        </form>
                    </section>
                )}

                <button
                    type="button"
                    className={`ai-chat-bubble ${isChatOpen ? 'active' : ''}`}
                    onClick={() => setIsChatOpen(prev => !prev)}
                    aria-label={isChatOpen ? 'Ocultar chat IA' : 'Abrir chat IA'}
                    aria-expanded={isChatOpen}
                >
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.1" strokeLinecap="round" strokeLinejoin="round" className="ai-chat-bubble-icon">
                        <path d="M21 15a4 4 0 0 1-4 4H8l-5 3V7a4 4 0 0 1 4-4h10a4 4 0 0 1 4 4z"></path>
                        <path d="M9 10h.01"></path>
                        <path d="M12 10h.01"></path>
                        <path d="M15 10h.01"></path>
                    </svg>
                </button>
            </div>
        </div>
    );
};

export default Layout;
