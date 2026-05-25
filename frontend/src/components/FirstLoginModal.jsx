import React, { useState } from 'react';

const API_BASE_URL = 'http://localhost:8080/api';

export default function FirstLoginModal({ clienteId, onClose, onSaved }) {
    const [form, setForm] = useState({
        tipoCliente: '',
        zonaInteres: '',
        presupuesto: '',
        tipoInmuebleDeseado: '',
        numeroHabitacionesDeseadas: 0,
        estadoBusqueda: ''
    });
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
                ...form,
                numeroHabitacionesDeseadas: Number(form.numeroHabitacionesDeseadas)
            };
            const response = await fetch(`${API_BASE_URL}/clientes/${clienteId}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
            if (!response.ok) {
                const err = await response.json().catch(() => null);
                throw new Error(err?.error || 'No se pudo guardar las preferencias');
            }
            onSaved && onSaved();
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
                <h3>Bienvenido — Cuéntanos tus preferencias</h3>
                <p>Con estas preferencias te mostraremos inmuebles recomendados.</p>
                <form onSubmit={handleSubmit} className="modal-form">
                    <label>Tipo de cliente
                        <select name="tipoCliente" value={form.tipoCliente} onChange={handleChange}>
                            <option value="">--Seleccionar--</option>
                            <option value="COMPRADOR">Comprador</option>
                            <option value="ARRENDATARIO">Arrendatario</option>
                        </select>
                    </label>
                    <label>Zona de interés
                        <select name="zonaInteres" value={form.zonaInteres} onChange={handleChange}>
                            <option value="">--Seleccionar--</option>
                            <option value="NORTE">Norte</option>
                            <option value="SUR">Sur</option>
                            <option value="CENTRO">Centro</option>
                        </select>
                    </label>
                    <label>Presupuesto
                        <input name="presupuesto" type="number" value={form.presupuesto} onChange={handleChange} placeholder="COP" />
                    </label>
                    <label>Tipo de inmueble deseado
                        <select name="tipoInmuebleDeseado" value={form.tipoInmuebleDeseado} onChange={handleChange}>
                            <option value="">--Seleccionar--</option>
                            <option value="APARTAMENTO">Apartamento</option>
                            <option value="CASA">Casa</option>
                            <option value="BODEGA">Bodega</option>
                            <option value="LOTE">Lote</option>
                            <option value="LOCAL_COMERCIAL">Local Comercial</option>
                            <option value="OFICINA">Oficina</option>
                        </select>
                    </label>
                    <label>Habitaciones deseadas
                        <input name="numeroHabitacionesDeseadas" type="number" value={form.numeroHabitacionesDeseadas} onChange={handleChange} min={0} />
                    </label>
                    <label>Estado de búsqueda
                        <select name="estadoBusqueda" value={form.estadoBusqueda} onChange={handleChange}>
                            <option value="">--Seleccionar--</option>
                            <option value="BUSCANDO">Buscando</option>
                            <option value="CERRADO">Cerrado</option>
                            <option value="NEGOCIANDO">Negociando</option>
                        </select>
                    </label>

                    {error && <div className="modal-error">{error}</div>}

                    <div className="modal-actions">
                        <button type="button" className="btn ghost" onClick={onClose} disabled={loading}>Cancelar</button>
                        <button type="submit" className="btn primary" disabled={loading}>{loading ? 'Guardando...' : 'Guardar preferencias'}</button>
                    </div>
                </form>
            </div>
        </div>
    );
}
