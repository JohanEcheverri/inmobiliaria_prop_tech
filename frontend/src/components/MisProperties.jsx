import React from 'react';

export default function MisProperties({ propiedades, onVerDetalle }) {
    const formatCurrency = (value) => new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 }).format(Number(value || 0));

    if (!propiedades || propiedades.length === 0) {
        return <p className="empty-text">No tienes propiedades adquiridas.</p>;
    }

    return (
        <div className="property-grid">
            {propiedades.map(p => (
                <article className="property-card" key={p.codigo}>
                    <div className="property-thumb"><div className="acquired-badge">Adquirido</div></div>
                    <div className="property-body">
                        <h3>{p.direccion}</h3>
                        <p>{p.ciudad} · {p.zona}</p>
                        <strong>{formatCurrency(p.precio)}</strong>
                        <div className="property-meta">
                            <span>{p.tipoInmueble}</span>
                            <span>{p.finalidad}</span>
                            <span>{p.area} m2</span>
                        </div>
                    </div>
                    <div className="card-actions">
                        <button onClick={() => onVerDetalle && onVerDetalle(p.codigo)}>Ver detalle</button>
                    </div>
                </article>
            ))}
        </div>
    );
}
