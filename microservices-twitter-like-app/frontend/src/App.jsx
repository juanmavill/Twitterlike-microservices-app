import { useCallback, useEffect, useMemo, useState } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import { createPost, getMyProfile, getStream } from './api';

const MAX_CHARS = 140;
const logoutReturnTo = import.meta.env.VITE_AUTH0_LOGOUT_RETURN_TO || window.location.origin;

function formatDate(isoDate) {
  return new Intl.DateTimeFormat('es-CO', {
    dateStyle: 'medium',
    timeStyle: 'short'
  }).format(new Date(isoDate));
}

export default function App() {
  const {
    isAuthenticated,
    isLoading,
    user,
    loginWithRedirect,
    logout,
    getAccessTokenSilently
  } = useAuth0();

  const [streamLoading, setStreamLoading] = useState(true);
  const [streamError, setStreamError] = useState('');
  const [posts, setPosts] = useState([]);

  const [draft, setDraft] = useState('');
  const [submitLoading, setSubmitLoading] = useState(false);
  const [submitError, setSubmitError] = useState('');

  const [me, setMe] = useState(null);

  const charsLeft = useMemo(() => MAX_CHARS - draft.length, [draft]);

  const loadStream = useCallback(async () => {
    setStreamLoading(true);
    setStreamError('');
    try {
      const data = await getStream();
      setPosts(data.posts || []);
    } catch (error) {
      setStreamError(error.message || 'No se pudo cargar el stream.');
    } finally {
      setStreamLoading(false);
    }
  }, []);

  const loadMe = useCallback(async () => {
    if (!isAuthenticated) {
      setMe(null);
      return;
    }

    try {
      const token = await getAccessTokenSilently({
        authorizationParams: {
          audience: import.meta.env.VITE_AUTH0_AUDIENCE,
          scope: 'read:profile'
        }
      });

      const profile = await getMyProfile(token);
      setMe(profile);
    } catch {
      setMe(null);
    }
  }, [getAccessTokenSilently, isAuthenticated]);

  useEffect(() => {
    loadStream();
  }, [loadStream]);

  useEffect(() => {
    loadMe();
  }, [loadMe]);

  async function handleCreatePost(event) {
    event.preventDefault();

    if (!draft.trim()) {
      setSubmitError('El mensaje no puede estar vacio.');
      return;
    }

    if (draft.length > MAX_CHARS) {
      setSubmitError('El mensaje supera 140 caracteres.');
      return;
    }

    setSubmitLoading(true);
    setSubmitError('');

    try {
      const token = await getAccessTokenSilently({
        authorizationParams: {
          audience: import.meta.env.VITE_AUTH0_AUDIENCE,
          scope: 'write:posts'
        }
      });

      await createPost(draft.trim(), token);
      setDraft('');
      await loadStream();
    } catch (error) {
      setSubmitError(error.message || 'No fue posible publicar el mensaje.');
    } finally {
      setSubmitLoading(false);
    }
  }

  if (isLoading) {
    return <div className="status-view">Cargando autenticacion...</div>;
  }

  return (
    <div className="page-shell">
      <div className="ambient-shape shape-a" />
      <div className="ambient-shape shape-b" />

      <main className="app-grid">
        <section className="panel panel-brand">
          <p className="eyebrow">Experimental Assignment</p>
          <h1>PulseFeed</h1>
          <p className="lead">
            Stream publico global con backend seguro por Auth0. Publica mensajes de maximo 140 caracteres.
          </p>

          <div className="auth-actions">
            {!isAuthenticated ? (
              <button className="btn btn-primary" onClick={() => loginWithRedirect()}>
                Iniciar sesion
              </button>
            ) : (
              <button
                className="btn btn-secondary"
                onClick={() =>
                    logout({
                    logoutParams: { returnTo: logoutReturnTo }
                  })
                }
              >
                Cerrar sesion
              </button>
            )}
          </div>

          <div className="identity-card">
            <h2>Sesion</h2>
            {!isAuthenticated ? (
              <p>No autenticado</p>
            ) : (
              <>
                <p>
                  <strong>Usuario:</strong> {user?.name || user?.email}
                </p>
                <p>
                  <strong>ID Auth0:</strong> {me?.auth0UserId || 'cargando...'}
                </p>
                <p>
                  <strong>Correo:</strong> {me?.email || user?.email || 'sin dato'}
                </p>
              </>
            )}
          </div>
        </section>

        <section className="panel panel-feed">
          <header className="feed-header">
            <h2>Global Stream</h2>
            <button className="btn btn-ghost" onClick={loadStream}>
              Recargar
            </button>
          </header>

          {isAuthenticated ? (
            <form className="composer" onSubmit={handleCreatePost}>
              <label htmlFor="post-content">Nuevo post</label>
              <textarea
                id="post-content"
                value={draft}
                maxLength={MAX_CHARS}
                onChange={(event) => setDraft(event.target.value)}
                placeholder="Escribe algo corto, claro y valioso..."
              />

              <div className="composer-footer">
                <span className={charsLeft < 15 ? 'counter counter-warning' : 'counter'}>
                  {charsLeft} caracteres restantes
                </span>
                <button className="btn btn-primary" disabled={submitLoading} type="submit">
                  {submitLoading ? 'Publicando...' : 'Publicar'}
                </button>
              </div>
              {submitError && <p className="error-text">{submitError}</p>}
            </form>
          ) : (
            <p className="notice-text">Inicia sesion para crear posts.</p>
          )}

          {streamLoading ? (
            <div className="status-view">Cargando stream...</div>
          ) : streamError ? (
            <p className="error-text">{streamError}</p>
          ) : posts.length === 0 ? (
            <p className="notice-text">No hay posts aun. Se el primero en publicar.</p>
          ) : (
            <ul className="post-list">
              {posts.map((post) => (
                <li className="post-item" key={post.id}>
                  <div className="post-meta">
                    <strong>{post.authorName}</strong>
                    <span>{formatDate(post.createdAt)}</span>
                  </div>
                  <p>{post.content}</p>
                </li>
              ))}
            </ul>
          )}
        </section>
      </main>
    </div>
  );
}
