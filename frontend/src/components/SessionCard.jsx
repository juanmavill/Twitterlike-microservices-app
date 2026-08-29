/**
 * Estado de la sesión. Muestra el identificador de Auth0 y el correo que
 * devuelve el backend en /api/me, no los que trae el token del cliente: sirve
 * para comprobar que la ruta protegida por scope responde de verdad.
 */
export default function SessionCard({ isAuthenticated, user, profile }) {
  return (
    <div className="identity-card">
      <h2>Sesión</h2>
      {!isAuthenticated ? (
        <p>No autenticado</p>
      ) : (
        <dl className="identity-list">
          <dt>Usuario</dt>
          <dd>{user?.name || user?.email}</dd>
          <dt>ID de Auth0</dt>
          <dd>{profile?.auth0UserId || 'cargando…'}</dd>
          <dt>Correo</dt>
          <dd>{profile?.email || user?.email || 'sin dato'}</dd>
        </dl>
      )}
    </div>
  );
}
