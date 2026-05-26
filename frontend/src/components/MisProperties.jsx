import React from 'react';

export default function MisProperties({ propiedades, onVerDetalle }) {
    const formatCurrency = (value) =>
        new Intl.NumberFormat('es-CO', {
            style: 'currency',
            currency: 'COP',
            maximumFractionDigits: 0
        }).format(Number(value || 0));


    const resolveImage = (img) => {
        if (!img) return null;
        if (typeof img === 'object') return img.url || img.path || String(img);
        if (typeof img !== 'string') return String(img);
        if (img.startsWith('data:') || img.startsWith('http')) return img;
        if (img.startsWith('/')) return `${window.location.origin}${img}`;
        return img;
    };

    const getImages = (inmueble) => {
        const raw = inmueble?.imagenes ?? inmueble?.imagen ?? [];
        if (!raw) return [];
        const list = Array.isArray(raw) ? raw : [raw];
        return list.map(resolveImage).filter(Boolean);
    };

    if (!propiedades || propiedades.length === 0) {
        return <p className="empty-text">No tienes propiedades adquiridas.</p>;
    }

    return (
        <div className="property-grid">
            {propiedades.map(p => {
                const imagenesPropiedad = getImages(p);
                const primeraImagen = imagenesPropiedad[0];

                return (
                    <article className="property-card" key={p.codigo}>
                        {/* Contenedor de la foto con su Badge de Adquirido encima */}
                        <div className="property-thumb">
                            {primeraImagen ? (
                                <img src={primeraImagen} alt={p.direccionBarrio || p.direccion} />
                            ) : (
                                <div className="carousel-empty"><span>{p.tipoInmueble}</span></div>
                            )}
                            <div className="acquired-badge">Adquirido</div>
                        </div>

                        {/* Cuerpo de la tarjeta */}
                        <div className="property-body">
                            <h3>{p.direccionBarrio || p.direccion}</h3>
                            <p>{p.ciudad} · {p.zona}</p>
                            <strong>{formatCurrency(p.precio)}</strong>

                            <div className="property-meta">
                                <span>{p.tipoInmueble}</span>
                                <span>{p.area} m²</span>
                                <span>{p.habitaciones} hab.</span>
                            </div>
                        </div>

                        {/* Botón de acción para conectar con el abrirDetalle del Dashboard */}
                        <div className="card-actions">
                            <button onClick={() => onVerDetalle(p.codigo)}>
                                Ver detalle
                            </button>
                        </div>
                    </article>
                );
            })}
        </div>
    );
}