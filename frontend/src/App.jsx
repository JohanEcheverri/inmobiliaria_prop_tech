import { Routes, Route } from 'react-router-dom';
import Login from './Login';

function App() {
    return (
        <Routes>
            <Route path="/" element={<Login />} />

            {/* Vistas según el Rol */}
            <Route path="/admin-dashboard" element={<AdminPanel />} />
            <Route path="/asesor-panel" element={<AsesorPanel />} />
            <Route path="/catalogo" element={<CatalogoCliente />} />
        </Routes>
    );
}

// Componentes temporales (luego los creas en archivos separados)
const AdminPanel = () => <div className="p-4"><h1>Panel de Administración</h1><p>Gestión de usuarios y reportes globales.</p></div>;
const AsesorPanel = () => <div className="p-4"><h1>Panel del Asesor</h1><p>Gestión de inmuebles y citas.</p></div>;
const CatalogoCliente = () => <div className="p-4"><h1>Catálogo de Inmuebles</h1><p>Busca tu próximo hogar.</p></div>;

export default App;