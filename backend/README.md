# Kabadiwala Connect

Smart India Hackathon platform that brings informal e-waste collectors (kabadiwalas) into the formal recycling chain by connecting them with authorized recyclers.

This repository contains:

- **Android app** (`app/`) — existing Jetpack Compose client (Sampah Jujur UI, adapted to this workflow)
- **Backend API** (`backend/`) — Node.js, Express, MongoDB, JWT

Demo recycler authorization numbers are **placeholders for hackathon demonstration**. They are not government-verified CPCB certificates.

## Architecture

```
Android (Compose / MVVM)
        |
        | HTTPS JSON + multipart
        v
Express API  (/api)
        |
        v
MongoDB (Mongoose)
        +
Local /uploads or Cloudinary for photos
```

Core flow:

Collector registers → creates an e-waste lot (photos, weight) → rule-based price estimate → ranked recycler matches → recycler offer → collector accepts → transaction → digital handover (both confirm) → payment → traceable timeline.

Price estimates and recycler ranking are **deterministic / rule-based**. They are structured so a future ML model can replace the service layer. This MVP does not claim a trained AI model.

## Tech stack

**Mobile:** Kotlin, Jetpack Compose, Hilt, Retrofit, Room, Coil, Cloudinary, OpenStreetMap  
**Backend:** Node.js, Express, MongoDB, Mongoose, JWT, bcryptjs, Multer, Cloudinary (optional), express-validator

## Prerequisites

- Node.js 18+
- MongoDB 6+ (local or Atlas)
- Android Studio Ladybug+ (to build the app)
- JDK 17+

## Backend installation

```bash
cd backend
copy .env.example .env
# edit .env: set MONGODB_URI and JWT_SECRET
npm install
```

## Frontend (Android) installation

Open the repository root in Android Studio and sync Gradle. Copy `local.properties.example` to `local.properties` and set:

```
sdk.dir=C:\\path\\to\\Android\\sdk
api.base.url=http://10.0.2.2:5000/api
cloudinary.cloud.name=...
```

`http://10.0.2.2:5000` is the Android emulator alias for `localhost:5000`. Use your LAN IP for a physical device.

## Environment variables (backend)

| Variable | Purpose |
| --- | --- |
| `PORT` | API port (default 5000) |
| `MONGODB_URI` | Mongo connection string |
| `JWT_SECRET` | Signing secret |
| `JWT_EXPIRES_IN` | Token lifetime |
| `CLIENT_ORIGIN` | CORS origins |
| `CLOUDINARY_*` | Optional image hosting; if empty, files are stored in `backend/uploads` |

Never commit `.env`.

## How to run MongoDB

Local:

```bash
mongod --dbpath <your-data-path>
```

Or MongoDB Atlas: put the connection string in `MONGODB_URI`.

## How to seed demo data

```bash
cd backend
npm run seed
```

## How to start backend

```bash
cd backend
npm run dev
```

Health check: `GET http://localhost:5000/api/health`

Trace UI: `http://localhost:5000/`

## How to start frontend

```bash
gradlew.bat installDebug
```

Or Run from Android Studio.

## API overview

- `POST /api/auth/register` `POST /api/auth/login` `GET /api/auth/me` `POST /api/auth/logout`
- `GET|POST|PATCH /api/materials`
- `GET /api/prices` `GET /api/prices/trends` `GET /api/prices/material/:id`
- `POST|GET|PATCH|DELETE /api/lots` `GET /api/lots/my-lots` `GET /api/lots/:id/matches`
- `GET /api/recyclers` `GET /api/recyclers/nearby` `GET /api/recyclers/:id`
- `POST|GET|PATCH /api/offers` `POST /api/offers/:id/accept` `POST /api/offers/:id/reject`
- `GET /api/transactions` `POST /api/transactions/:id/schedule`
- `POST /api/handovers` `GET /api/handovers/:id` `POST /api/handovers/:id/confirm`
- `POST|GET /api/payments`
- `GET /api/dashboard/collector|recycler|admin`
- `GET /api/notifications` `PATCH /api/notifications/:id/read`
- `GET /api/trace/:referenceId`
- `POST /api/sync`

## Demo credentials (after seed)

Password for all demo users: `Demo@12345`

| Role | Email | Phone |
| --- | --- | --- |
| Admin | admin@kabadiwala.demo | 9990000001 |
| Collector | ramesh.collector@kabadiwala.demo | 9876500001 |
| Collector | suresh.collector@kabadiwala.demo | 9876500002 |
| Recycler | greencycle@kabadiwala.demo | 9811100001 |
| Recycler | ecoboard@kabadiwala.demo | 9811100002 |
| Recycler | punehub@kabadiwala.demo | 9811100003 |

Run API flow tests (server must be up):

```bash
cd backend
npm run test:api
```
