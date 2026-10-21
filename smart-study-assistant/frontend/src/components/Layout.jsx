import React from 'react';
import { NavLink } from 'react-router-dom';
import { BookOpen } from 'lucide-react';

const Layout = ({ children }) => (
  <>
    <nav className="navbar">
      <span className="brand"><BookOpen size={18} style={{ verticalAlign: 'text-bottom' }} /> Smart Study Assistant</span>
      <NavLink to="/" end>Dashboard</NavLink>
      <NavLink to="/chat">Chat</NavLink>
      <NavLink to="/quiz">Quiz</NavLink>
      <NavLink to="/documents">Documents</NavLink>
    </nav>
    <main className="container">{children}</main>
  </>
);

export default Layout;
