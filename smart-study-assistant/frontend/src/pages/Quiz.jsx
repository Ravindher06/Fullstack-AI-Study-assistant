import React, { useState } from 'react';
import { studyApi } from '../api/studyApi';

const Quiz = () => {
  const [topic, setTopic] = useState('');
  const [count, setCount] = useState(5);
  const [questions, setQuestions] = useState([]);
  const [answers, setAnswers] = useState({});
  const [submitted, setSubmitted] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const generate = async () => {
    setLoading(true); setError(null); setQuestions([]); setAnswers({}); setSubmitted(false);
    try {
      const data = await studyApi.generateQuiz(topic, Number(count));
      if (!Array.isArray(data) || data.length === 0) throw { message: 'The model returned no questions. Try again.' };
      setQuestions(data);
    } catch (e) {
      setError(e.message || 'Failed to generate quiz');
    } finally {
      setLoading(false);
    }
  };

  const score = questions.filter((q, i) => answers[i] === q.correctAnswer).length;

  const cls = (q, i, opt) => {
    if (!submitted) return answers[i] === opt ? 'option selected' : 'option';
    if (opt === q.correctAnswer) return 'option correct';
    if (answers[i] === opt) return 'option wrong';
    return 'option';
  };

  return (
    <>
      <h1>Quiz</h1>
      <div className="card row">
        <input type="text" style={{ flex: 1 }} placeholder="Topic (e.g. photosynthesis)"
               value={topic} onChange={(e) => setTopic(e.target.value)} />
        <input type="number" min="1" max="20" value={count} onChange={(e) => setCount(e.target.value)}
               style={{ width: 80 }} />
        <button className="btn-primary" onClick={generate} disabled={loading || !topic.trim()}>
          {loading ? 'Generating…' : 'Generate'}
        </button>
      </div>
      {error && <p className="error">{error}</p>}

      {questions.map((q, i) => (
        <div className="card" key={i}>
          <strong>{i + 1}. {q.questionText}</strong>
          {(q.multipleChoiceOptions || []).map((opt, j) => (
            <button key={j} className={cls(q, i, opt)} disabled={submitted}
                    onClick={() => setAnswers({ ...answers, [i]: opt })}>
              {opt}
            </button>
          ))}
        </div>
      ))}

      {questions.length > 0 && !submitted && (
        <button className="btn-primary" onClick={() => setSubmitted(true)}
                disabled={Object.keys(answers).length < questions.length}>
          Submit answers
        </button>
      )}
      {submitted && <h3>Score: {score} / {questions.length}</h3>}
    </>
  );
};

export default Quiz;
