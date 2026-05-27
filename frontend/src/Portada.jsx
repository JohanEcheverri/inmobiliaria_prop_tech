import { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import './Portada.css';
import Layout from './components/Layout';

import imgBusqueda from './assets/Busqueda.png';
import imgGestion from './assets/Gestion.png';
import imgAnalisis from './assets/Analisis.png';

// Datos estáticos para el carrusel de inmuebles destacados
const INMUEBLES_DESTACADOS = [
    {
        id: 1,
        tipo: 'CASA',
        finalidad: 'VENTA',
        ubicacion: 'La Castellana, Armenia',
        precio: '$420.000.000',
        imagen: 'https://images.unsplash.com/photo-1580587771525-78b9dba3b914?auto=format&fit=crop&w=800&q=80'
    },
    {
        id: 2,
        tipo: 'APARTAMENTO',
        finalidad: 'VENTA',
        ubicacion: 'Norte, Armenia',
        precio: '$280.000.000',
        imagen: 'https://images.unsplash.com/photo-1545324418-cc1a3fa10c00?auto=format&fit=crop&w=800&q=80'
    },
    {
        id: 3,
        tipo: 'LOCAL COMERCIAL',
        finalidad: 'ARRENDAMIENTO',
        ubicacion: 'Granada, Armenia',
        precio: '$4.800.000 / mes',
        imagen: 'https://images.unsplash.com/photo-1441986300917-64674bd600d8?auto=format&fit=crop&w=800&q=80'
    }
];

const Portada = () => {
    const navigate = useNavigate();
    const [currentSlide, setCurrentSlide] = useState(0);

    // Loop automático para rotar el carrusel de imágenes cada 5 segundos
    useEffect(() => {
        const timer = setInterval(() => {
            setCurrentSlide((prev) => (prev === INMUEBLES_DESTACADOS.length - 1 ? 0 : prev + 1));
        }, 5000);
        return () => clearInterval(timer);
    }, []);

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
                </div>

                {/* --- SECCIÓN VISUAL CON CARRUSEL DE IMÁGENES --- */}
                <div className="hero-visual">
                    <div className="carousel-wrapper">
                        {INMUEBLES_DESTACADOS.map((inm, index) => (
                            <div
                                key={inm.id}
                                className={`carousel-slide ${index === currentSlide ? 'active' : ''}`}
                                style={{ backgroundImage: `url(${inm.imagen})` }}
                            >
                                <div className="carousel-overlay">
                                    <span className="carousel-badge">{inm.tipo} · {inm.finalidad}</span>
                                    <h4>{inm.ubicacion}</h4>
                                    <p className="carousel-price">{inm.precio}</p>
                                </div>
                            </div>
                        ))}

                        {/* Controles manuales */}
                        <button
                            className="carousel-btn prev"
                            onClick={() => setCurrentSlide(currentSlide === 0 ? INMUEBLES_DESTACADOS.length - 1 : currentSlide - 1)}
                        >
                            &#10094;
                        </button>
                        <button
                            className="carousel-btn next"
                            onClick={() => setCurrentSlide(currentSlide === INMUEBLES_DESTACADOS.length - 1 ? 0 : currentSlide + 1)}
                        >
                            &#10095;
                        </button>

                        {/* Indicadores de puntos inferiores */}
                        <div className="carousel-dots">
                            {INMUEBLES_DESTACADOS.map((_, index) => (
                                <button
                                    key={index}
                                    className={`dot ${index === currentSlide ? 'active' : ''}`}
                                    onClick={() => setCurrentSlide(index)}
                                />
                            ))}
                        </div>
                    </div>

                    <div className="stats-mini-card">
                        <span>+500</span>
                        <p>Propiedades Activas</p>
                    </div>
                </div>
            </header>

            {/* Características de valor de DomusTech */}
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

            {/* Seccion de Zonas en Tendencia */}
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