import React, { useState } from 'react';

export default function MisProperties({ propiedades, clienteId, onVerDetalle, onRefresh }) {
    const [cancelando, setCancelando] = useState(null);
    const [motivo, setMotivo] = useState('');
    const [error, setError] = useState('');

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

    const esArriendo = (p) => p.tipoOperacion === 'ARRIENDO';
    const puedeSolicitarCancelacion = (p) =>
        esArriendo(p) && p.operacionEstado === 'COMPLETADA';

    const solicitarCancelacion = async (operacionCodigo) => {
        if (!motivo.trim()) {
            setError('Indica el motivo de la cancelación.');
            return;
        }
        setError('');
        try {
            const response = await fetch(
                `http://localhost:8080/api/operaciones/arriendos/${operacionCodigo}/solicitar-cancelacion`,
                {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ clienteId, motivo: motivo.trim() })
                }
            );
            if (!response.ok) {
                const err = await response.json().catch(() => null);
                throw new Error(err?.mensaje || err?.error || 'No se pudo enviar la solicitud');
            }
            setCancelando(null);
            setMotivo('');
            await onRefresh?.();
        } catch (e) {
            setError(e.message || 'No se pudo enviar la solicitud');
        }
    };

    if (!propiedades || propiedades.length === 0) {
        return <p className="empty-text">No tienes propiedades adquiridas ni arrendadas.</p>;
    }

    return (
        <div className="property-grid">
            {propiedades.map(p => {
                const imagenesPropiedad = getImages(p);
                const primeraImagen = imagenesPropiedad[0];
                const arriendo = esArriendo(p);
                const badgeClass = arriendo ? 'rented-badge' : 'acquired-badge';
                const badgeLabel = arriendo ? 'Arrendado' : 'Adquirido';

                return (
                    <article className="property-card" key={p.operacionCodigo || p.codigo}>
                        <div className="property-thumb">
                            {primeraImagen ? (
                                <img src={primeraImagen} alt={p.direccionBarrio || p.direccion} />
                            ) : (
                                <div className="carousel-empty"><span>{p.tipoInmueble}</span></div>
                            )}
                            <div className={badgeClass}>{badgeLabel}</div>
                        </div>

                        <div className="property-body">
                            <h3>{p.direccionBarrio || p.direccion}</h3>
                            <p>{p.ciudad} · {p.zona}</p>
                            <strong>{formatCurrency(p.valorAcordado || p.precio)}</strong>
                            {arriendo && (
                                <p className="property-rent-meta">
                                    Contrato: {p.duracionMeses} mes(es)
                                    {p.fechaVencimiento ? ` · vence ${p.fechaVencimiento}` : ''}
                                </p>
                            )}
                            {p.operacionEstado === 'EN_PROCESO' && arriendo && (
                                <span className="status-pill reprogramada">Cancelación en trámite</span>
                            )}
                            <div className="property-meta">
                                <span>{p.tipoInmueble}</span>
                                <span>{p.area} m²</span>
                                <span>{p.habitaciones} hab.</span>
                            </div>
                        </div>

                        <div className="card-actions">

                            {puedeSolicitarCancelacion(p) && (
                                <button
                                    className="danger"
                                    onClick={() => {
                                        setCancelando(p.operacionCodigo);
                                        setMotivo('');
                                        setError('');
                                    }}
                                >
                                    Solicitar cancelación
                                </button>
                            )}
                        </div>

                        {cancelando === p.operacionCodigo && (
                            <div className="inline-cancel-form">
                                <label>
                                    Motivo de cancelación
                                    <textarea
                                        value={motivo}
                                        onChange={(e) => setMotivo(e.target.value)}
                                        rows={2}
                                        placeholder="Describe brevemente el motivo..."
                                    />
                                </label>
                                {error && <div className="modal-error">{error}</div>}
                                <div className="inline-actions">
                                    <button type="button" onClick={() => solicitarCancelacion(p.operacionCodigo)}>
                                        Enviar solicitud
                                    </button>
                                    <button type="button" className="ghost" onClick={() => setCancelando(null)}>
                                        Cerrar
                                    </button>
                                </div>
                            </div>
                        )}
                    </article>
                );
            })}
        </div>
    );
}
