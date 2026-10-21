import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { Upload } from 'lucide-react';
import { studyApi } from '../api/studyApi';

const Dashboard = () => {
  const [file, setFile] = useState(null);
  const [status, setStatus] = useState(null);
  const [loading, setLoading] = useState(false);

  const handleUpload = async () => {
    if (!file) return;
    setLoading(true);
    setStatus(null);
    try {
      const msg = await studyApi.uploadMaterial(file);
      setStatus({ ok: true, text: msg });
      setFile(null);
    } catch (e) {
      setStatus({ ok: false, text: e.message });
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <div>
        <h1>Dashboard</h1>
        <p className="muted">Upload study material (.txt, .md, .pdf), then chat with it or generate a quiz.</p>
      </div>

      <div className="card">
        <h3><Upload size={18} style={{ verticalAlign: 'text-bottom' }} /> Upload material</h3>
        <div className="row">
          <input type="file" accept=".txt,.md,.pdf,.csv,.json"
                 onChange={(e) => setFile(e.target.files[0] || null)} />
          <button className="btn-primary" onClick={handleUpload} disabled={!file || loading}>
            {loading ? 'Embedding… (can take a minute)' : 'Upload'}
          </button>
        </div>
        {status && <p className={status.ok ? 'success' : 'error'}>{status.text}</p>}
      </div>

      <div className="row">
        <Link to="/chat" className="card" style={{ flex: 1, textDecoration: 'none', color: 'inherit' }}>
          <h3>Ask questions →</h3><span className="muted">Chat with your notes.</span>
        </Link>
        <Link to="/quiz" className="card" style={{ flex: 1, textDecoration: 'none', color: 'inherit' }}>
          <h3>Generate a quiz →</h3><span className="muted">Test yourself on a topic.</span>
        </Link>
      </div>
    </>
  );
};

export default Dashboard;
