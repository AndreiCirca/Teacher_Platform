import { useState, useEffect, useRef } from 'react';
import './Register.css'; // Importăm fișierul CSS creat anterior

const Register = () => {
    const [formData, setFormData] = useState({
        firstName: '',
        lastName: '',
        email: '',
        password: '',
        confirmPassword: '',
        schoolId: null
    });

    const [showPassword, setShowPassword] = useState(false);
    const [passwordStrength, setPasswordStrength] = useState(0);
    const [isLoading, setIsLoading] = useState(false);

    const [schoolSearch, setSchoolSearch] = useState('');
    const [isDropdownOpen, setIsDropdownOpen] = useState(false);
    const dropdownRef = useRef(null);

    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');

    const mockSchools = [
        { id: 1, name: 'Colegiul Național „I.L. Caragiale”', county: 'București' },
        { id: 2, name: 'Liceul Teoretic „Avram Iancu”', county: 'Cluj' },
        { id: 3, name: 'Școala Gimnazială Nr. 1', county: 'Iași' }
    ];

    useEffect(() => {
        const handleClickOutside = (event) => {
            if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
                setIsDropdownOpen(false);
            }
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData({ ...formData, [name]: value });

        if (name === 'password') {
            checkStrength(value);
        }
    };

    const checkStrength = (p) => {
        let strength = 0;
        if (p.length > 5) strength++;
        if (p.length > 8) strength++;
        if (/[A-Z]/.test(p)) strength++;
        if (/[0-9]/.test(p) || /[^A-Za-z0-9]/.test(p)) strength++;
        setPasswordStrength(strength);
    };

    const getStrengthClass = (index) => {
        if (index >= passwordStrength) return 'strength-0';
        return `strength-${passwordStrength}`;
    };

    const getStrengthText = () => {
        if (passwordStrength === 0) return { text: 'Introdu o parolă sigură.', class: 'text-muted' };
        const labels = ['Foarte slabă', 'Slabă', 'Medie', 'Sigură'];
        return {
            text: labels[passwordStrength - 1],
            class: passwordStrength > 2 ? 'text-strong' : 'text-strong'
        };
    };

    const handleSchoolSelect = (school) => {
        setSchoolSearch(`${school.name} (${school.county})`);
        setFormData({ ...formData, schoolId: school.id });
        setIsDropdownOpen(false);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setSuccess('');

        if (formData.password !== formData.confirmPassword) {
            setError('Parolele nu coincid!');
            return;
        }

        setIsLoading(true);

        const payload = {
            firstName: formData.firstName,
            lastName: formData.lastName,
            email: formData.email,
            password: formData.password,
            role: "ADMIN",
            schoolId: formData.schoolId,
            phoneNumber: ""
        };

        try {
            const response = await fetch('http://localhost:8080/api/auth/register', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(payload)
            });

            const data = await response.json();

            if (response.ok) {
                setSuccess('Cont creat cu succes! Verifică e-mail-ul pentru confirmare.');
            } else {
                setError(data.message || 'A apărut o eroare la înregistrare.');
            }
        } catch (err) {
            setError('Eroare de conexiune la server. Te rugăm să încerci mai târziu.');
        } finally {
            setIsLoading(false);
        }
    };

    const strengthLabel = getStrengthText();

    return (
        <div className="page-wrapper">
            <div className="bg-blob-1"></div>
            <div className="bg-blob-2"></div>

            <main className="register-card">
                <div className="card-header">
                    <div className="logo-container">
                        <span className="material-symbols-outlined">school</span>
                        <h1 className="logo-title">FormaProf</h1>
                    </div>
                    <h2 className="card-subtitle">Creează-ți contul</h2>
                    <div className="info-box">
                        <p>
                            <span className="material-symbols-outlined" style={{ fontSize: '18px' }}>info</span>
                            Doar pentru profesori. Formatorii sunt adăugați de administrator.
                        </p>
                    </div>
                </div>

                <form className="register-form" onSubmit={handleSubmit}>
                    {error && (
                        <div className="alert alert-error">
                            <span className="material-symbols-outlined" style={{ fontSize: '18px' }}>error</span>
                            {error}
                        </div>
                    )}

                    {success && (
                        <div className="alert alert-success">
                            <span className="material-symbols-outlined" style={{ fontSize: '18px' }}>check_circle</span>
                            {success}
                        </div>
                    )}

                    <div className="form-grid">
                        <div className="form-group">
                            <label className="form-label" htmlFor="firstName">Prenume</label>
                            <input className="form-input" id="firstName" name="firstName" placeholder="Ex: Ion" required type="text"
                                   value={formData.firstName} onChange={handleChange} />
                        </div>
                        <div className="form-group">
                            <label className="form-label" htmlFor="lastName">Nume</label>
                            <input className="form-input" id="lastName" name="lastName" placeholder="Ex: Popescu" required type="text"
                                   value={formData.lastName} onChange={handleChange} />
                        </div>
                    </div>

                    <div className="form-group">
                        <label className="form-label" htmlFor="email">Email Instituțional / Personal</label>
                        <div className="input-wrapper">
                            <span className="material-symbols-outlined input-icon">mail</span>
                            <input className="form-input input-with-icon" id="email" name="email" placeholder="nume@scoala.ro" required type="email"
                                   value={formData.email} onChange={handleChange} />
                        </div>
                    </div>

                    <div className="form-group" ref={dropdownRef}>
                        <label className="form-label" htmlFor="schoolSearch">Unitatea de Învățământ</label>
                        <div className="input-wrapper">
                            <span className="material-symbols-outlined input-icon">search</span>
                            <input className="form-input input-with-icon" id="schoolSearch" placeholder="Caută școala sau liceul..." type="text"
                                   value={schoolSearch}
                                   onChange={(e) => setSchoolSearch(e.target.value)}
                                   onFocus={() => setIsDropdownOpen(true)} />
                        </div>

                        {isDropdownOpen && (
                            <div className="dropdown-menu">
                                {mockSchools.map((school) => (
                                    <div key={school.id} className="dropdown-item" onClick={() => handleSchoolSelect(school)}>
                                        <p className="dropdown-item-title">{school.name}</p>
                                        <p className="dropdown-item-subtitle">{school.county}</p>
                                    </div>
                                ))}
                            </div>
                        )}
                    </div>

                    <div className="form-group">
                        <label className="form-label" htmlFor="password">Parolă</label>
                        <div className="input-wrapper">
                            <span className="material-symbols-outlined input-icon">lock</span>
                            <input className="form-input input-with-icon input-with-action" id="password" name="password" required type={showPassword ? "text" : "password"}
                                   value={formData.password} onChange={handleChange} />
                            <button className="action-icon" type="button" onClick={() => setShowPassword(!showPassword)}>
                                <span className="material-symbols-outlined">
                                    {showPassword ? 'visibility_off' : 'visibility'}
                                </span>
                            </button>
                        </div>
                        <div className="strength-container">
                            {[0, 1, 2, 3].map(index => (
                                <div key={index} className={`strength-bar ${getStrengthClass(index)}`}></div>
                            ))}
                        </div>
                        <p className={`strength-text ${strengthLabel.class}`}>
                            {strengthLabel.text}
                        </p>
                    </div>

                    <div className="form-group">
                        <label className="form-label" htmlFor="confirmPassword">Confirmă Parola</label>
                        <div className="input-wrapper">
                            <span className="material-symbols-outlined input-icon">lock_reset</span>
                            <input className="form-input input-with-icon" id="confirmPassword" name="confirmPassword" required type="password"
                                   value={formData.confirmPassword} onChange={handleChange} />
                        </div>
                    </div>

                    <div className="checkbox-group">
                        <input className="checkbox-input" id="terms" name="terms" required type="checkbox" />
                        <label className="checkbox-label" htmlFor="terms">
                            Sunt de acord cu <a className="link" href="#terms">Termenii și Condițiile</a> și <a className="link" href="#privacy">Politica de Confidențialitate</a>.
                        </label>
                    </div>

                    <button className="btn-submit" type="submit" disabled={isLoading}>
                        {isLoading ? (
                            <>
                                <span className="material-symbols-outlined spin-icon">progress_activity</span>
                                Se procesează...
                            </>
                        ) : (
                            <>
                                Creează cont
                                <span className="material-symbols-outlined" style={{ fontSize: '18px' }}>arrow_forward</span>
                            </>
                        )}
                    </button>

                    <p className="footer-text">
                        Ai deja un cont? <a className="link" href="/login">Autentifică-te</a>
                    </p>
                </form>
            </main>
        </div>
    );
};

export default Register;