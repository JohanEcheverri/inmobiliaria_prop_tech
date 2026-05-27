// ==========================================================================
// 1. COMPONENTE: TABLA DE INMUEBLES
// ==========================================================================
function InmueblesTable({ data, onEdit, onDelete }) {
    return (
        <table className="domustech-admin-table">
            <thead>
            <tr>
                <th>Código</th>
                <th>Foto</th>
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
                    <td colSpan="9" className="table-empty-row">No hay inmuebles registrados.</td>
                </tr>
            ) : (
                data.map((inm) => {
                    const fotoPrincipal = Array.isArray(inm.imagenes) && inm.imagenes.length > 0
                        ? inm.imagenes[0]
                        : null;
                    return (
                        <tr key={inm.codigo}>
                            <td><strong>{inm.codigo}</strong></td>
                            <td>
                                <div className="table-avatar-preview property-table-preview">
                                    {fotoPrincipal ? (
                                        <img src={fotoPrincipal} alt={`Inmueble ${inm.codigo}`} className="table-avatar-img property-table-img" />
                                    ) : (
                                        <div className="table-avatar-placeholder property-table-placeholder">🏠</div>
                                    )}
                                </div>
                            </td>

                            <td>
                                <span className={`badge-type ${inm.finalidad?.toLowerCase()}`}>
                                    {inm.tipoInmueble} ({inm.finalidad})
                                </span>
                            </td>
                            <td>{inm.direccion}, ({inm.ciudad})</td>
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
                    );
                })
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

const formatCurrency = (value) => {
    const amount = Number(value);
    return Number.isFinite(amount) ? `$${amount.toLocaleString()}` : 'N/A';
};

const formatDate = (value) => {
    if (!value) return 'N/A';
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? value : date.toLocaleString();
};

const getInmuebleZona = (item) => item.zona || item.inmuebleZona || item.inmueble?.zona || item.inmueble?.barrio?.zona || 'N/A';
const getInmuebleCodigo = (item) => item.inmuebleCodigo || item.inmueble?.codigo || 'N/A';
const getInmuebleDireccion = (item) => item.inmuebleDireccion || item.inmueble?.direccion || item.inmueble?.direccionBarrio || 'N/A';
const getClienteNombre = (item) => item.clienteNombre || item.cliente?.nombre || 'N/A';
const getAsesorNombre = (item) => item.asesorNombre || item.asesor?.nombre || item.asesotAsignado?.nombre || 'N/A';

function AlertasTable({ data, onDelete, onAtender }) {
    return (
        <table className="domustech-admin-table">
            <thead>
            <tr>
                <th>Prioridad</th>
                <th>Tipo</th>
                <th>Título</th>
                <th>Referencia</th>
                <th>Fecha</th>
                <th>Estado</th>
                <th>Acciones</th>
            </tr>
            </thead>
            <tbody>
            {data.length === 0 ? (
                <tr>
                    <td colSpan="7" className="table-empty-row">No hay alertas para mostrar.</td>
                </tr>
            ) : (
                data.map((alerta) => (
                    <tr key={alerta.codigo}>
                        <td><span className={`badge-specialty-zone ${alerta.prioridad?.toLowerCase()}`}>{alerta.prioridad}</span></td>
                        <td>{alerta.tipo}</td>
                        <td>
                            <strong>{alerta.titulo}</strong>
                            <div>{alerta.descripcion}</div>
                        </td>
                        <td>{alerta.referenciaId}</td>
                        <td>{formatDate(alerta.fechaGeneracion)}</td>
                        <td>{alerta.atendida ? 'Atendida' : 'Pendiente'}</td>
                        <td>
                            {!alerta.atendida && (
                                <button className="btn-action-svg-edit" onClick={() => onAtender(alerta.codigo)} title="Atender alerta">
                                    ✓
                                </button>
                            )}
                            <button className="btn-action-svg-delete" onClick={() => onDelete(alerta.codigo, 'alertas')} title="Eliminar alerta">
                                ×
                            </button>
                        </td>
                    </tr>
                ))
            )}
            </tbody>
        </table>
    );
}

function ComportamientoTable({ data, onResolver }) {
    return (
        <table className="domustech-admin-table">
            <thead>
            <tr>
                <th>Nivel</th>
                <th>Tipo</th>
                <th>Descripción</th>
                <th>Referencia</th>
                <th>Fecha</th>
                <th>Estado</th>
                <th>Acciones</th>
            </tr>
            </thead>
            <tbody>
            {data.length === 0 ? (
                <tr>
                    <td colSpan="7" className="table-empty-row">No hay registros de comportamiento.</td>
                </tr>
            ) : (
                data.map((registro) => (
                    <tr key={registro.id}>
                        <td><span className={`badge-specialty-zone ${registro.nivelAtencion?.toLowerCase()}`}>{registro.nivelAtencion}</span></td>
                        <td>{registro.tipoComportamiento}</td>
                        <td>
                            <strong>{registro.descripcion}</strong>
                            {registro.observaciones && <div>{registro.observaciones}</div>}
                        </td>
                        <td>{registro.referenciaId}</td>
                        <td>{formatDate(registro.fechaDeteccion)}</td>
                        <td>{registro.resuelto ? 'Resuelto' : 'Activo'}</td>
                        <td>
                            {!registro.resuelto && (
                                <button className="btn-action-svg-edit" onClick={() => onResolver(registro.id)} title="Resolver registro">
                                    ✓
                                </button>
                            )}
                        </td>
                    </tr>
                ))
            )}
            </tbody>
        </table>
    );
}

function ReportesTable({ data, reporteSubView, isLoadingReporte }) {
    if (isLoadingReporte) {
        return <div className="table-empty-row">Cargando reporte...</div>;
    }

    if (reporteSubView === 'visitas') {
        return (
            <table className="domustech-admin-table">
                <thead>
                <tr>
                    <th>Código</th>
                    <th>Cliente</th>
                    <th>Inmueble</th>
                    <th>Zona</th>
                    <th>Fecha</th>
                    <th>Estado</th>
                    <th>Asesor</th>
                </tr>
                </thead>
                <tbody>
                {data.length === 0 ? (
                    <tr><td colSpan="7" className="table-empty-row">No hay visitas para este reporte.</td></tr>
                ) : data.map((visita) => (
                    <tr key={visita.codigo}>
                        <td><strong>{visita.codigo}</strong></td>
                        <td>{getClienteNombre(visita)}</td>
                        <td>{getInmuebleCodigo(visita)} · {getInmuebleDireccion(visita)}</td>
                        <td>{getInmuebleZona(visita)}</td>
                        <td>{visita.fecha} {visita.hora || ''}</td>
                        <td>{visita.estado}</td>
                        <td>{getAsesorNombre(visita)}</td>
                    </tr>
                ))}
                </tbody>
            </table>
        );
    }

    return (
        <table className="domustech-admin-table">
            <thead>
            <tr>
                <th>Código</th>
                <th>Estado</th>
                <th>Inmueble</th>
                <th>Zona</th>
                <th>Cliente</th>
                <th>Valor</th>
                <th>Fecha</th>
            </tr>
            </thead>
            <tbody>
            {data.length === 0 ? (
                <tr><td colSpan="7" className="table-empty-row">No hay operaciones para este reporte.</td></tr>
            ) : data.map((operacion) => (
                <tr key={operacion.codigo}>
                    <td><strong>{operacion.codigo}</strong></td>
                    <td>{operacion.estado}</td>
                    <td>{getInmuebleCodigo(operacion)} · {getInmuebleDireccion(operacion)}</td>
                    <td>{getInmuebleZona(operacion)}</td>
                    <td>{getClienteNombre(operacion)}</td>
                    <td>{formatCurrency(operacion.valorAcordado || operacion.inmueblePrecio)}</td>
                    <td>{formatDate(operacion.fecha)}</td>
                </tr>
            ))}
            </tbody>
        </table>
    );
}

// ==========================================================================
// 4. ENRUTADOR PRINCIPAL (AdminTables)
// ==========================================================================
function AdminTables({ seccion, data = [], onEdit, onDelete, onAtender, onResolver, reporteSubView, isLoadingReporte }) {
    switch (seccion) {
        case 'inmuebles':
            return <InmueblesTable data={data} onEdit={onEdit} onDelete={onDelete} />;
        case 'clientes':
            return <ClientesTable data={data} onEdit={onEdit} onDelete={onDelete} />;
        case 'asesores':
            return <AsesoresTable data={data} onEdit={onEdit} onDelete={onDelete} />;
        case 'alertas':
            return <AlertasTable data={data} onDelete={onDelete} onAtender={onAtender} />;
        case 'comportamiento':
            return <ComportamientoTable data={data} onResolver={onResolver} />;
        case 'reportes':
            return <ReportesTable data={data} reporteSubView={reporteSubView} isLoadingReporte={isLoadingReporte} />;
        default:
            return <div className="table-empty-row">Sección no válida</div>;
    }
}

export default AdminTables;
