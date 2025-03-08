import React from 'react';
import { Provider } from 'react-redux';
import { BrowserRouter } from 'react-router-dom';
import { AuthProvider } from './contexts/auth.context';
import { store } from './store';
import AppRoutes from './routes';
import 'antd/dist/reset.css';
import './styles/auth.css';
import { StompProvider } from './contexts/stomp.context';

const App: React.FC = () => {
  return (
    <BrowserRouter>
      <Provider store={store}>
        <AuthProvider>
          <StompProvider>
            <AppRoutes />
          </StompProvider>
        </AuthProvider>
      </Provider>
    </BrowserRouter>
  );
};

export default App;
