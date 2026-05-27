import React from 'react';
import { API_BASE_URL } from '../api';

export default function MisProperties({ propiedades, onVerDetalle }) {
    const formatCurrency = (value) =>
        new Intl.NumberFormat('es-CO', {
            style: 'currency',
            currency: 'COP',
            maximumFractionDigits: 0
        }).format(Number(value || 0));

    const resolveImage = (img) => {
        if (!img) return null;

        // Normalizar a string: soporta objetos devueltos por el backend y cadenas
        let src = img;
        if (typeof img === 'object') {
            src = img.url || img.path || img.ruta || img.src || img.nombre || img.toString();
        }

        if (typeof src !== 'string') src = String(src);

        // Base64 data URL o URL absoluta
        if (src.startsWith('data:') || src.startsWith('http')) return src;

        // Si es Base64 puro (sin prefijo data:), asumimos image/jpeg como fallback
        // Detectar cadenas base64 largas que sólo contienen el alfabeto base64
        const base64Like = /^[A-Za-z0-9+/=\n\r]+$/.test(src) && src.length > 100;
        if (base64Like) return `data:image/jpeg;base64,${src.replace(/\s+/g, '')}`;

        // Ruta absoluta en el servidor (ej. '/uploads/...') => resolver contra el origin del API
        if (src.startsWith('/')) {
            try {
                const apiOrigin = new URL(API_BASE_URL).origin;
                return `${apiOrigin}${src}`;
            } catch (e) {
                // Fallback al origin actual si API_BASE_URL no es válido
                return `${window.location.origin}${src}`;
            }
        }

        return src;
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
                        onClick={() => onVerDetalle && onVerDetalle(datosInmueble.codigo || p.codigo)}
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