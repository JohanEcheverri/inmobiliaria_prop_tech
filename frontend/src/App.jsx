import { Routes, Route } from 'react-router-dom';
import Login from './Login';
import Portada from './Portada';
import Register from './Register';
import AdminDashboard from './dashboards/AdminDashboard';
import AsesorDashboard from './dashboards/AsesorDashboard';
import ClienteDashboard from './dashboards/ClienteDashboard';

function App() {
    return (
        <Routes>
            {/* Ahora la raíz es la Portada de DomusTech */}
            <Route path="/" element={<Portada />} />

            {/* El login ahora está en su propia URL */}
            <Route path="/login" element={<Login />} />

            <Route path="/register" element={<Register />} />
            <Route path="/registro" element={<Register />} />

            {/* Vistas según el Rol (Dashboards) */}
            <Route path="/cliente-dashboard" element={<ClienteDashboard />} />
            <Route path="/admin-dashboard" element={<AdminDashboard />} />
            <Route path="/asesor-dashboard" element={<AsesorDashboard />} />
            <Route path="/asesor-panel" element={<AsesorDashboard />} />
            <Route path="/catalogo" element={<ClienteDashboard />} />
        </Routes>
    );
}

export default App;
