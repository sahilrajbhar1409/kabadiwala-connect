import React, { useState } from 'react';
import { api } from '../api';

export default function Login({ onLoginComplete }) {
  const [identifier, setIdentifier] = useState('admin@kabadiwala.demo');
  const [password, setPassword] = useState('Demo@12345');
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setLoading(true);

    try {
      const response = await api.login(identifier, password);
      // Validate role
      if (response.data.user.role !== 'admin') {
        throw new Error('Access denied. Admin role required.');
      }
      
      sessionStorage.setItem('admin_token', response.data.token);
      onLoginComplete();
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-box">
        <h2>Admin Authentication</h2>
        {error && <div className="alert-error">{error}</div>}
        <form className="auth-form" onSubmit={handleSubmit}>
          <div>
            <label style={{display: 'block', marginBottom: '0.5rem', color: 'var(--text-muted)'}}>Email</label>
            <input 
              style={{width: '100%'}}
              type="text" 
              value={identifier} 
              onChange={(e) => setIdentifier(e.target.value)}
              placeholder="Admin Email"
              required 
            />
          </div>
          <div>
            <label style={{display: 'block', marginBottom: '0.5rem', color: 'var(--text-muted)'}}>Password</label>
            <input 
              style={{width: '100%'}}
              type="password" 
              value={password} 
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Password"
              required 
            />
          </div>
          <button type="submit" className="btn-primary" disabled={loading}>
            {loading ? 'Authenticating...' : 'Sign In'}
          </button>
        </form>
      </div>
    </div>
  );
}
