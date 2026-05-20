import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import './Login.css';

const Login = () => {
    const navigate = useNavigate();

    const [formData, setFormData] = useState({
        email: '',
        password: ''
    });

    const [showPassword, setShowPassword] = useState(false);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState('');

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData({ ...formData, [name]: value });
    };

    const togglePasswordVisibility = () => {
        setShowPassword(!showPassword);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setIsLoading(true);

        const payload = {
            email: formData.email,
            password: formData.password
        };

        try {
            const response = await fetch('http://localhost:8080/api/auth/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(payload)
            });

            const data = await response.json();

            if (response.ok) {
                // Aici în mod normal ai salva token-ul JWT (ex: în localStorage)
                // localStorage.setItem('token', data.token);

                // Redirect către register conform cerinței
                navigate('/register');
            } else {
                setError(data.message || 'Email sau parolă incorectă.');
            }
        } catch (err) {
            setError('Eroare de conexiune la server. Te rugăm să încerci mai târziu.');
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="page-wrapper">
            <main className="login-main">
                <div className="login-card">
                    {/* Header Section */}
                    <div className="login-header">
                        <div className="logo-group">
                            <span className="material-symbols-outlined logo-icon">school</span>
                            <h1 className="logo-text">FormaProf</h1>
                        </div>
                        <h2 className="welcome-title">Bun venit înapoi</h2>
                        <p className="welcome-subtitle">Continuă-ți parcursul de dezvoltare profesională</p>
                    </div>

                    {/* Login Form */}
                    <form className="login-form" onSubmit={handleSubmit}>

                        {error && (
                            <div className="alert-error">
                                <span className="material-symbols-outlined" style={{ fontSize: '18px' }}>error</span>
                                {error}
                            </div>
                        )}

                        {/* Email Input */}
                        <div className="form-group">
                            <label className="form-label label-row" htmlFor="email">Email</label>
                            <div className="input-wrapper">
                                <span className="material-symbols-outlined input-icon">mail</span>
                                <input
                                    className="form-input input-with-icon"
                                    id="email"
                                    name="email"
                                    placeholder="nume@exemplu.ro"
                                    required
                                    type="email"
                                    value={formData.email}
                                    onChange={handleChange}
                                />
                            </div>
                        </div>

                        {/* Password Input */}
                        <div className="form-group">
                            <div className="label-row">
                                <label className="form-label" htmlFor="password">Parolă</label>
                            </div>
                            <div className="input-wrapper">
                                <span className="material-symbols-outlined input-icon">lock</span>
                                <input
                                    className="form-input input-with-icon input-with-action"
                                    id="password"
                                    name="password"
                                    placeholder="••••••••"
                                    required
                                    type={showPassword ? "text" : "password"}
                                    value={formData.password}
                                    onChange={handleChange}
                                />
                                <button
                                    className="action-icon"
                                    type="button"
                                    onClick={togglePasswordVisibility}
                                >
                                    <span className="material-symbols-outlined">
                                        {showPassword ? 'visibility_off' : 'visibility'}
                                    </span>
                                </button>
                            </div>
                        </div>

                        {/* Submit Button */}
                        <button className="btn-submit" type="submit" disabled={isLoading}>
                            {isLoading ? (
                                <>
                                    <span className="material-symbols-outlined spin-icon">progress_activity</span>
                                    Se autentifică...
                                </>
                            ) : (
                                <>
                                    Autentifică-te
                                    <span className="material-symbols-outlined" style={{ fontSize: '18px' }}>arrow_forward</span>
                                </>
                            )}
                        </button>
                    </form>

                    {/* Footer Link */}
                    <div className="card-footer">
                        <p className="footer-text">
                            Nu ai un cont?{' '}
                            <Link className="register-link" to="/register">
                                Înregistrează-te acum
                            </Link>
                        </p>
                    </div>
                </div>
            </main>
        </div>
    );
};

export default Login;