#!/usr/bin/env bash
# Installs deps and starts the Vite dev server on http://localhost:5173
cd "$(dirname "$0")/frontend"
npm install && npm run dev
