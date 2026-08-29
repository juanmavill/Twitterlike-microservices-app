# Auth0 Setup Guide

## 1. Create API in Auth0

1. Go to Auth0 Dashboard > Applications > APIs > Create API.
2. Name: Twitterlike API.
3. Identifier (Audience): `https://twitterlike-api`.
4. Signing Algorithm: RS256.

Create scopes:
- `read:posts`
- `write:posts`
- `read:profile`

## 2. Create SPA application

1. Go to Applications > Applications > Create Application.
2. Type: Single Page Application.
3. Framework: React.

Configure SPA app URLs:
- Allowed Callback URLs:
  - `http://localhost:5173`
  - `http://YOUR-S3-WEBSITE-ENDPOINT`
- Allowed Logout URLs:
  - `http://localhost:5173`
  - `http://YOUR-S3-WEBSITE-ENDPOINT`
- Allowed Web Origins:
  - `http://localhost:5173`
  - `http://YOUR-S3-WEBSITE-ENDPOINT`
- Allowed Origins (CORS):
  - `http://localhost:5173`
  - `http://YOUR-S3-WEBSITE-ENDPOINT`

## 3. Configure frontend

Create frontend `.env` from `.env.example` and set:

- `VITE_AUTH0_DOMAIN`
- `VITE_AUTH0_CLIENT_ID`
- `VITE_AUTH0_AUDIENCE`
- `VITE_API_BASE_URL`

## 4. Configure monolith

Set environment variables before running Spring Boot:

- `AUTH0_ISSUER_URI=https://YOUR-DOMAIN.auth0.com/`
- `AUTH0_AUDIENCE=https://twitterlike-api`
- `APP_CORS_ALLOWED_ORIGINS=http://localhost:5173`

Notes:
- The backend derives the JWKS endpoint from `AUTH0_ISSUER_URI`.
- If you paste the issuer without the final `/`, the application normalizes it automatically.

## 5. Configure microservices deployment

During SAM deploy, pass parameters:

- `Auth0Issuer`
- `Auth0Audience`
- `CorsAllowedOrigin`
