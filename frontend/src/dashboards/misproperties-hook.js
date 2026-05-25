// helper not used yet - placeholder
export async function fetchMisPropiedades(clienteId) {
    const url = `http://localhost:8080/api/operaciones/cliente/${clienteId}/propiedades`;
    const res = await fetch(url);
    if (!res.ok) throw new Error('No se pudieron obtener propiedades adquiridas');
    return await res.json();
}
