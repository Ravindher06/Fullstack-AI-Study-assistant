import React, { useEffect, useState } from 'react';
import { Trash2 } from 'lucide-react';
import { studyApi } from '../api/studyApi';

const Documents = () => {
  const [docs, setDocs] = useState([]);
  const [error, setError] = useState(null);

  const load = async () => {
    try { setDocs(await studyApi.listDocuments()); setError(null); }
    catch (e) { setError(e.message); }
  };
  useEffect(() => { load(); }, []);

  const remove = async (id) => {
    try { await studyApi.deleteDocument(id); await load(); }
    catch (e) { setError(e.message); }
  };

  return (
    <>
      <h1>Documents</h1>
      {error && <p className="error">{error}</p>}
      <div className="card">
        {docs.length === 0 ? <span className="muted">No documents uploaded yet.</span> : (
          <table>
            <thead><tr><th>File</th><th>Size</th><th>Uploaded</th><th></th></tr></thead>
            <tbody>
              {docs.map((d) => (
                <tr key={d.id}>
                  <td>{d.filename}</td>
                  <td>{(d.size / 1024).toFixed(1)} KB</td>
                  <td>{d.uploadDate ? new Date(d.uploadDate).toLocaleString() : ''}</td>
                  <td><button className="btn-danger" onClick={() => remove(d.id)}><Trash2 size={14} /></button></td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </>
  );
};

export default Documents;
