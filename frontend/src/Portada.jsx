import { useNavigate } from 'react-router-dom';
import './Portada.css';
import Layout from './components/Layout';

import imgBusqueda from './assets/Busqueda.png';
import imgGestion from './assets/Gestion.png';
import imgAnalisis from './assets/Analisis.png';

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

                    {/* Los botones inferiores se han eliminado por redundancia */}
                </div>

                <div className="hero-visual">
                    <div className="abstract-blob"></div>
                    <div className="stats-mini-card">
                        <span>+500</span>
                        <p>Propiedades Activas</p>
                    </div>
                </div>
            </header>

            {/* Sección de Valor Técnico con Imágenes Mejoradas */}
            <section className="features">
                <div className="feature-card">
                    <div className="feature-image-container">
                        <img src={imgBusqueda} alt="Búsqueda Inteligente" className="feature-illustration" />
                    </div>
                    <h3>Búsqueda Inteligente</h3>
                    <p>Optimización mediante estructuras de datos para filtrado instantáneo por zonas y precios.</p>
                </div>

                <div className="feature-card">
                    <div className="feature-image-container">
                        <img src={imgGestion} alt="Gestión de Visitas" className="feature-illustration" />
                    </div>
                    <h3>Gestión de Visitas</h3>
                    <p>Sistema de priorización para asesores, garantizando una atención ágil y organizada.</p>
                </div>

                <div className="feature-card">
                    <div className="feature-image-container">
                        <img src={imgAnalisis} alt="Market Analytics" className="feature-illustration" />
                    </div>
                    <h3>Market Analytics</h3>
                    <p>Análisis de tendencias y relaciones cliente-inmueble para decisiones basadas en datos.</p>
                </div>
            </section>

            {/* Zonas en Tendencia */}
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