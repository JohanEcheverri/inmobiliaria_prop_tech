import Layout from '../components/Layout';
import {useNavigate} from "react-router-dom";
import {useEffect} from "react";

function AsesorDashboard() {
    const navigate = useNavigate();

    useEffect(() => {
        const session = JSON.parse(localStorage.getItem("user_session"));
        if (!session || session.rol !== 'ASESOR') {
            navigate('/login');
        }
    }, [navigate]);

    return (
        <Layout contentClassName="page-placeholder">
            <h1>Dashboard de Asesor</h1>
            <p>Gestiona inmuebles asignados, visitas, clientes activos y oportunidades comerciales.</p>
        </Layout>
    );
}

export default AsesorDashboard;
