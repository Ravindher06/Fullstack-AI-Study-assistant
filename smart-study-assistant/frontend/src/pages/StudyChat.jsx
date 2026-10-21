import React, { useState, useRef, useEffect } from 'react';
import { Send } from 'lucide-react';
import { studyApi } from '../api/studyApi';

const StudyChat = () => {
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const endRef = useRef(null);

  useEffect(() => { endRef.current?.scrollIntoView({ behavior: 'smooth' }); }, [messages, loading]);

  const send = async () => {
    const question = input.trim();
    if (!question || loading) return;
    setMessages((m) => [...m, { role: 'user', text: question }]);
    setInput('');
    setLoading(true);
    try {
      const res = await studyApi.askQuestion(question);
      setMessages((m) => [...m, { role: 'bot', text: res.answer, sources: res.sources }]);
    } catch (e) {
      setMessages((m) => [...m, { role: 'bot', text: `Error: ${e.message}`, error: true }]);
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <h1>Study Chat</h1>
      <div className="card chat-box">
        {messages.length === 0 && <span className="muted">Ask something about your uploaded material.</span>}
        {messages.map((m, i) => (
          <div key={i} className={`msg ${m.role}`}>
            <span className={m.error ? 'error' : ''}>{m.text}</span>
            {m.sources?.length > 0 && <div className="sources">Sources: {m.sources.join(', ')}</div>}
          </div>
        ))}
        {loading && <div className="msg bot muted">Thinking…</div>}
        <div ref={endRef} />
      </div>
      <div className="row">
        <input type="text" style={{ flex: 1 }} value={input} placeholder="Type your question…"
               onChange={(e) => setInput(e.target.value)}
               onKeyDown={(e) => e.key === 'Enter' && send()} />
        <button className="btn-primary" onClick={send} disabled={loading || !input.trim()}>
          <Send size={16} style={{ verticalAlign: 'text-bottom' }} /> Send
        </button>
      </div>
    </>
  );
};

export default StudyChat;
