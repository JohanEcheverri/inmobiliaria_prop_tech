import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Layout from '../components/Layout';

function AdminDashboard() {
    const navigate = useNavigate();

    useEffect(() => {
        const session = JSON.parse(localStorage.getItem("user_session"));
        // Si no hay sesión o el rol no es ADMIN, para afuera.
        if (!session || session.rol !== 'ADMINISTRADOR') {
            navigate('/login');
        }
    }, [navigate]);

    return (
        <Layout contentClassName="page-placeholder">
            <h1>Dashboard de Administrador</h1>
            {/* Aquí puedes usar los datos de la sesión si quieres */}
            <p>Bienvenido al panel de control global.</p>
        </Layout>
    );
}

export default AdminDashboard;