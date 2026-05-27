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

        // Si viene como un objeto (por ejemplo { url: '...', id: ... }), extraemos su ruta interna
        if (typeof img === 'object') {
            return img.url || img.path || img.ruta || String(img);
        }

        if (typeof img !== 'string') return String(img);

        // Si es Base64 (data:) o una URL externa (http), se retorna tal cual como hace tu AdminModal
        if (img.startsWith('data:') || img.startsWith('http')) return img;

        // Si es una ruta relativa local
        if (img.startsWith('/')) return `${window.location.origin}${img}`;

        return img;
    };

    const getImages = (inmueble) => {
        if (!inmueble) return [];

        // 🚀 Ajuste de desestructuración: busca la data en la raíz o en el objeto interno del backend
        const datosReales = inmueble.inmueble ? inmueble.inmueble : inmueble;

        // Buscamos la lista de imágenes tal cual la lógica de 'crearFormDataInicial'
        const imgsCandidate = datosReales.imagenes || datosReales.imagen || [];
        const arr = Array.isArray(imgsCandidate) ? imgsCandidate : [imgsCandidate];

        return arr.filter(Boolean).map(resolveImage);
    };

    if (!propiedades || propiedades.length === 0) {
        return <p className="empty-text">No tienes propiedades adquiridas.</p>;
    }

    return (
        <div className="property-grid">
            {propiedades.map((p, index) => {
                // Evaluamos si el endpoint envolvió el objeto en un campo llamado 'inmueble'
                const datosInmueble = p.inmueble ? p.inmueble : p;

                const imagenesPropiedad = getImages(p);
                const primeraImagen = imagenesPropiedad[0];

                return (
                    <article
                        className="property-card"
                        key={datosInmueble.codigo || `prop-${index}`}
                        onClick={() => onVerDetalle && onVerDetalle(p)}
                        style={{ cursor: onVerDetalle ? 'pointer' : 'default' }}
                    >
                        {/* Contenedor de la foto con su Badge de Adquirido encima */}
                        <div className="property-thumb">
                            {primeraImagen ? (
                                <img
                                    src={primeraImagen}
                                    alt={datosInmueble.direccionBarrio || datosInmueble.direccion || "Inmueble"}
                                />
                            ) : (
                                <div className="carousel-empty">
                                    <span>{datosInmueble.tipoInmueble || 'CASA'}</span>
                                </div>
                            )}
                            <div className="acquired-badge">Adquirido</div>
                        </div>

                        {/* Cuerpo de la tarjeta */}
                        <div className="property-body">
                            <h3>{datosInmueble.direccionBarrio || datosInmueble.direccion || 'Sin dirección'}</h3>
                            <p>{datosInmueble.ciudad || 'Ciudad'} · {datosInmueble.zona || 'Zona'}</p>
                            <strong>{formatCurrency(datosInmueble.precio)}</strong>

                            <div className="property-meta">
                                <span>{datosInmueble.tipoInmueble || 'Inmueble'}</span>
                                <span>{datosInmueble.area || '0'} m²</span>
                                <span>{datosInmueble.habitaciones || '0'} hab.</span>
                            </div>
                        </div>
                    </article>
                );
            })}
        </div>
    );
}