import { FC, ReactNode } from 'react';
import '../../styles/auth.css';

interface AuthLayoutProps {
  children: ReactNode;
  title: string;
  subtitle: string;
  type?: 'login' | 'register';
}

const AuthLayout: FC<AuthLayoutProps> = ({ children, title, subtitle, type = 'login' }) => {
  return (
    <div className="bg-gradient-primary auth-wrapper">
      <div className="container">
        <div className="row justify-content-center">
          <div className="col-xl-10 col-lg-12 col-md-9">
            <div className="auth-card o-hidden border-0 shadow-lg my-5">
              <div className="card-body p-0">
                <div className="row">
                  <div className="col-lg-6 d-none d-lg-block bg-login-image"></div>
                  <div className="col-lg-6">
                    <div className="p-5">
                      <div className="text-center">
                        <h1 className="h4 text-gray-900 mb-4">{title}</h1>
                        <p className="mb-4 text-gray-600">{subtitle}</p>
                      </div>
                      {children}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AuthLayout; 