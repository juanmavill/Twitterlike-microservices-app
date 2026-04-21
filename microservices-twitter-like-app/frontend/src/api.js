const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080').replace(/\/+$/, '');

async function request(path, options = {}) {
  const response = await fetch(`${API_BASE_URL}${path}`, options);

  if (!response.ok) {
    let message = `Request failed with status ${response.status}`;
    const rawBody = await response.text();
    try {
      const errorBody = rawBody ? JSON.parse(rawBody) : null;
      if (errorBody?.error) {
        message = errorBody.error;
      }
    } catch {
      const authHeader = response.headers.get('www-authenticate');
      const authDescription = authHeader?.match(/error_description=\"([^\"]+)\"/)?.[1];
      if (authDescription) {
        message = authDescription;
      } else if (rawBody?.trim()) {
        message = rawBody.trim();
      }
    }
    throw new Error(message);
  }

  if (response.status === 204) {
    return null;
  }

  return response.json();
}

export function getStream() {
  return request('/api/stream');
}

export function createPost(content, accessToken) {
  return request('/api/posts', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${accessToken}`
    },
    body: JSON.stringify({ content })
  });
}

export function getMyProfile(accessToken) {
  return request('/api/me', {
    method: 'GET',
    headers: {
      Authorization: `Bearer ${accessToken}`
    }
  });
}
