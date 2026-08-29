import { charsLeft, isNearLimit, MAX_CHARS } from '../lib/post.js';

/**
 * Formulario de publicación. El contador vive fuera del textarea para que un
 * lector de pantalla lo anuncie sin interrumpir la escritura.
 */
export default function Composer({ draft, onDraftChange, onSubmit, submitting, error }) {
  const restantes = charsLeft(draft);

  return (
    <form className="composer" onSubmit={onSubmit}>
      <label htmlFor="post-content">Nuevo post</label>
      <textarea
        id="post-content"
        value={draft}
        maxLength={MAX_CHARS}
        onChange={(event) => onDraftChange(event.target.value)}
        placeholder="Escribe algo corto, claro y valioso…"
        aria-describedby="post-counter"
      />

      <div className="composer-footer">
        <span
          id="post-counter"
          className={isNearLimit(draft) ? 'counter counter-warning' : 'counter'}
          // Solo se anuncia cuando queda poco margen: anunciar cada tecla seria ruido.
          aria-live={isNearLimit(draft) ? 'polite' : 'off'}
        >
          {restantes} caracteres restantes
        </span>
        <button className="btn btn-primary" disabled={submitting} type="submit">
          {submitting ? 'Publicando…' : 'Publicar'}
        </button>
      </div>

      {error && (
        <p className="error-text" role="alert">
          {error}
        </p>
      )}
    </form>
  );
}
