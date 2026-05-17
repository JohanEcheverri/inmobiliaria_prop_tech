import { useNavigate } from 'react-router-dom';
import logo from '../assets/logo.png';
import './Layout.css';

const Layout = ({ children, actions, contentClassName = '' }) => {
    const navigate = useNavigate();

    return (
        <div className="layout">
            <header className="layout-header">
                <div className="layout-brand" onClick={() => navigate('/')}>
                    <img className="layout-logo" src={logo} alt="DomusTech" />
                    <div className="layout-title">
                        <span className="layout-title-main">DOMUS</span>
                        <span className="layout-title-accent">TECH</span>
                    </div>
                </div>

                {actions && <div className="layout-actions">{actions}</div>}
            </header>

            <main className={`layout-main ${contentClassName}`}>
                {children}
            </main>

            <footer className="layout-footer">
                <p>&copy; 2026 DomusTech - Ingeniería de Sistemas UQ</p>
            </footer>
        </div>
    );
};

export default Layout;
