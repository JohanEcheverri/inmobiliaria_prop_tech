import React, { useState, useEffect } from 'react';
import axios from 'axios';
import axiosDirect from 'axios';

const API_BASE_URL = "http://localhost:8080/api";

function AdminModal({ seccion, datos, onClose, onSuccess }) {
    const isEdit = !!datos;
    const [showPassword, setShowPassword] = useState(false);
    const [error, setError] = useState('');

    // Estado unificado usando las propiedades exactas que tus DTOs y base de datos esperan
    const [formData, setFormData] = useState({
        // Inmuebles
        codigo: '', direccion: '', ciudad: '', barrio: '', tipoInmueble: 'casa',
        finalidad: 'venta', precio: '', area: '', habitaciones: '', banos: '',
        estadoInmueble: '', disponibilidad: 'Disponible', asesorResponsable: '',

        // Usuarios (Sincronizado al 100% con Register y ClienteRequest DTO)
        identificacion: '', // Se mapeará a "id" en el payload del Cliente
        nombre: '',
        email: '',          // Cambiado de 'correo' a 'email'
        password: '',       // Cambiado de 'contrasenia' a 'password'
        telefono: '',
        fotoPerfil: null,
        especialidad: ''
    });

    useEffect(() => {
        if (isEdit && datos) {
            // Mapeamos los datos entrantes al estado del formulario local
            setFormData(prev => ({
                ...prev,
                ...datos,
                identificacion: datos.id || datos.identificacion || '',
                email: datos.email || datos.correo || '',
                password: '' // Vacía por seguridad en modo edición
            }));
        }
    }, [datos, isEdit]);

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleFileChange = (e) => {
        const file = e.target.files[0];
        if (file) {
            // Aquí puedes manejar la carga temporal o guardarlo en el estado si usas MultipartForm
            const imageUrl = URL.createObjectURL(file);
            setFormData(prev => ({ ...prev, fotoPerfil: imageUrl }));
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');

        let endpoint = `${API_BASE_URL}/${seccion}`;
        let payload = {};

        try {
            // Construcción del Payload estructural según la sección
            if (seccion === 'inmuebles') {
                payload = {
                    codigo: formData.codigo,
                    direccion: formData.direccion,
                    ciudad: formData.ciudad,
                    barrio: formData.barrio,
                    tipoInmueble: formData.tipoInmueble,
                    finalidad: formData.finalidad,
                    precio: formData.precio,
                    area: formData.area,
                    habitaciones: formData.habitaciones,
                    banos: formData.banos,
                    estadoInmueble: formData.estadoInmueble,
                    disponibilidad: formData.disponibilidad,
                    asesorResponsable: formData.asesorResponsable
                };
            } else if (seccion === 'clientes') {
                // Estructura idéntica a tu Register.jsx funcional
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
                // Estructura adaptada para los Asesores
                payload = {
                    id: formData.identificacion,
                    nombre: formData.nombre,
                    email: formData.email,
                    telefono: formData.telefono,
                    password: formData.password,
                    fotoPerfil: formData.fotoPerfil || null,
                    especialidad: formData.especialidad || 'General'
                };
            }

            // Si estamos editando y no cambiaron la contraseña, la quitamos del payload para no sobreescribirla
            if (isEdit && !formData.password) {
                delete payload.password;
            }

            // Ejecución de la petición HTTP
            if (isEdit) {
                const idRegistro = formData.identificacion;
                await axiosDirect.put(`${endpoint}/${idRegistro}`, payload);
            } else {
                await axiosDirect.post(endpoint, payload);
            }

            if (onSuccess) onSuccess();
            onClose();
        } catch (err) {
            console.error("Error al guardar en el modal:", err);
            if (err.response) {
                setError(err.response.data.error || `No se pudo procesar la solicitud en ${seccion}`);
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
                                <label>Dirección</label>
                                <input type="text" name="direccion" value={formData.direccion} onChange={handleInputChange} required />
                            </div>
                            <div className="modal-group">
                                <label>Ciudad</label>
                                <input type="text" name="ciudad" value={formData.ciudad} onChange={handleInputChange} required />
                            </div>
                            <div className="modal-group">
                                <label>Barrio o Zona</label>
                                <input type="text" name="barrio" value={formData.barrio} onChange={handleInputChange} required />
                            </div>
                            <div className="modal-group">
                                <label>Tipo de Inmueble</label>
                                <select name="tipoInmueble" value={formData.tipoInmueble} onChange={handleInputChange}>
                                    <option value="casa">Casa</option>
                                    <option value="apartamento">Apartamento</option>
                                    <option value="local comercial">Local Comercial</option>
                                    <option value="oficina">Oficina</option>
                                </select>
                            </div>
                            <div className="modal-group">
                                <label>Finalidad</label>
                                <select name="finalidad" value={formData.finalidad} onChange={handleInputChange}>
                                    <option value="venta">Venta</option>
                                    <option value="arriendo">Arriendo</option>
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
                                <input type="text" name="estadoInmueble" value={formData.estadoInmueble} onChange={handleInputChange} required />
                            </div>
                            <div className="modal-group">
                                <label>Disponibilidad</label>
                                <select name="disponibilidad" value={formData.disponibilidad} onChange={handleInputChange}>
                                    <option value="Disponible">Disponible</option>
                                    <option value="Vendido">Vendido</option>
                                    <option value="Arrendado">Arrendado</option>
                                </select>
                            </div>
                        </div>
                    ) : (
                        /* ESTRUCTURA DE USUARIOS UNIFICADA (CLIENTE / ASESOR) */
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

                            {seccion === 'asesores' && (
                                <div className="modal-group">
                                    <label>Especialidad / Zona Inicial</label>
                                    <input type="text" name="especialidad" value={formData.especialidad} onChange={handleInputChange} placeholder="Ej: Norte / Residencial" />
                                </div>
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