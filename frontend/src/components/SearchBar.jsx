import React, { useState } from 'react';

export default function SearchBar({ onSearch, initialClienteId }) {
    const ZONAS = ['', 'NORTE', 'SUR', 'ESTE', 'OESTE', 'CENTRO'];
    const TIPOS = ['', 'APARTAMENTO', 'CASA', 'LOCAL_COMERCIAL', 'OFICINA', 'LOTE', 'BODEGA'];
    const FINALIDADES = ['', 'VENTA', 'ARRENDAMIENTO'];
    const PRICE_RANGES = [
        { id: '', label: 'Cualquiera', min: '', max: '' },
        { id: '0-50000000', label: '< 50.000.000', min: 0, max: 50000000 },
        { id: '50000000-100000000', label: '50.000.000 - 100.000.000', min: 50000000, max: 100000000 },
        { id: '100000000-200000000', label: '100.000.000 - 200.000.000', min: 100000000, max: 200000000 },
        { id: '200000000-', label: '> 200.000.000', min: 200000000, max: '' }
    ];

    const [filters, setFilters] = useState({ zona: '', tipo: '', priceRange: '', minPrecio: '', maxPrecio: '', finalidad: '', minHabitaciones: '', maxHabitaciones: '' });

    const applySearch = (next) => {
        // If priceRange selected, map to minPrecio/maxPrecio
        const pr = PRICE_RANGES.find(p => p.id === next.priceRange);
        const payload = { ...next };
        if (pr) {
            payload.minPrecio = pr.min !== '' ? pr.min : '';
            payload.maxPrecio = pr.max !== '' ? pr.max : '';
        }
        if (onSearch) onSearch({ ...payload, clienteId: initialClienteId });
    };

    const handleChange = (e) => {
        const { name, value } = e.target;
        const next = { ...filters, [name]: value };
        setFilters(next);
        // Apply filter immediately on change
        applySearch(next);
    };

    const submit = (e) => {
        e.preventDefault();
        applySearch(filters);
    };

    return (
        <form className="catalog-search-bar" onSubmit={submit}>
            <select name="zona" value={filters.zona} onChange={handleChange}>
                {ZONAS.map(z => <option key={z} value={z}>{z === '' ? 'Zona (Cualquiera)' : z}</option>)}
            </select>

            <select name="tipo" value={filters.tipo} onChange={handleChange}>
                {TIPOS.map(t => <option key={t} value={t}>{t === '' ? 'Tipo (Cualquiera)' : t.replace('_', ' ')}</option>)}
            </select>

            <select name="finalidad" value={filters.finalidad} onChange={handleChange}>
                {FINALIDADES.map(f => <option key={f} value={f}>{f === '' ? 'Venta / Arriendo' : f}</option>)}
            </select>

            <select name="priceRange" value={filters.priceRange} onChange={handleChange}>
                {PRICE_RANGES.map(p => <option key={p.id} value={p.id}>{p.label}</option>)}
            </select>

            <input name="minHabitaciones" type="number" placeholder="Min hab" value={filters.minHabitaciones} onChange={handleChange} />
            <button type="submit" className="btn small">Buscar</button>
        </form>
    );
}
