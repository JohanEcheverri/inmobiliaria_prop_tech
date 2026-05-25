import React, { useState } from 'react';

export default function SearchBar({ onSearch, initialClienteId }) {
    const [filters, setFilters] = useState({ zona: '', tipo: '', minPrecio: '', maxPrecio: '', minHabitaciones: '', maxHabitaciones: '' });

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFilters(prev => ({ ...prev, [name]: value }));
    };

    const submit = (e) => {
        e.preventDefault();
        onSearch({ ...filters, clienteId: initialClienteId });
    };

    return (
        <form className="catalog-search-bar" onSubmit={submit}>
            <input name="zona" placeholder="Zona (ej. NORTE)" value={filters.zona} onChange={handleChange} />
            <input name="tipo" placeholder="Tipo (ej. APARTAMENTO)" value={filters.tipo} onChange={handleChange} />
            <input name="minPrecio" type="number" placeholder="Min precio" value={filters.minPrecio} onChange={handleChange} />
            <input name="maxPrecio" type="number" placeholder="Max precio" value={filters.maxPrecio} onChange={handleChange} />
            <input name="minHabitaciones" type="number" placeholder="Min hab" value={filters.minHabitaciones} onChange={handleChange} />
            <button type="submit" className="btn small">Buscar</button>
        </form>
    );
}
