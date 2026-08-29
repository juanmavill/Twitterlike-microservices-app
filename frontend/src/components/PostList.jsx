import { formatDate } from '../lib/post.js';

/**
 * Stream público. Cubre los cuatro estados posibles en lugar de asumir que
 * siempre hay datos: cargando, error, vacío y con contenido.
 */
export default function PostList({ posts, loading, error }) {
  if (loading) {
    return (
      <div className="status-view" role="status" aria-live="polite">
        Cargando stream…
      </div>
    );
  }

  if (error) {
    return (
      <p className="error-text" role="alert">
        {error}
      </p>
    );
  }

  if (posts.length === 0) {
    return <p className="notice-text">Todavía no hay publicaciones. Sé el primero.</p>;
  }

  return (
    <ul className="post-list">
      {posts.map((post) => (
        <li className="post-item" key={post.id}>
          <div className="post-meta">
            <strong>{post.authorName}</strong>
            <time dateTime={post.createdAt}>{formatDate(post.createdAt)}</time>
          </div>
          <p>{post.content}</p>
        </li>
      ))}
    </ul>
  );
}
