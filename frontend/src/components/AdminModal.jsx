import React, { useState, useEffect } from 'react';

function AdminModal({ seccion, datos, onClose }) {
    const isEdit = !!datos;

    // Estado unificado incluyendo los nuevos atributos requeridos para la base de datos real
    const [formData, setFormData] = useState({
        codigo: '', direccion: '', ciudad: '', barrio: '', tipoInmueble: 'casa',
        finalidad: 'venta', precio: '', area: '', habitaciones: '', banos: '',
        estadoInmueble: '', disponibilidad: 'Disponible', asesorResponsable: '',

        identificacion: '', nombre: '', correo: '', telefono: '',
        contrasena: '', fotoPerfil: '', especialidad: ''
    });

    useEffect(() => {
        if (isEdit && datos) {
            setFormData(prev => ({ ...prev, ...datos, contrasena: '' })); // Contraseña en blanco por seguridad al editar
        }
    }, [datos, isEdit]);

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleFileChange = (e) => {
        const file = e.target.files[0];
        if (file) {
            // Preparamos la lectura como Base64 o URL simulada para el backend
            const imageUrl = URL.createObjectURL(file);
            setFormData(prev => ({ ...prev, fotoPerfil: imageUrl }));
        }
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        console.log(`Enviando payload a la API de Spring Boot (${seccion}):`, formData);
        // Aquí conectarás tu axios.post o axios.put correspondientes
        onClose();
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
                        /* ESTRUCTURA PROFESIONAL MEJORADA PARA CLIENTES Y ASESORES (Doble Columna) */
                        <div className="modal-grid-inputs data-user-grid">

                            {/* Componente visual para cargar la foto de perfil en el lateral */}
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
                                    <label>Identificación / Cédula</label>
                                    <input type="text" name="identificacion" value={formData.identificacion} onChange={handleInputChange} disabled={isEdit} required />
                                </div>
                                <div className="modal-group">
                                    <label>Nombre Completo</label>
                                    <input type="text" name="nombre" value={formData.nombre} onChange={handleInputChange} required />
                                </div>
                            </div>

                            <div className="modal-group">
                                <label>Correo Electrónico</label>
                                <input type="email" name="correo" value={formData.correo} onChange={handleInputChange} required />
                            </div>
                            <div className="modal-group">
                                <label>Teléfono Movil</label>
                                <input type="tel" name="telefono" value={formData.telefono} onChange={handleInputChange} required />
                            </div>

                            <div className="modal-group">
                                <label>{isEdit ? 'Nueva Contraseña (Opcional)' : 'Contraseña de Acceso'}</label>
                                <input type="password" name="contrasena" value={formData.contrasena} onChange={handleInputChange} placeholder="••••••••" required={!isEdit} />
                            </div>

                            {seccion === 'asesores' && (
                                <div className="modal-group">
                                    <label>Especialidad / Zona Inicial</label>
                                    <input type="text" name="especialidad" value={formData.especialidad} onChange={handleInputChange} placeholder="Ej: Norte / Residencial" />
                                </div>
                            )}
                        </div>
                    )}

                    <div className="modal-footer-actions">
                        <button type="button" className="btn-cancel-modal" onClick={onClose}>Cancelar</button>
                        <button type="submit" className="btn-submit-modal">Guardar en Base de Datos</button>
                    </div>
                </form>
            </div>
        </div>
    );
}

export default AdminModal;