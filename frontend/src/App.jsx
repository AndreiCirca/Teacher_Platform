// import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';

// Importă componenta pe care tocmai am creat-o
import Register from '../pages/Register.jsx'; // ajustează calea dacă e necesar
import Login from '../pages/Login.jsx'; // o vei adăuga ulterior

function App() {
    return (
        <BrowserRouter>
            <Routes>
                {/* Ruta pentru pagina de înregistrare */}
                <Route path="/register" element={<Register />} />
                <Route path="/login" element={<Login />} />

                {/* Redirecționare default către login */}
                <Route path="*" element={<Navigate to="/login" replace />} />
            </Routes>
        </BrowserRouter>
    );
}

export default App;