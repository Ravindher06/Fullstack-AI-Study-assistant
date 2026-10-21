import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Layout from './components/Layout';
import Dashboard from './pages/Dashboard';
import StudyChat from './pages/StudyChat';
import Quiz from './pages/Quiz';
import Documents from './pages/Documents';
import './styles/global.css';

const App = () => (
  <Router>
    <Layout>
      <Routes>
        <Route path="/" element={<Dashboard />} />
        <Route path="/chat" element={<StudyChat />} />
        <Route path="/quiz" element={<Quiz />} />
        <Route path="/documents" element={<Documents />} />
        <Route path="*" element={<Dashboard />} />
      </Routes>
    </Layout>
  </Router>
);

export default App;
