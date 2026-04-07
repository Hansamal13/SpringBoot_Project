import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation, useSearchParams } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import apiClient from '../services/apiClient';
import { LandingNavbar } from './landing/components/LandingNavbar';

export default function AuthPage({ isAdmin = false }) {
  const navigate = useNavigate();
  const location = useLocation();
  const { setToken, setUser } = useAuthStore();

  const [isLogin, setIsLogin] = useState(location.pathname.includes('login') || isAdmin);
  const [searchParams] = useSearchParams();

  const [loginForm, setLoginForm] = useState({ email: '', password: '' });
  const [registerForm, setRegisterForm] = useState({
    title: '', firstName: '', lastName: '', gender: '', email: '', phoneNumber: '',
    password: '', confirmPassword: '', role: '',
    studentId: '', academicYear: '', semester: '', faculty: '',
    position: '', technicianPosition: ''
  });

  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setIsLogin(location.pathname.includes('login') || isAdmin);
    // Show friendly error if redirected from backend with error
    const errorParam = searchParams.get('error');
    if (errorParam === 'not_registered') {
      setError('This account is not registered. Please register first.');
    } else if (errorParam === 'oauth_failed') {
      setError('Google login failed. Please try again.');
    } else {
      setError('');
    }
  }, [location.pathname, isAdmin, searchParams]);

  const toggleMode = () => {
    const newMode = !isLogin;
    navigate(newMode ? '/login' : '/register', { replace: true });
    setIsLogin(newMode);
    setError('');
  };

  const handleLoginChange = (e) => setLoginForm({ ...loginForm, [e.target.name]: e.target.value });
  const handleRegisterChange = (e) => setRegisterForm({ ...registerForm, [e.target.name]: e.target.value });



  const handleLoginSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const endpoint = isAdmin ? '/api/auth/admin/login' : '/api/auth/login';
      const payload = { email: loginForm.email, password: loginForm.password };

      const res = await apiClient.post(endpoint, payload);
      onAuthSuccess(res.data);
    } catch (err) {
      setError(err.response?.data?.message || 'Login failed.');
    } finally {
      setLoading(false);
    }
  };

  const handleRegisterSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (registerForm.password !== registerForm.confirmPassword) {
      setError('Passwords do not match.');
      return;
    }
    if (registerForm.password.length < 8) {
      setError('Password must be at least 8 characters.');
      return;
    }

    if (registerForm.password !== registerForm.confirmPassword) {
      setError('Passwords do not match.');
      return;
    }

    // Role-specific validation and endpoint
    let endpoint = '';
    let payload = {
      firstName: registerForm.firstName,
      lastName: registerForm.lastName,
      email: registerForm.email,
      phoneNumber: registerForm.phoneNumber,
      password: registerForm.password,
    };

    if (registerForm.role === 'STUDENT') {
      endpoint = '/api/auth/register/student';
      payload = {
        ...payload,
        confirmPassword: registerForm.confirmPassword,
        gender: registerForm.gender,
        faculty: registerForm.faculty,
        academicYear: registerForm.academicYear,
        semester: registerForm.semester
      };
      if (!payload.gender) return setError('Please select your gender.');
      if (!payload.faculty) return setError('Please select your faculty.');
      if (!payload.academicYear) return setError('Please select your academic year.');
      if (!payload.semester) return setError('Please select your semester.');

    } else if (registerForm.role === 'LECTURER') {
      endpoint = '/api/auth/register/lecturer';
      payload = {
        ...payload,
        confirmPassword: registerForm.confirmPassword,
        title: registerForm.title,
        faculty: registerForm.faculty,
        position: registerForm.position
      };
      if (!payload.title) return setError('Please select your title.');
      if (!payload.faculty) return setError('Please select your faculty.');
      if (!payload.position) return setError('Please select your position.');

    } else if (registerForm.role === 'TECHNICIAN') {
      endpoint = '/api/auth/register/technician';
      payload = {
        ...payload,
        confirmPassword: registerForm.confirmPassword,
        technicianPosition: registerForm.technicianPosition
      };
      if (!payload.technicianPosition) return setError('Please select your position.');
    } else {
      setError('Please select a role.');
      return;
    }

    setLoading(true);
    try {
      const res = await apiClient.post(endpoint, payload);
      if (registerForm.role === 'STUDENT') {
        onAuthSuccess(res.data);
      } else {
        // Lecturer/Technician are pending approval
        setError('');
        alert(res.data.message || 'Registration submitted for approval.');
        navigate('/login');
        setIsLogin(true);
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Registration failed.');
    } finally {
      setLoading(false);
    }
  };

  const onAuthSuccess = (data) => {
    setToken(data.token);
    setUser({
      userId: data.userId,
      firstName: data.firstName,
      lastName: data.lastName,
      email: data.email,
      role: data.role,
      profilePictureUrl: data.profilePictureUrl,
    });
    navigate('/dashboard');
  };

  return (
    <>
      <LandingNavbar mode={isAdmin ? 'adminAuth': 'landing'} />
      <div className="flex flex-col items-center justify-center min-h-screen w-full bg-[#f8fafc] px-4 pt-24 pb-6 overflow-hidden font-sans">
        <div className="relative w-full max-w-6xl h-[85vh] min-h-[620px] max-h-[820px] bg-white overflow-hidden rounded-[2.5rem] shadow-2xl border border-slate-100">

        {/* Sign Up Container */}
        <div
          className={`absolute top-0 left-0 h-full w-1/2 transition-all duration-700 ease-in-out z-10 flex flex-col items-center justify-center px-12 bg-white overflow-y-auto ${!isLogin ? 'translate-x-full opacity-100 z-40' : 'opacity-0 z-10 pointer-events-none'
            }`}
        >
          <div className="w-full max-w-sm flex flex-col items-center max-h-screen overflow-y-auto py-8 no-scrollbar">
            <form onSubmit={handleRegisterSubmit} className="flex flex-col w-full gap-4">
              <div className="text-center mb-4">
                <h1 className="text-4xl font-black text-[#0f172a] mb-2">Create Account</h1>
                <p className="text-slate-400 text-sm font-medium">Register for the Management Portal</p>
              </div>

              {error && !isLogin && <div className="w-full bg-red-50 text-red-500 p-4 rounded-2xl text-xs font-bold text-center border border-red-100">{error}</div>}

              <div className="flex w-full gap-3">
                <input type="text" name="firstName" placeholder="First Name*" value={registerForm.firstName} onChange={handleRegisterChange} required className="w-full bg-slate-50 border border-slate-100 rounded-2xl py-4 px-6 text-sm text-[#0f172a] outline-none focus:ring-2 focus:ring-[#2563eb] focus:bg-white transition-all shadow-sm" />
                <input type="text" name="lastName" placeholder="Last Name*" value={registerForm.lastName} onChange={handleRegisterChange} required className="w-full bg-slate-50 border border-slate-100 rounded-2xl py-4 px-6 text-sm text-[#0f172a] outline-none focus:ring-2 focus:ring-[#2563eb] focus:bg-white transition-all shadow-sm" />
              </div>

              <select name="role" value={registerForm.role} onChange={handleRegisterChange} required className={`w-full bg-slate-50 border border-slate-100 rounded-2xl py-4 px-6 outline-none focus:ring-2 focus:ring-[#2563eb] focus:bg-white transition-all text-sm font-bold appearance-none cursor-pointer ${registerForm.role ? 'text-[#0f172a]' : 'text-slate-400'}`}>
                <option value="">I am a...*</option>
                <option value="STUDENT">Student</option>
                <option value="LECTURER">Lecturer</option>
                <option value="TECHNICIAN">Technician</option>
              </select>

              <input type="email" name="email" placeholder="Email Address*" value={registerForm.email} onChange={(e) => handleRegisterChange({ target: { name: 'email', value: e.target.value.toLowerCase() } })}  required className="w-full bg-slate-50 border border-slate-100 rounded-2xl py-4 px-6 text-sm text-[#0f172a] outline-none focus:ring-2 focus:ring-[#2563eb] focus:bg-white transition-all shadow-sm" />
              
              <input type="tel" name="phoneNumber" placeholder="Phone Number*" value={registerForm.phoneNumber} onChange={handleRegisterChange} required className="w-full bg-slate-50 border border-slate-100 rounded-2xl py-4 px-6 text-sm text-[#0f172a] outline-none focus:ring-2 focus:ring-[#2563eb] focus:bg-white transition-all shadow-sm" />

              <div className="flex w-full gap-3">
                <input type="password" name="password" placeholder="Password*" value={registerForm.password} onChange={handleRegisterChange} required className="w-full bg-slate-50 border border-slate-100 rounded-2xl py-4 px-6 text-sm text-[#0f172a] outline-none focus:ring-2 focus:ring-[#2563eb] focus:bg-white transition-all shadow-sm" />
                <input type="password" name="confirmPassword" placeholder="Confirm Password*" value={registerForm.confirmPassword} onChange={handleRegisterChange} required className="w-full bg-slate-50 border border-slate-100 rounded-2xl py-4 px-6 text-sm text-[#0f172a] outline-none focus:ring-2 focus:ring-[#2563eb] focus:bg-white transition-all shadow-sm" />
              </div>

              {registerForm.role === 'STUDENT' && (
                <div className="flex flex-col gap-3 animate-in fade-in slide-in-from-top-2 duration-300">
                  <div className="flex w-full gap-3">
                    <select name="gender" value={registerForm.gender} onChange={handleRegisterChange} required className={`w-full bg-slate-50 border border-slate-100 rounded-2xl py-4 px-6 text-sm font-bold outline-none focus:ring-2 focus:ring-[#2563eb] focus:bg-white transition-all ${registerForm.gender ? 'text-[#0f172a]' : 'text-slate-400'}`}>
                      <option value="">Gender*</option>
                      <option value="Male">Male</option>
                      <option value="Female">Female</option>
                    </select>
                    <select name="faculty" value={registerForm.faculty} onChange={handleRegisterChange} required className={`w-full bg-slate-50 border border-slate-100 rounded-2xl py-4 px-6 outline-none focus:ring-2 focus:ring-[#2563eb] focus:bg-white transition-all text-sm font-bold ${registerForm.faculty ? 'text-[#0f172a]' : 'text-slate-400'}`}>
                      <option value="">Faculty*</option>
                      <option value="FOC">Computing</option>
                      <option value="FOE">Engineering</option>
                    </select>
                  </div>
                </div>
              )}

              <button type="submit" disabled={loading} className="w-full py-5 bg-[#2563eb] text-white font-black rounded-2xl uppercase text-xs tracking-[0.2em] shadow-xl shadow-blue-500/20 hover:brightness-110 active:scale-[0.98] transition-all disabled:opacity-50">
                {loading ? 'Processing...' : 'Register Account'}
              </button>
              
              <button type="button" onClick={toggleMode} className="w-full py-4 text-[#2563eb] font-black uppercase text-[10px] tracking-widest hover:underline">
                Already have an account? Sign In
              </button>
            </form>
          </div>
        </div>

        <div
          className={`absolute top-0 left-0 h-full w-1/2 transition-all duration-700 ease-in-out z-20 flex flex-col items-center justify-center px-12 bg-white ${!isLogin ? 'translate-x-full pointer-events-none' : ''
            }`}
        >
          <div className="w-full max-w-sm flex flex-col items-center">
            <form onSubmit={handleLoginSubmit} className="flex flex-col w-full gap-6">
              <div className="text-center mb-4">
                <h1 className="text-4xl font-black text-[#0f172a] mb-2">Welcome Back</h1>
                <p className="text-slate-400 text-sm font-medium">Access your System Central account</p>
              </div>

              {error && isLogin && <div className="w-full bg-red-50 text-red-500 p-4 rounded-2xl text-xs font-bold text-center border border-red-100">{error}</div>}

              <input type="email" name="email" placeholder="Corporate Email*" value={loginForm.email} onChange={(e) => handleLoginChange({ target: { name: 'email', value: e.target.value.toLowerCase() } })} required className="w-full bg-slate-50 border border-slate-100 rounded-2xl py-5 px-8 text-sm text-[#0f172a] outline-none focus:ring-2 focus:ring-[#2563eb] focus:bg-white transition-all shadow-sm" />
              
              <div className="flex flex-col gap-2">
                <input type="password" name="password" placeholder="Password*" value={loginForm.password} onChange={handleLoginChange} required className="w-full bg-slate-50 border border-slate-100 rounded-2xl py-5 px-8 text-sm text-[#0f172a] outline-none focus:ring-2 focus:ring-[#2563eb] focus:bg-white transition-all shadow-sm" />
                <button type="button" onClick={() => navigate('/forgot-password')} className="text-[#38bdf8] text-[10px] font-black uppercase tracking-widest text-right hover:underline">
                  Forget Password?
                </button>
              </div>

              <div className="flex flex-col gap-4 mt-2">
                <button type="submit" disabled={loading} className="w-full py-5 bg-[#2563eb] text-white font-black rounded-2xl uppercase text-xs tracking-[0.2em] shadow-xl shadow-blue-500/20 hover:brightness-110 active:scale-[0.98] transition-all">
                  {loading ? 'Authenticating...' : 'Sign In to Portal'}
                </button>
                <div className="flex items-center w-full my-1">
                  <div className="flex-grow border-t border-slate-100"></div>
                  <span className="px-4 text-slate-300 text-[10px] font-black uppercase tracking-widest">or</span>
                  <div className="flex-grow border-t border-slate-100"></div>
                </div>
                <button type="button" onClick={toggleMode} className="w-full py-5 border-2 border-slate-100 text-[#0f172a] font-black rounded-2xl uppercase text-xs tracking-[0.2em] hover:bg-slate-50 transition-all">
                  Create New Account
                </button>
              </div>
            </form>
          </div>
        </div>

        {/* Overlay Container - Blue Theme */}
        <div
          className={`absolute top-0 left-1/2 w-1/2 h-full overflow-hidden transition-transform duration-700 ease-in-out z-50 ${!isLogin ? '-translate-x-full' : ''
            }`}
        >
          <div
            className={`text-white relative -left-full h-full w-[200%] transition-transform duration-700 ease-in-out bg-cover bg-center bg-no-repeat ${!isLogin ? 'translate-x-1/2' : 'translate-x-0'
              }`}
            style={{
              backgroundImage: "linear-gradient(rgba(15, 23, 42, 0.9), rgba(30, 64, 175, 0.9)), url('/faculties/malabe.jpg')"
            }}
          >
            <div className={`absolute flex flex-col items-center justify-center p-12 text-center top-0 h-full w-1/2 transition-transform duration-700 ease-in-out ${!isLogin ? 'translate-x-0' : '-translate-x-[20%]'}`}>
              <h2 className="text-4xl font-black mb-6 italic tracking-tighter">New here?</h2>
              <p className="text-lg font-medium mb-10 text-slate-300 leading-relaxed">Join thousands of students and faculty members in the most advanced campus management ecosystem.</p>
              <div className="w-20 h-1 bg-[#38bdf8] rounded-full"></div>
            </div>

            <div className={`absolute right-0 flex flex-col items-center justify-center p-12 text-center top-0 h-full w-1/2 transition-transform duration-700 ease-in-out ${!isLogin ? 'translate-x-[20%]' : 'translate-x-0'}`}>
              <h2 className="text-4xl font-black mb-6 italic tracking-tighter">Portal Access</h2>
              <p className="text-lg font-medium mb-10 text-slate-300 leading-relaxed">Securely manage your academic journey, resource bookings, and institutional requests with ease.</p>
              <div className="w-20 h-1 bg-[#38bdf8] rounded-full"></div>
            </div>
          </div>
        </div>
        </div>
      </div>
    </>
  );
}
