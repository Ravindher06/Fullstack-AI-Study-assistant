# Smart Study Assistant

Full-stack RAG study assistant: Spring Boot 3 + Spring AI (Ollama) backend, React + Vite frontend.

## Prerequisites
- JDK 17+ and Maven 3.9+
- Node.js 18+
- [Ollama](https://ollama.com) installed and running

## Run
1. Models (the backend can also auto-pull them on first start):
   ```
   ollama pull llama3.2
   ollama pull nomic-embed-text
   ```
2. Backend (port 8080): `./run-backend.sh` (Windows: `run-backend.bat`) or `mvn spring-boot:run`
3. Frontend (port 5173): `./run-frontend.sh` (Windows: `run-frontend.bat`) or `cd frontend && npm install && npm run dev`
4. Open http://localhost:5173

Uploaded material is stored in `./data/` (vector store + document list) and survives restarts.

## API
| Method | Path | Purpose |
|---|---|---|
| POST | /api/v1/study/upload | multipart `file` (.txt/.md/.pdf) |
| POST | /api/v1/study/ask | `{question}` |
| POST | /api/v1/study/generate-quiz | `{targetTopic, numberOfQuestions}` |
| GET | /api/v1/study/documents | list |
| DELETE | /api/v1/study/documents/{id} | delete doc + its vectors |

## Notes
- Config lives in `src/main/resources/application.properties` (models, data dir, port).
- To add the Maven wrapper: `mvn wrapper:wrapper`.
