import React, { useEffect } from 'react';
import { BrowserRouter } from 'react-router-dom';
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import 'bootstrap/dist/css/bootstrap.min.css';
import '@fortawesome/fontawesome-free/css/all.min.css';
import './styles/main.scss';
import AppRoutes from './routes';
import { setupTokenRefresh } from './utils/tokenUtils';

const App = () => {
    useEffect(() => {
        // Setup token refresh on app load if user is logged in
        const token = localStorage.getItem('token');
        if (token) {
            setupTokenRefresh(token);
        }
    }, []);

    return (
        <>
            <BrowserRouter future={{ v7_startTransition: true, v7_relativeSplatPath: true }}>
                <AppRoutes />
            </BrowserRouter>
            <ToastContainer
                position="top-right"
                autoClose={5000}
                hideProgressBar={false}
                newestOnTop
                closeOnClick
                rtl={false}
                pauseOnFocusLoss
                draggable
                pauseOnHover
            />
        </>
    );
};

export default App;
