import { describe, expect, it } from 'vitest';

import { MAX_CHARS, charsLeft, formatDate, isNearLimit, validateDraft } from './post.js';

describe('charsLeft', () => {
  it('descuenta los caracteres escritos del máximo', () => {
    expect(charsLeft('hola')).toBe(MAX_CHARS - 4);
  });

  it('trata un borrador vacío o nulo como cero caracteres', () => {
    expect(charsLeft('')).toBe(MAX_CHARS);
    expect(charsLeft(undefined)).toBe(MAX_CHARS);
  });

  /** Pegar texto largo debe poder mostrar el exceso, no quedarse en cero. */
  it('devuelve un valor negativo cuando se excede el límite', () => {
    expect(charsLeft('x'.repeat(MAX_CHARS + 5))).toBe(-5);
  });
});

describe('isNearLimit', () => {
  it('no avisa mientras queda margen', () => {
    expect(isNearLimit('x'.repeat(100))).toBe(false);
  });

  it('avisa cuando quedan menos de 15 caracteres', () => {
    expect(isNearLimit('x'.repeat(MAX_CHARS - 14))).toBe(true);
  });
});

describe('validateDraft', () => {
  it('acepta un mensaje normal', () => {
    expect(validateDraft('un mensaje')).toEqual({ valid: true });
  });

  /** Un borrador de puros espacios se ve lleno pero no aporta contenido. */
  it('rechaza un borrador que solo tiene espacios', () => {
    expect(validateDraft('   ').valid).toBe(false);
    expect(validateDraft('   ').error).toMatch(/vacío/);
  });

  it('acepta exactamente el límite', () => {
    expect(validateDraft('x'.repeat(MAX_CHARS))).toEqual({ valid: true });
  });

  it('rechaza un carácter por encima del límite', () => {
    const resultado = validateDraft('x'.repeat(MAX_CHARS + 1));
    expect(resultado.valid).toBe(false);
    expect(resultado.error).toContain(String(MAX_CHARS));
  });
});

describe('formatDate', () => {
  it('formatea una fecha ISO sin lanzar', () => {
    expect(formatDate('2026-08-19T15:30:00Z')).toBeTruthy();
  });

  /** El stream viene del backend; una fecha corrupta no debe romper el render. */
  it('devuelve cadena vacía ante una fecha inválida', () => {
    expect(formatDate('no-es-fecha')).toBe('');
  });
});
