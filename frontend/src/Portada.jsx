import { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import './Portada.css';
import Layout from './components/Layout';

import imgBusqueda from './assets/Busqueda.png';
import imgGestion from './assets/Gestion.png';
import imgAnalisis from './assets/Analisis.png';

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

    const [busquedaZona, setBusquedaZona] = useState('');
    const [busquedaTipo, setBusquedaTipo] = useState('');

    const [totalPropiedades, setTotalPropiedades] = useState('+500');
    const [textoCard, setTextoCard] = useState('Propiedades disponibles en la plataforma');
    const [isSearching, setIsSearching] = useState(false);

    useEffect(() => {
        const timer = setInterval(() => {
            setCurrentSlide((prev) => (prev === INMUEBLES_DESTACADOS.length - 1 ? 0 : prev + 1));
        }, 5000);
        return () => clearInterval(timer);
    }, []);

    const handleQuickSearch = (event) => {
        event.preventDefault();

        if (!busquedaZona.trim() && !busquedaTipo) {
            setTotalPropiedades('+500');
            setTextoCard('Propiedades disponibles en la plataforma');
            return;
        }

        setIsSearching(true);

        setTimeout(() => {
            const totalSimulado = Math.floor(Math.random() * 25) + 3;
            setTotalPropiedades(`${totalSimulado}`);

            // 1. Formatear la ciudad de "ARmenIa" o "armenia" a "Armenia"
            const zonaLimpia = busquedaZona.trim();
            const zonaFormateada = zonaLimpia
                ? zonaLimpia.charAt(0).toUpperCase() + zonaLimpia.slice(1).toLowerCase()
                : '';

            // 2. Manejar correctamente el plural según el tipo de inmueble
            let tipoPlural = 'inmuebles';
            if (busquedaTipo) {
                if (busquedaTipo === 'LOCAL') {
                    tipoPlural = 'locales'; // 👈 Corrección específica para "locales"
                } else {
                    tipoPlural = `${busquedaTipo.toLowerCase()}s`; // Agrega la 's' normal para casas, bodegas, etc.
                }
            }

            // 3. Construir el texto final con los datos limpios
            const textoTipo = tipoPlural.charAt(0).toUpperCase() + tipoPlural.slice(1);
            const zonaTexto = zonaFormateada ? ` en "${zonaFormateada}"` : '';

            setTextoCard(`${textoTipo} listados que coinciden con tu criterio${zonaTexto}`);

            setIsSearching(false);

            // Vaciar la barra de búsqueda automáticamente
            setBusquedaZona('');
            setBusquedaTipo('');
        }, 800);
    };

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

                    <form className="quick-search" onSubmit={handleQuickSearch}>
                        <input
                            type="text"
                            placeholder="Ciudad"
                            aria-label="Ciudad"
                            value={busquedaZona}
                            onChange={(e) => setBusquedaZona(e.target.value)}
                        />
                        <select
                            aria-label="Tipo de inmueble"
                            value={busquedaTipo}
                            onChange={(e) => setBusquedaTipo(e.target.value)}
                        >
                            <option value="">Todos los tipos</option>
                            {/* 👈 Nuevas opciones añadidas con éxito */}
                            <option value="APARTAMENTO">APARTAMENTO</option>
                            <option value="BODEGA">BODEGA</option>
                            <option value="CASA">CASA</option>
                            <option value="LOCAL">LOCAL</option>
                            <option value="LOTE">LOTE</option>
                            <option value="OFICINA">OFICINA</option>
                        </select>
                        <button type="submit" disabled={isSearching}>
                            {isSearching ? 'Buscando...' : 'Buscar'}
                        </button>
                    </form>

                    <div className={`stats-quick-card ${isSearching ? 'is-loading' : ''}`}>
                        <div className="card-counter-wrapper">
                            {isSearching ? (
                                <div className="search-spinner"></div>
                            ) : (
                                <span className="counter-number">{totalPropiedades}</span>
                            )}
                        </div>
                        <p className="counter-text">{textoCard}</p>
                    </div>
                </div>

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
                </div>
            </header>

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