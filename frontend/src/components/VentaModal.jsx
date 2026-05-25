import React, { useState } from 'react';

const API_BASE_URL = 'http://localhost:8080/api';

export default function VentaModal({ inmuebleCodigo, asesorId, onClose, onCompleted }) {
    const [form, setForm] = useState({ clienteId: '', valorAcordado: '', comision: '' });
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    const handleChange = (e) => {
        const { name, value } = e.target;
        setForm(prev => ({ ...prev, [name]: value }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError('');
        try {
            const payload = {
                estado: 'VENDIDO',
                venta: {
                    inmuebleCodigo,
                    clienteId: form.clienteId,
                    asesorId: asesorId || null,
                    valorAcordado: Number(form.valorAcordado),
                    comision: Number(form.comision)
                }
            };
            const response = await fetch(`${API_BASE_URL}/inmuebles/${inmuebleCodigo}/estado`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
            if (!response.ok) {
                const err = await response.json().catch(() => null);
                throw new Error(err?.error || 'No se pudo registrar la venta');
            }
            onCompleted && onCompleted();
            onClose && onClose();
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="modal-backdrop">
            <div className="modal-card">
                <h3>Registrar venta - {inmuebleCodigo}</h3>
                <form onSubmit={handleSubmit} className="modal-form">
                    <label>Id del comprador
                        <input name="clienteId" value={form.clienteId} onChange={handleChange} required />
                    </label>
                    <label>Valor acordado (COP)
                        <input name="valorAcordado" type="number" value={form.valorAcordado} onChange={handleChange} required />
                    </label>
                    <label>Comisión (COP)
                        <input name="comision" type="number" value={form.comision} onChange={handleChange} />
                    </label>
                    {error && <div className="modal-error">{error}</div>}
                    <div className="modal-actions">
                        <button type="button" className="btn ghost" onClick={onClose} disabled={loading}>Cancelar</button>
                        <button type="submit" className="btn primary" disabled={loading}>{loading ? 'Guardando...' : 'Registrar venta'}</button>
                    </div>
                </form>
            </div>
        </div>
    );
}
