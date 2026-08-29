import { useCallback, useEffect, useState } from 'react';
import { useAuth0 } from '@auth0/auth0-react';

import { createPost, getMyProfile, getStream } from './api';
import { validateDraft } from './lib/post.js';
import Composer from './components/Composer.jsx';
import PostList from './components/PostList.jsx';
import SessionCard from './components/SessionCard.jsx';

const logoutReturnTo = import.meta.env.VITE_AUTH0_LOGOUT_RETURN_TO || window.location.origin;
const audience = import.meta.env.VITE_AUTH0_AUDIENCE;

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
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState('');

  const [profile, setProfile] = useState(null);

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

  // El perfil viene de una ruta protegida por el scope read:profile. Si el token
  // no lo trae, se deja en null en vez de romper la vista.
  const loadProfile = useCallback(async () => {
    if (!isAuthenticated) {
      setProfile(null);
      return;
    }
    try {
      const token = await getAccessTokenSilently({
        authorizationParams: { audience, scope: 'read:profile' }
      });
      setProfile(await getMyProfile(token));
    } catch {
      setProfile(null);
    }
  }, [getAccessTokenSilently, isAuthenticated]);

  useEffect(() => {
    loadStream();
  }, [loadStream]);

  useEffect(() => {
    loadProfile();
  }, [loadProfile]);

  async function handleCreatePost(event) {
    event.preventDefault();

    const { valid, error } = validateDraft(draft);
    if (!valid) {
      setSubmitError(error);
      return;
    }

    setSubmitting(true);
    setSubmitError('');
    try {
      const token = await getAccessTokenSilently({
        authorizationParams: { audience, scope: 'write:posts' }
      });
      await createPost(draft.trim(), token);
      setDraft('');
      await loadStream();
    } catch (error) {
      setSubmitError(error.message || 'No fue posible publicar el mensaje.');
    } finally {
      setSubmitting(false);
    }
  }

  if (isLoading) {
    return (
      <div className="status-view" role="status" aria-live="polite">
        Cargando autenticación…
      </div>
    );
  }

  return (
    <div className="page-shell">
      <div className="ambient-shape shape-a" aria-hidden="true" />
      <div className="ambient-shape shape-b" aria-hidden="true" />

      <main className="app-grid">
        <section className="panel panel-brand" aria-labelledby="brand-title">
          <p className="eyebrow">Stream público · Auth0 · AWS</p>
          <h1 id="brand-title">PulseFeed</h1>
          <p className="lead">
            Stream público global con backend protegido por Auth0. Las publicaciones son de
            máximo 140 caracteres y crear una exige el scope <code>write:posts</code>.
          </p>

          <div className="auth-actions">
            {!isAuthenticated ? (
              <button className="btn btn-primary" onClick={() => loginWithRedirect()}>
                Iniciar sesión
              </button>
            ) : (
              <button
                className="btn btn-secondary"
                onClick={() => logout({ logoutParams: { returnTo: logoutReturnTo } })}
              >
                Cerrar sesión
              </button>
            )}
          </div>

          <SessionCard isAuthenticated={isAuthenticated} user={user} profile={profile} />
        </section>

        <section className="panel panel-feed" aria-labelledby="feed-title">
          <header className="feed-header">
            <h2 id="feed-title">Stream global</h2>
            <button className="btn btn-ghost" onClick={loadStream} disabled={streamLoading}>
              {streamLoading ? 'Cargando…' : 'Recargar'}
            </button>
          </header>

          {isAuthenticated ? (
            <Composer
              draft={draft}
              onDraftChange={setDraft}
              onSubmit={handleCreatePost}
              submitting={submitting}
              error={submitError}
            />
          ) : (
            <p className="notice-text">Inicia sesión para publicar.</p>
          )}

          <PostList posts={posts} loading={streamLoading} error={streamError} />
        </section>
      </main>
    </div>
  );
}
