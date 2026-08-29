// Reglas del borrador de publicación, sin React ni DOM.
// El backend rechaza con 400 lo que no cumpla esto; validar aquí evita un viaje
// al servidor y permite dar el mensaje en el idioma de la interfaz.

export const MAX_CHARS = 140;

/** Caracteres que quedan disponibles. Puede ser negativo si se pega texto largo. */
export function charsLeft(draft) {
  return MAX_CHARS - (draft ?? '').length;
}

/** Umbral a partir del cual el contador se resalta para avisar al usuario. */
export function isNearLimit(draft) {
  return charsLeft(draft) < 15;
}

/**
 * @returns {{valid: boolean, error?: string}} motivo del rechazo, o válido.
 */
export function validateDraft(draft) {
  const trimmed = (draft ?? '').trim();

  if (!trimmed) {
    return { valid: false, error: 'El mensaje no puede estar vacío.' };
  }
  // Se mide el borrador completo, no el recortado: el usuario ve ese conteo.
  if ((draft ?? '').length > MAX_CHARS) {
    return { valid: false, error: `El mensaje supera ${MAX_CHARS} caracteres.` };
  }
  return { valid: true };
}

export function formatDate(isoDate) {
  const date = new Date(isoDate);
  if (Number.isNaN(date.getTime())) {
    return '';
  }
  return new Intl.DateTimeFormat('es-CO', {
    dateStyle: 'medium',
    timeStyle: 'short'
  }).format(date);
}
