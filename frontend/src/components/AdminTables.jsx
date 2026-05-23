// ==========================================================================
// 1. COMPONENTE: TABLA DE INMUEBLES
// ==========================================================================
function InmueblesTable({ data, onEdit, onDelete }) {
    return (
        <table className="domustech-admin-table">
            <thead>
            <tr>
                <th>Código</th>
                <th>Tipo / Finalidad</th>
                <th>Ubicación</th>
                <th>Precio</th>
                <th>Características</th>
                <th>Estado / Disp.</th>
                <th>Asesor</th>
                <th>Acciones</th>
            </tr>
            </thead>
            <tbody>
            {data.length === 0 ? (
                <tr>
                    <td colSpan="8" className="table-empty-row">No hay inmuebles registrados.</td>
                </tr>
            ) : (
                data.map((inm) => (
                    <tr key={inm.codigo}>
                        <td><strong>{inm.codigo}</strong></td>
                        <td>
                                <span className={`badge-type ${inm.finalidad?.toLowerCase()}`}>
                                    {inm.tipoInmueble} ({inm.finalidad})
                                </span>
                        </td>
                        <td>{inm.direccion}, {inm.barrio} ({inm.ciudad})</td>
                        <td>${Number(inm.precio).toLocaleString()}</td>
                        <td>{inm.area}m² | {inm.habitaciones} Hab | {inm.banos} Baños</td>
                        <td>{inm.estadoInmueble} / <span className="status-active">{inm.disponibilidad}</span></td>
                        <td>{inm.asesorResponsable || 'Sin asignar'}</td>
                        <td>
                            <button className="btn-action-svg-edit" onClick={() => onEdit(inm)} title="Editar Inmueble">
                                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="table-icon-svg"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path></svg>
                            </button>
                            <button className="btn-action-svg-delete" onClick={() => onDelete(inm.codigo, 'inmuebles')} title="Eliminar Inmueble">
                                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="table-icon-svg"><polyline points="3 6 5 6 21 6"></polyline><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path><line x1="10" y1="11" x2="10" y2="17"></line><line x1="14" y1="11" x2="14" y2="17"></line></svg>
                            </button>
                        </td>
                    </tr>
                ))
            )}
            </tbody>
        </table>
    );
}

// ==========================================================================
// 2. COMPONENTE: TABLA DE CLIENTES (Ajustado para Base de Datos)
// ==========================================================================
function ClientesTable({ data, onEdit, onDelete }) {
    return (
        <table className="domustech-admin-table">
            <thead>
            <tr>
                <th>Foto</th>
                <th>Identificación</th>
                <th>Nombre Completo</th>
                <th>Correo Electrónico</th>
                <th>Teléfono</th>
                <th>Acciones</th>
            </tr>
            </thead>
            <tbody>
            {data.length === 0 ? (
                <tr>
                    <td colSpan="6" className="table-empty-row">No hay clientes registrados.</td>
                </tr>
            ) : (
                data.map((item) => (
                    /* 1. SE CAMBIA item.identificacion POR item.id */
                    <tr key={item.id}>
                        <td>
                            <div className="table-avatar-preview">
                                {item.fotoPerfil ? (
                                    <img src={item.fotoPerfil} alt={item.nombre} className="table-avatar-img" />
                                ) : (
                                    <div className="table-avatar-placeholder">{item.nombre?.charAt(0).toUpperCase()}</div>
                                )}
                            </div>
                        </td>
                        {/* 2. SE CAMBIA item.identificacion POR item.id */}
                        <td>{item.id}</td>
                        <td><strong>{item.nombre}</strong></td>
                        {/* 3. SE CAMBIA item.correo POR item.email */}
                        <td>{item.email}</td>
                        <td>{item.telefono}</td>
                        <td>
                            <button className="btn-action-svg-edit" onClick={() => onEdit(item)} title="Editar Cliente">
                                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="table-icon-svg"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path></svg>
                            </button>
                            {/* 4. SE ENVÍA item.id EN LUGAR DE item.identificacion */}
                            <button className="btn-action-svg-delete" onClick={() => onDelete(item.id, 'clientes')} title="Eliminar Cliente">
                                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="table-icon-svg"><polyline points="3 6 5 6 21 6"></polyline><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path><line x1="10" y1="11" x2="10" y2="17"></line><line x1="14" y1="11" x2="14" y2="17"></line></svg>
                            </button>
                        </td>
                    </tr>
                ))
            )}
            </tbody>
        </table>
    );
}

// ==========================================================================
// 3. COMPONENTE: TABLA DE ASESORES
// ==========================================================================
function AsesoresTable({ data, onEdit, onDelete }) {
    return (
        <table className="domustech-admin-table">
            <thead>
            <tr>
                <th>Foto</th>
                <th>Identificación</th>
                <th>Nombre Completo</th>
                <th>Correo Electrónico</th>
                <th>Teléfono</th>
                <th>Especialidad / Zona</th>
                <th>Acciones</th>
            </tr>
            </thead>
            <tbody>
            {data.length === 0 ? (
                <tr>
                    <td colSpan="7" className="table-empty-row">No hay asesores registrados.</td>
                </tr>
            ) : (
                data.map((item) => (
                    <tr key={item.id}>
                        <td>
                            <div className="table-avatar-preview">
                                {item.fotoPerfil ? (
                                    <img src={item.fotoPerfil} alt={item.nombre} className="table-avatar-img" />
                                ) : (
                                    <div className="table-avatar-placeholder">{item.nombre?.charAt(0).toUpperCase()}</div>
                                )}
                            </div>
                        </td>
                        <td>{item.id}</td>
                        <td><strong>{item.nombre}</strong></td>
                        <td>{item.email}</td>
                        <td>{item.telefono}</td>
                        <td>
                            <div className="flex flex-col gap-1">
                                <span className="badge-specialty-zone">
                                    {item.especialidad} / {item.zonaAsignada}
                                </span>
                            </div>
                        </td>
                        <td>
                            <button className="btn-action-svg-edit" onClick={() => onEdit(item)} title="Editar Asesor">
                                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="table-icon-svg"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path></svg>
                            </button>
                            <button className="btn-action-svg-delete" onClick={() => onDelete(item.id, 'asesores')} title="Eliminar Asesor">
                                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="table-icon-svg"><polyline points="3 6 5 6 21 6"></polyline><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path><line x1="10" y1="11" x2="10" y2="17"></line><line x1="14" y1="11" x2="14" y2="17"></line></svg>
                            </button>
                        </td>
                    </tr>
                ))
            )}
            </tbody>
        </table>
    );
}

// ==========================================================================
// 4. ENRUTADOR PRINCIPAL (AdminTables)
// ==========================================================================
function AdminTables({ seccion, data = [], onEdit, onDelete }) {
    switch (seccion) {
        case 'inmuebles':
            return <InmueblesTable data={data} onEdit={onEdit} onDelete={onDelete} />;
        case 'clientes':
            return <ClientesTable data={data} onEdit={onEdit} onDelete={onDelete} />;
        case 'asesores':
            return <AsesoresTable data={data} onEdit={onEdit} onDelete={onDelete} />;
        default:
            return <div className="table-empty-row">Sección no válida</div>;
    }
}

export default AdminTables;
