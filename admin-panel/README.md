# Kabadiwala Connect - Admin Panel (Phase 2 Frontend)

This is a Vite + React prototype dashboard for the Admin Analytics Panel, built specifically for the hackathon demo. It connects directly to the Phase 1 Backend Analytics APIs.

## Requirements
- Node.js >= 18
- Backend server running (default `http://localhost:5000`)

## Setup & Installation
1. Move to this directory: `cd admin-panel`
2. DO NOT run `npm install` without explicit permissions. If authorized, install dependencies: `npm install`. Note: this requires Vite and React dependencies.
3. Configure the `.env` file for the API endpoint (defaults to `http://localhost:5000/api`).

## Running the Application
Run the frontend:
```bash
npm run dev
```
Open `http://localhost:3000` in your browser.

## Demo Credentials
The backend provides seeded mock data for the demo.
- **Email:** `admin@kabadiwala.demo`
- **Password:** `Demo@12345`

The dashboard automatically toggles **"Include Demo Data"** to ON by default to display the seeded transactions and data from the local mock db.
