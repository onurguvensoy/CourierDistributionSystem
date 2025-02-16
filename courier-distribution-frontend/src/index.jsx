import React from 'react';
import ReactDOM from 'react-dom/client';

// Import jQuery and Bootstrap
import 'jquery';
import '@popperjs/core';
import 'bootstrap';

// Import styles
import './styles/main.scss';
import 'react-toastify/dist/ReactToastify.css';
import '@fortawesome/fontawesome-free/css/all.css';

import App from './App';

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);

