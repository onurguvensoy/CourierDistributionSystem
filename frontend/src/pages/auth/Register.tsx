import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import axios from 'axios';

export default function Register() {
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    password: '',
    repeatPassword: ''
  });
  const [isLoading, setIsLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (formData.password !== formData.repeatPassword) {
      toast.error('Passwords do not match');
      return;
    }

    setIsLoading(true);
    try {
      await axios.post(`${import.meta.env.VITE_API_URL}/auth/register`, {
        username: formData.username,
        email: formData.email,
        password: formData.password
      });
      toast.success('Registration successful!');
      navigate('/login');
    } catch (err) {
      toast.error('Registration failed. Please try again.');
      console.error('Registration error:', err);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="bg-gradient-primary min-vh-100">
      <div className="container">
        <div className="card o-hidden border-0 shadow-lg my-5">
          <div className="card-body p-0">
            <div className="row">
              <div className="col-lg-5 d-none d-lg-block bg-register-image"></div>
              <div className="col-lg-7">
                <div className="p-5">
                  <div className="text-center">
                    <h1 className="h4 text-gray-900 mb-4">Create an Account!</h1>
                  </div>
                  <form className="user" onSubmit={handleSubmit}>
                    <div className="form-group">
                      <input
                        type="text"
                        className="form-control form-control-user"
                        placeholder="Username"
                        value={formData.username}
                        onChange={(e) => setFormData({...formData, username: e.target.value})}
                        required
                      />
                    </div>
                    <div className="form-group">
                      <input
                        type="email"
                        className="form-control form-control-user"
                        placeholder="Email Address"
                        value={formData.email}
                        onChange={(e) => setFormData({...formData, email: e.target.value})}
                        required
                      />
                    </div>
                    <div className="form-group row">
                      <div className="col-sm-6 mb-3 mb-sm-0">
                        <input
                          type="password"
                          className="form-control form-control-user"
                          placeholder="Password"
                          value={formData.password}
                          onChange={(e) => setFormData({...formData, password: e.target.value})}
                          required
                        />
                      </div>
                      <div className="col-sm-6">
                        <input
                          type="password"
                          className="form-control form-control-user"
                          placeholder="Repeat Password"
                          value={formData.repeatPassword}
                          onChange={(e) => setFormData({...formData, repeatPassword: e.target.value})}
                          required
                        />
                      </div>
                    </div>
                    <button
                      type="submit"
                      className="btn btn-primary btn-user btn-block"
                      disabled={isLoading}
                    >
                      {isLoading ? 'Registering...' : 'Register Account'}
                    </button>
                  </form>
                  <hr />
                  <div className="text-center">
                    <a className="small" href="/forgot-password">
                      Forgot Password?
                    </a>
                  </div>
                  <div className="text-center">
                    <a className="small" href="/login">
                      Already have an account? Login!
                    </a>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
} 