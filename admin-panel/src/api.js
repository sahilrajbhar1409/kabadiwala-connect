const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:5000/api';

class ApiError extends Error {
  constructor(message, status) {
    super(message);
    this.status = status;
  }
}

async function request(endpoint, options = {}) {
  const token = sessionStorage.getItem('admin_token');
  const headers = {
    'Content-Type': 'application/json',
    ...options.headers,
  };

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const response = await fetch(`${BASE_URL}${endpoint}`, {
    ...options,
    headers,
  });

  const json = await response.json().catch(() => ({}));

  if (!response.ok || json.success === false) {
    throw new ApiError(json.message || `API Error: ${response.status}`, response.status);
  }

  return json;
}

export const api = {
  login: (identifier, password) =>
    request('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ identifier, password })
    }),

  getAnalytics: (endpoint, queryObj = {}) => {
    const params = new URLSearchParams();
    if (queryObj.from) params.append('from', queryObj.from);
    if (queryObj.to) params.append('to', queryObj.to);
    if (queryObj.includeDemo !== undefined) params.append('includeDemo', String(queryObj.includeDemo));

    const qs = params.toString();
    return request(`/admin/analytics${endpoint}${qs ? '?' + qs : ''}`);
  }
};
