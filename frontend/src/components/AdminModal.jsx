import { useState } from 'react';
import axiosDirect from 'axios';
import { apiUrl } from '../api';

const formDataBase = {
    // Inmuebles
    codigo: '', direccionBarrio: '', direccion: '', ciudad: '', departamento: '', barrio: '', zona: 'CENTRO', tipoInmueble: 'CASA',
    finalidad: 'VENTA', precio: '', area: '', habitaciones: '', banos: '',
    estadoInmueble: 'DISPONIBLE', disponibilidad: 'DISPONIBLE', asesorResponsable: '', imagenes: [],

    // Usuarios comunes
    identificacion: '',
    nombre: '',
    email: '',
    password: '',
    telefono: '',
    fotoPerfil: null,

    // Exclusivos Asesores
    zonaAsignada: 'CENTRO',
    especialidad: 'CASA'
};

const crearFormDataInicial = (datos) => ({
    ...formDataBase,
    ...(datos || {}),
    identificacion: datos?.id || datos?.identificacion || '',
    email: datos?.email || datos?.correo || '',
    zonaAsignada: datos?.zonaAsignada || 'CENTRO',
    zona: datos?.zona || 'CENTRO',
    direccionBarrio: datos?.direccionBarrio || datos?.direccion || datos?.barrio || '',
    departamento: datos?.departamento || '',


    imagenes: (() => {
        if (!datos) return [];
        // Buscamos la lista de imágenes dentro del objeto 'datos'
        const imgsCandidate = datos.imagenes || datos.imagen || [];
        const arr = Array.isArray(imgsCandidate) ? imgsCandidate : [imgsCandidate];

        return arr.filter(Boolean).map(i => {
            if (typeof i === 'object') return i.url || i.path || String(i);
            if (typeof i !== 'string') return String(i);
            if (i.startsWith('data:') || i.startsWith('http')) return i;
            if (i.startsWith('/')) return `${window.location.origin}${i}`;
            return i;
        });
    })(),


    especialidad: datos?.especialidad || 'CASA',
    estadoInmueble: datos?.estadoInmueble || datos?.estado || 'DISPONIBLE',
    disponibilidad: datos?.disponibilidad || datos?.estado || 'DISPONIBLE',
    asesorResponsable: datos?.asesorId || datos?.asesorResponsable || '',
    password: ''
});

function AdminModal({ seccion, datos, onClose, onSuccess }) {
    const isEdit = !!datos;
    const [showPassword, setShowPassword] = useState(false);
    const [error, setError] = useState('');
    const [formData, setFormData] = useState(() => crearFormDataInicial(datos));

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleFileChange = (e) => {
        const file = e.target.files[0];
        if (file) {
            const reader = new FileReader();
            reader.onloadend = () => {
                setFormData(prev => ({ ...prev, fotoPerfil: reader.result }));
            };
            reader.readAsDataURL(file);
        }
    };

    const handlePropertyImagesChange = (e) => {
        const files = Array.from(e.target.files || []).slice(0, 6);
        if (files.length === 0) {
            return;
        }

        Promise.all(files.map(file => new Promise((resolve, reject) => {
            const reader = new FileReader();
            reader.onloadend = () => resolve(reader.result);
            reader.onerror = reject;
            reader.readAsDataURL(file);
        }))).then(images => {
            setFormData(prev => ({ ...prev, imagenes: images }));
        }).catch(() => {
            setError('No se pudieron cargar las imágenes del inmueble');
        });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');

        let endpoint = apiUrl(`/${seccion}`);
        let payload = {};

        try {
            if (seccion === 'inmuebles') {
                payload = {
                    codigo: formData.codigo,
                    direccion: formData.direccionBarrio,
                    direccionBarrio: formData.direccionBarrio,
                    ciudad: formData.ciudad,
                    departamento: formData.departamento,
                    barrio: formData.direccionBarrio,
                    zona: formData.zona,
                    tipoInmueble: formData.tipoInmueble,
                    finalidad: formData.finalidad,
                    precio: formData.precio,
                    area: formData.area,
                    habitaciones: formData.habitaciones,
                    banos: formData.banos,
                    estadoInmueble: formData.estadoInmueble,
                    disponibilidad: formData.disponibilidad,
                    asesorResponsable: formData.asesorResponsable,
                    imagenes: formData.imagenes
                };
            } else if (seccion === 'clientes') {
                payload = {
                    id: formData.identificacion,
                    nombre: formData.nombre,
                    email: formData.email,
                    telefono: formData.telefono,
                    password: formData.password,
                    fotoPerfil: formData.fotoPerfil || null,
                    tipoCliente: formData.tipoCliente || 'COMPRADOR',
                    zonaInteres: formData.zonaInteres || 'CENTRO',
                    presupuesto: formData.presupuesto || null,
                    tipoInmuebleDeseado: formData.tipoInmuebleDeseado || null,
                    numeroHabitacionesDeseadas: formData.numeroHabitacionesDeseadas || 0,
                    estadoBusqueda: formData.estadoBusqueda || 'BUSCANDO'
                };
            } else if (seccion === 'asesores') {
                // Envía las variables mapeando perfectamente con tu AsesorRequest DTO
                payload = {
                    id: formData.identificacion,
                    nombre: formData.nombre,
                    email: formData.email,
                    telefono: formData.telefono,
                    password: formData.password,
                    fotoPerfil: formData.fotoPerfil || null,
                    zonaAsignada: formData.zonaAsignada, // Enum Zona
                    especialidad: formData.especialidad   // Enum TipoInmueble
                };
            }

            if (isEdit && !formData.password) {
                delete payload.password;
            }

            if (isEdit) {
                const idRegistro = seccion === 'inmuebles' ? formData.codigo : formData.identificacion;
                await axiosDirect.put(`${endpoint}/${idRegistro}`, payload);
            } else {
                await axiosDirect.post(endpoint, payload);
            }

            if (onSuccess) onSuccess();
            onClose();
        } catch (err) {
            console.error("Error al guardar en el modal:", err);
            if (err.response) {
                setError(err.response.data?.mensaje || err.response.data?.message || err.response.data?.error || err.response.data || `No se pudo procesar la solicitud en ${seccion}`);
            } else {
                setError('No se pudo conectar con el servidor backend');
            }
        }
    };

    return (
        <div className="modal-backdrop">
            <div className="modal-window-card">
                <div className="modal-window-header">
                    <h2>{isEdit ? 'Modificar' : 'Registrar'} {seccion.toUpperCase()}</h2>
                    <button className="btn-close-modal" onClick={onClose}>&times;</button>
                </div>
                <form onSubmit={handleSubmit} className="modal-form-body">

                    {seccion === 'inmuebles' ? (
                        <div className="modal-grid-inputs">
                            <div className="modal-group">
                                <label>Código</label>
                                <input type="text" name="codigo" value={formData.codigo} onChange={handleInputChange} disabled={isEdit} required />
                            </div>
                            <div className="modal-group">
                                <label>Dirección / Barrio</label>
                                <input type="text" name="direccionBarrio" value={formData.direccionBarrio} onChange={handleInputChange} required />
                            </div>
                            <div className="modal-group">
                                <label>Ciudad</label>
                                <input type="text" name="ciudad" value={formData.ciudad} onChange={handleInputChange} required />
                            </div>
                            <div className="modal-group">
                                <label>Departamento</label>
                                <input type="text" name="departamento" value={formData.departamento} onChange={handleInputChange} />
                            </div>
                            <div className="modal-group">
                                <label>Zona</label>
                                <select name="zona" value={formData.zona} onChange={handleInputChange}>
                                    <option value="NORTE">Norte</option>
                                    <option value="SUR">Sur</option>
                                    <option value="ESTE">Este</option>
                                    <option value="OESTE">Oeste</option>
                                    <option value="CENTRO">Centro</option>
                                </select>
                            </div>
                            <div className="modal-group">
                                <label>Tipo de Inmueble</label>
                                <select name="tipoInmueble" value={formData.tipoInmueble} onChange={handleInputChange}>
                                    <option value="CASA">Casa</option>
                                    <option value="APARTAMENTO">Apartamento</option>
                                    <option value="LOCAL_COMERCIAL">Local Comercial</option>
                                    <option value="OFICINA">Oficina</option>
                                    <option value="LOTE">Lote</option>
                                    <option value="BODEGA">Bodega</option>
                                </select>
                            </div>
                            <div className="modal-group">
                                <label>Finalidad</label>
                                <select name="finalidad" value={formData.finalidad} onChange={handleInputChange}>
                                    <option value="VENTA">Venta</option>
                                    <option value="ARRENDAMIENTO">Arriendo</option>
                                </select>
                            </div>
                            <div className="modal-group">
                                <label>Precio</label>
                                <input type="number" name="precio" value={formData.precio} onChange={handleInputChange} required />
                            </div>
                            <div className="modal-group">
                                <label>Área (m²)</label>
                                <input type="number" name="area" value={formData.area} onChange={handleInputChange} required />
                            </div>
                            <div className="modal-group">
                                <label>Habitaciones</label>
                                <input type="number" name="habitaciones" value={formData.habitaciones} onChange={handleInputChange} required />
                            </div>
                            <div className="modal-group">
                                <label>Baños</label>
                                <input type="number" name="banos" value={formData.banos} onChange={handleInputChange} required />
                            </div>
                            <div className="modal-group">
                                <label>Estado del Inmueble</label>
                                <select name="estadoInmueble" value={formData.estadoInmueble} onChange={(e) => {
                                    handleInputChange(e);
                                    setFormData(prev => ({ ...prev, disponibilidad: e.target.value }));
                                }}>
                                    <option value="DISPONIBLE">Disponible</option>
                                    <option value="VENDIDO">Vendido</option>
                                    <option value="ARRENDADO">Arrendado</option>
                                    <option value="RESERVADO">Reservado</option>
                                </select>
                            </div>
                            <div className="modal-group">
                                <label>Disponibilidad</label>
                                <select name="disponibilidad" value={formData.disponibilidad} onChange={handleInputChange}>
                                    <option value="DISPONIBLE">Disponible</option>
                                    <option value="VENDIDO">Vendido</option>
                                    <option value="ARRENDADO">Arrendado</option>
                                    <option value="RESERVADO">Reservado</option>
                                </select>
                            </div>
                            <div className="modal-group">
                                <label>ID o Email del Asesor</label>
                                <input type="text" name="asesorResponsable" value={formData.asesorResponsable} onChange={handleInputChange} placeholder="Opcional" />
                            </div>
                            <div className="modal-group property-images-group">
                                <label>Imágenes del Inmueble (3 a 6)</label>
                                <input type="file" accept="image/*" multiple onChange={handlePropertyImagesChange} />
                                <div className="property-images-preview">
                                    {formData.imagenes?.map((imagen, index) => (
                                        <img key={`${imagen}-${index}`} src={imagen} alt={`Inmueble ${index + 1}`} />
                                    ))}
                                </div>
                            </div>
                        </div>
                    ) : (
                        <div className="modal-grid-inputs data-user-grid">

                            <div className="modal-group user-avatar-upload-row">
                                <label>Foto de Perfil</label>
                                <div className="avatar-picker-container">
                                    <div className="avatar-picker-preview">
                                        {formData.fotoPerfil ? (
                                            <img src={formData.fotoPerfil} alt="Previsualización" />
                                        ) : (
                                            <span className="avatar-plus-icon">+</span>
                                        )}
                                    </div>
                                    <input type="file" accept="image/*" id="avatar-file-input" onChange={handleFileChange} />
                                    <label htmlFor="avatar-file-input" className="btn-select-avatar-file">Subir foto</label>
                                </div>
                            </div>

                            <div className="modal-user-fields-subgrid">
                                <div className="modal-group">
                                    <label>Identificación</label>
                                    <input type="text" name="identificacion" value={formData.identificacion} onChange={handleInputChange} disabled={isEdit} required />
                                </div>
                                <div className="modal-group">
                                    <label>Nombre Completo</label>
                                    <input type="text" name="nombre" value={formData.nombre} onChange={handleInputChange} required />
                                </div>
                            </div>

                            <div className="modal-group">
                                <label>Correo Electrónico</label>
                                <input type="email" name="email" value={formData.email} onChange={handleInputChange} required />
                            </div>
                            <div className="modal-group">
                                <label>Teléfono Móvil</label>
                                <input type="tel" name="telefono" value={formData.telefono} onChange={handleInputChange} required />
                            </div>

                            <div className="modal-group">
                                <label>{isEdit ? 'Nueva Contraseña (Opcional)' : 'Contraseña'}</label>
                                <div className="password-wrapper">
                                    <input
                                        type={showPassword ? "text" : "password"}
                                        name="password"
                                        value={formData.password}
                                        onChange={handleInputChange}
                                        placeholder="••••••••"
                                        required={!isEdit}
                                    />
                                    <button
                                        type="button"
                                        className="toggle-password"
                                        onClick={() => setShowPassword(!showPassword)}
                                        aria-label={showPassword ? "Ocultar contraseña" : "Mostrar contraseña"}
                                    >
                                        {showPassword ? (
                                            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="eye-icon">
                                                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                                                <circle cx="12" cy="12" r="3"></circle>
                                            </svg>
                                        ) : (
                                            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="eye-icon">
                                                <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path>
                                                <line x1="1" y1="1" x2="23" y2="23"></line>
                                            </svg>
                                        )}
                                    </button>
                                </div>
                            </div>

                            {/* SECCIÓN CORREGIDA CON ENUMS EXACTOS DE JAVA */}
                            {seccion === 'asesores' && (
                                <>
                                    <div className="modal-group">
                                        <label>Zona Asignada</label>
                                        <select name="zonaAsignada" value={formData.zonaAsignada} onChange={handleInputChange}>
                                            <option value="NORTE">Norte</option>
                                            <option value="SUR">Sur</option>
                                            <option value="ESTE">Este</option>
                                            <option value="OESTE">Oeste</option>
                                            <option value="CENTRO">Centro</option>
                                        </select>
                                    </div>
                                    <div className="modal-group">
                                        <label>Especialidad Inmueble</label>
                                        <select name="especialidad" value={formData.especialidad} onChange={handleInputChange}>
                                            <option value="CASA">Casa</option>
                                            <option value="APARTAMENTO">Apartamento</option>
                                            <option value="LOCAL_COMERCIAL">Local Comercial</option>
                                            <option value="OFICINA">Oficina</option>
                                            <option value="LOTE">Lote</option>
                                            <option value="BODEGA">Bodega</option>
                                        </select>
                                    </div>
                                </>
                            )}
                        </div>
                    )}

                    {error && <div className="error-message p-3 text-center" style={{ color: '#d9534f', backgroundColor: '#f2dede', border: '1px solid #ebccd1', borderRadius: '4px', marginTop: '15px' }}>{error}</div>}

                    <div className="modal-footer-actions">
                        <button type="button" className="btn-cancel-modal" onClick={onClose}>Cancelar</button>
                        <button type="submit" className="btn-submit-modal">Guardar</button>
                    </div>
                </form>
            </div>
        </div>
    );
}

export default AdminModal;
