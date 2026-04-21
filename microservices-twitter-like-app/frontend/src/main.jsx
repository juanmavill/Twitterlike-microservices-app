import React from 'react';
import ReactDOM from 'react-dom/client';
import { Auth0Provider } from '@auth0/auth0-react';
import App from './App';
import './App.css';

const domain = import.meta.env.VITE_AUTH0_DOMAIN;
const clientId = import.meta.env.VITE_AUTH0_CLIENT_ID;
const audience = import.meta.env.VITE_AUTH0_AUDIENCE;
const redirectUri = import.meta.env.VITE_AUTH0_REDIRECT_URI || window.location.origin;

function MissingConfiguration() {
  return (
    <div className="missing-config">
      <h1>Missing Auth0 configuration</h1>
      <p>Set VITE_AUTH0_DOMAIN, VITE_AUTH0_CLIENT_ID and VITE_AUTH0_AUDIENCE in your .env file.</p>
      <p>Then restart the development server.</p>
    </div>
  );
}

const root = ReactDOM.createRoot(document.getElementById('root'));

if (!domain || !clientId || !audience) {
  root.render(
    <React.StrictMode>
      <MissingConfiguration />
    </React.StrictMode>
  );
} else {
  root.render(
    <React.StrictMode>
        <Auth0Provider
          domain={domain}
          clientId={clientId}
          authorizationParams={{
          redirect_uri: redirectUri,
          audience,
          scope: 'openid profile email read:posts write:posts read:profile'
          }}
        useRefreshTokens
        cacheLocation="memory"
      >
        <App />
      </Auth0Provider>
    </React.StrictMode>
  );
}
