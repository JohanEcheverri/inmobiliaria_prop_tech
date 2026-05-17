import { useNavigate } from 'react-router-dom';
import './Portada.css';
import Layout from './components/Layout';

const Portada = () => {
    const navigate = useNavigate();

    return (
        <Layout
            contentClassName="portada-container"
            actions={
                <>
                    <button className="btn-catalogo" onClick={() => navigate('/register')}>Registrarse</button>
                    <button className="btn-login" onClick={() => navigate('/login')}>
                        Ingresar
                    </button>
                </>
            }
        >
            {/* Sección Hero: El primer impacto */}
            <header className="hero-section">
                <div className="hero-content">
                    <div className="badge">PropTech Ecosystem</div>
                    <h1>Encuentra tu próximo hogar con <span className="highlight">DomusTech</span></h1>
                    <p>
                        Plataforma digital para conectar clientes, asesores e inmuebles con una
                        experiencia ágil, organizada y segura.
                    </p>

                    <form className="quick-search" onSubmit={(event) => event.preventDefault()}>
                        <input type="text" placeholder="Ciudad, barrio o zona" aria-label="Ciudad, barrio o zona" />
                        <select aria-label="Tipo de inmueble" defaultValue="">
                            <option value="" disabled>Tipo de inmueble</option>
                            <option>Casa</option>
                            <option>Apartamento</option>
                            <option>Local</option>
                        </select>
                        <button type="submit">Buscar</button>
                    </form>

                    <div className="hero-buttons">
                        <button className="btn-primary" onClick={() => navigate('/login')}>
                            Ingresar
                        </button>
                        <button className="btn-secondary" onClick={() => navigate('/register')}>
                            Registrarse
                        </button>
                    </div>
                </div>
                <div className="hero-visual">
                    {/* El gradiente simula la energía de los datos del logo */}
                    <div className="abstract-blob"></div>
                    <div className="stats-mini-card">
                        <span>+500</span>
                        <p>Propiedades Activas</p>
                    </div>
                </div>
            </header>

            {/* Sección de Valor Técnico */}
            <section className="features">
                <div className="feature-card">
                    <div className="icon">🔍</div>
                    <h3>Búsqueda Inteligente</h3>
                    <p>Optimización mediante estructuras de datos para filtrado instantáneo por zonas y precios.</p>
                </div>
                <div className="feature-card">
                    <div className="icon">📅</div>
                    <h3>Gestión de Visitas</h3>
                    <p>Sistema de priorización para asesores, garantizando una atención ágil y organizada.</p>
                </div>
                <div className="feature-card">
                    <div className="icon">📊</div>
                    <h3>Market Analytics</h3>
                    <p>Análisis de tendencias y relaciones cliente-inmueble para decisiones basadas en datos.</p>
                </div>
            </section>

            {/* Nueva sección: Zonas en Tendencia (Simulando el contexto del proyecto) */}
            <section className="trending-zones">
                <h2>Zonas con mayor actividad</h2>
                <div className="zones-grid">
                    <div className="zone-tag">Norte - Alta Demanda</div>
                    <div className="zone-tag">Centro - Comercial</div>
                    <div className="zone-tag">Sur - Residencial</div>
                    <div className="zone-tag">Occidente - En Crecimiento</div>
                </div>
            </section>

        </Layout>
    );
};

export default Portada;
