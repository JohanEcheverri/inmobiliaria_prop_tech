import Layout from '../components/Layout';
import {useNavigate} from "react-router-dom";
import {useEffect} from "react";

function ClienteDashboard() {
    const navigate = useNavigate();

    useEffect(() => {
        const session = JSON.parse(localStorage.getItem("user_session"));
        if (!session || session.rol !== 'CLIENTE') {
            navigate('/login');
        }
    }, [navigate]);

    return (
        <Layout contentClassName="page-placeholder">
            <h1>Dashboard de Cliente</h1>
            <p>Consulta inmuebles recomendados, visitas programadas y el estado de tus solicitudes.</p>
        </Layout>
    );
}

export default ClienteDashboard;
