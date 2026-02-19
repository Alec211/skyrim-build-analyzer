import { NavLink } from 'react-router-dom';
import './Navbar.css';

export default function Navbar() {
  return (
    <nav className="navbar">
      <div className="navbar-brand">Skyrim Build Analyzer</div>
      <div className="navbar-links">
        <NavLink to="/" end>Home</NavLink>
        <NavLink to="/tournament">Tournament</NavLink>
        <NavLink to="/encounter">Encounter</NavLink>
      </div>
    </nav>
  );
}
