import React, { useState, useEffect, useRef } from 'react';
import { 
  Activity, 
  Camera, 
  History, 
  User, 
  MoreHorizontal, 
  ChevronLeft, 
  Send, 
  Trash2, 
  LogOut, 
  Globe, 
  Lock, 
  Mail, 
  Award, 
  AlertTriangle, 
  Heart, 
  Smile, 
  ShieldAlert, 
  Sparkles,
  Info,
  ChevronRight,
  UserCheck,
  Download
} from 'lucide-react';
import './App.css';

// Original resource image files from Android res folder
import toothLogo from './assets/tooth_logo.png';
import toothAvatar from './assets/tooth_avatar.png';
import toothBanner from './assets/tooth_banner.png';
import toothLocation from './assets/tooth_location.png';
import icAssessment from './assets/ic_assessment.png';
import icHistory from './assets/ic_history.png';
import icScan from './assets/ic_scan.png';
import icTips from './assets/ic_tips.png';

// Base URL for API requests
const API_BASE = window.location.origin.includes("github.io")
  ? (localStorage.getItem("API_BASE_URL") || "https://aicaries-backend.cleverapps.io/")
  : (window.location.origin.includes("localhost:") 
      ? "http://localhost/aicaries/api/" 
      : `${window.location.origin}/aicaries/api/`);

function App() {
  // Navigation & Authentication state
  const [currentScreen, setCurrentScreen] = useState('splash'); // splash, language, signin, signup, forgot, home, assessment, scan, result_detail, history, profile, tips, chat
  const [language, setLanguage] = useState(() => localStorage.getItem('app_lang') || 'en');
  const [user, setUser] = useState(() => {
    try {
      const saved = localStorage.getItem('app_user');
      return saved ? JSON.parse(saved) : null;
    } catch {
      return null;
    }
  });

  const [lastAssessmentScore, setLastAssessmentScore] = useState(() => {
    return parseInt(localStorage.getItem('app_assessment_score') || '0', 10);
  });

  const [toast, setToast] = useState(null);
  const [selectedResult, setSelectedResultState] = useState(null);
  const [resetEmail, setResetEmail] = useState('');
  const [resetToken, setResetToken] = useState('');

  const showToast = (message, type = 'info') => {
    setToast({ message, type });
    setTimeout(() => setToast(null), 3000);
  };

  const setSelectedResult = (res) => {
    setSelectedResultState(res);
  };

  // 1. Splash Screen lifecycle
  useEffect(() => {
    if (currentScreen === 'splash') {
      const timer = setTimeout(() => {
        if (user) {
          setCurrentScreen('home');
        } else {
          const langSet = localStorage.getItem('app_lang');
          if (!langSet) {
            setCurrentScreen('language');
          } else {
            setCurrentScreen('signin');
          }
        }
      }, 2200);
      return () => clearTimeout(timer);
    }
  }, [currentScreen, user]);

  // Intercept password reset token from URL on mount
  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const token = params.get('token');
    const email = params.get('email');
    if (token && email) {
      setResetEmail(email);
      setResetToken(token);
      setCurrentScreen('reset_password');
    }
  }, []);

  const handleLogout = () => {
    localStorage.removeItem('app_user');
    localStorage.removeItem('app_assessment_score');
    setUser(null);
    setLastAssessmentScore(0);
    setCurrentScreen('signin');
    showToast("Logged out successfully");
  };

  const handleNavClick = (screenName) => {
    if (!user) {
      setCurrentScreen('signin');
      return;
    }
    setCurrentScreen(screenName);
  };

  // Checks whether sidebar should be shown
  const showSidebar = user && ['home', 'history', 'chat', 'profile', 'tips', 'assessment', 'scan', 'result_detail'].includes(currentScreen);

  return (
    <div className="app-layout">
      {toast && (
        <div className="toast-msg">
          <Info size={16} />
          <span>{toast.message}</span>
        </div>
      )}

      {/* Render sidebar on larger screens */}
      {showSidebar && (
        <Sidebar 
          currentScreen={currentScreen} 
          setCurrentScreen={setCurrentScreen} 
          handleLogout={handleLogout} 
          user={user} 
        />
      )}

      {/* Main content pane */}
      <div className={user && currentScreen !== 'splash' && currentScreen !== 'language' ? "main-content" : "auth-wrapper"}>
        {currentScreen === 'splash' && <SplashScreen />}
        
        {currentScreen === 'language' && (
          <LanguageScreen 
            setLanguage={setLanguage} 
            setCurrentScreen={setCurrentScreen} 
          />
        )}
        
        {currentScreen === 'signin' && (
          <SignInScreen 
            setUser={setUser} 
            setCurrentScreen={setCurrentScreen} 
            showToast={showToast} 
          />
        )}
        
        {currentScreen === 'signup' && (
          <SignUpScreen 
            setUser={setUser} 
            setCurrentScreen={setCurrentScreen} 
            showToast={showToast} 
          />
        )}
        
        {currentScreen === 'forgot' && (
          <ForgotPasswordScreen 
            setCurrentScreen={setCurrentScreen} 
            showToast={showToast} 
          />
        )}
        
        {currentScreen === 'reset_password' && (
          <ResetPasswordScreen 
            email={resetEmail}
            token={resetToken}
            setCurrentScreen={setCurrentScreen} 
            showToast={showToast} 
          />
        )}

        {/* Dashboard and Inner App views */}
        {user && (
          <>
            {currentScreen === 'home' && (
              <HomeScreen 
                user={user} 
                setCurrentScreen={setCurrentScreen} 
                lastAssessmentScore={lastAssessmentScore}
                setSelectedResult={setSelectedResult}
                showToast={showToast}
              />
            )}
            
            {currentScreen === 'assessment' && (
              <AssessmentScreen 
                user={user} 
                setCurrentScreen={setCurrentScreen} 
                setLastAssessmentScore={setLastAssessmentScore}
                setSelectedResult={setSelectedResult}
                showToast={showToast}
              />
            )}
            
            {currentScreen === 'scan' && (
              <ScanScreen 
                user={user} 
                setCurrentScreen={setCurrentScreen} 
                lastAssessmentScore={lastAssessmentScore}
                setSelectedResult={setSelectedResult}
                showToast={showToast}
              />
            )}
            
            {currentScreen === 'result_detail' && (
              <ResultDetailScreen 
                result={selectedResult} 
                setCurrentScreen={setCurrentScreen} 
                user={user}
              />
            )}
            
            {currentScreen === 'history' && (
              <HistoryScreen 
                user={user} 
                setCurrentScreen={setCurrentScreen} 
                setSelectedResult={setSelectedResult}
                showToast={showToast}
              />
            )}
            
            {currentScreen === 'profile' && (
              <ProfileScreen 
                user={user} 
                setUser={setUser} 
                handleLogout={handleLogout} 
                setCurrentScreen={setCurrentScreen} 
                showToast={showToast}
              />
            )}
            
            {currentScreen === 'tips' && (
              <TipsScreen />
            )}
            
            {currentScreen === 'chat' && (
              <ChatScreen 
                user={user} 
                setCurrentScreen={setCurrentScreen} 
                showToast={showToast}
              />
            )}

            {/* Mobile Bottom Tab Bar */}
            {['home', 'history', 'chat', 'profile', 'tips'].includes(currentScreen) && (
              <div className="bottom-nav">
                <div 
                  className={`nav-item ${currentScreen === 'home' ? 'active' : ''}`}
                  onClick={() => handleNavClick('home')}
                >
                  <Activity size={20} />
                  <span>Home</span>
                </div>
                <div 
                  className={`nav-item ${currentScreen === 'history' ? 'active' : ''}`}
                  onClick={() => handleNavClick('history')}
                >
                  <History size={20} />
                  <span>History</span>
                </div>
                <div 
                  className={`nav-item ${currentScreen === 'chat' ? 'active' : ''}`}
                  onClick={() => handleNavClick('chat')}
                >
                  <Smile size={20} />
                  <span>AI Chat</span>
                </div>
                <div 
                  className={`nav-item ${currentScreen === 'tips' ? 'active' : ''}`}
                  onClick={() => handleNavClick('tips')}
                >
                  <Sparkles size={20} />
                  <span>Tips</span>
                </div>
                <div 
                  className={`nav-item ${currentScreen === 'profile' ? 'active' : ''}`}
                  onClick={() => handleNavClick('profile')}
                >
                  <User size={20} />
                  <span>Profile</span>
                </div>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
}

// -------------------------------------------------------------------
// DESKTOP NAVIGATION SIDEBAR
// -------------------------------------------------------------------
function Sidebar({ currentScreen, setCurrentScreen, handleLogout, user }) {
  const firstName = user.name.split(' ')[0] || 'User';

  return (
    <div className="sidebar">
      <div className="sidebar-logo">
        <img src={toothLogo} alt="AICaries logo" className="sidebar-logo-img" />
        <span>AICaries</span>
      </div>

      <div className="sidebar-menu">
        <div 
          className={`sidebar-menu-item ${currentScreen === 'home' ? 'active' : ''}`}
          onClick={() => setCurrentScreen('home')}
        >
          <Activity size={18} />
          <span>Home Dashboard</span>
        </div>
        <div 
          className={`sidebar-menu-item ${currentScreen === 'assessment' ? 'active' : ''}`}
          onClick={() => setCurrentScreen('assessment')}
        >
          <Award size={18} />
          <span>Risk Assessment</span>
        </div>
        <div 
          className={`sidebar-menu-item ${currentScreen === 'scan' ? 'active' : ''}`}
          onClick={() => setCurrentScreen('scan')}
        >
          <Camera size={18} />
          <span>AI Dental Scan</span>
        </div>
        <div 
          className={`sidebar-menu-item ${currentScreen === 'history' ? 'active' : ''}`}
          onClick={() => setCurrentScreen('history')}
        >
          <History size={18} />
          <span>Checkup History</span>
        </div>
        <div 
          className={`sidebar-menu-item ${currentScreen === 'chat' ? 'active' : ''}`}
          onClick={() => setCurrentScreen('chat')}
        >
          <Smile size={18} />
          <span>Teeth AI Chatbot</span>
        </div>
        <div 
          className={`sidebar-menu-item ${currentScreen === 'tips' ? 'active' : ''}`}
          onClick={() => setCurrentScreen('tips')}
        >
          <Sparkles size={18} />
          <span>Oral Care Tips</span>
        </div>
        <div 
          className={`sidebar-menu-item ${currentScreen === 'profile' ? 'active' : ''}`}
          onClick={() => setCurrentScreen('profile')}
        >
          <User size={18} />
          <span>Profile Settings</span>
        </div>
      </div>

      <div className="sidebar-footer">
        <div className="sidebar-user-info">
          <img src={toothAvatar} alt="user avatar" className="sidebar-user-avatar" />
          <div>
            <div className="name">{firstName}</div>
            <div className="email">{user.email}</div>
          </div>
        </div>
        <div className="sidebar-logout-btn" onClick={handleLogout} title="Sign Out">
          <LogOut size={18} />
        </div>
      </div>
    </div>
  );
}

// -------------------------------------------------------------------
// SCREEN 1: SPLASH SCREEN
// -------------------------------------------------------------------
function SplashScreen() {
  return (
    <div className="splash-container">
      <img src={toothLogo} alt="AICaries logo" className="splash-logo-img" />
      <h1 className="splash-title">AICaries</h1>
      <p className="splash-subtitle">Smart AI Dental Diagnostics</p>
      <div className="spinner">
        <Activity size={24} color="var(--primary)" />
      </div>
    </div>
  );
}

// -------------------------------------------------------------------
// SCREEN 2: LANGUAGE SELECTION
// -------------------------------------------------------------------
function LanguageScreen({ setLanguage, setCurrentScreen }) {
  const selectLang = (lang) => {
    localStorage.setItem('app_lang', lang);
    setLanguage(lang);
    setCurrentScreen('signin');
  };

  return (
    <div className="auth-container animate-slide-in">
      <div className="auth-header">
        <img src={toothLogo} alt="Logo" style={{ width: '70px', height: '70px', margin: '0 auto 16px auto', display: 'block' }} />
        <h1>Select Language</h1>
        <p>Choose your preferred language</p>
      </div>

      <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
        <button className="btn-primary" onClick={() => selectLang('en')}>
          English
        </button>
        <button 
          className="btn-primary" 
          style={{ background: 'var(--bg-input)', color: 'var(--text-primary)', border: '1px solid var(--border-color)', boxShadow: 'none' }} 
          onClick={() => selectLang('id')}
        >
          Bahasa Indonesia
        </button>
      </div>
    </div>
  );
}

// -------------------------------------------------------------------
// SCREEN 3: SIGN IN SCREEN
// -------------------------------------------------------------------
function SignInScreen({ setUser, setCurrentScreen, showToast }) {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSignIn = async (e) => {
    e.preventDefault();
    if (!email || !password) {
      showToast("Please fill in all fields", "warning");
      return;
    }

    setLoading(true);
    try {
      const res = await fetch(`${API_BASE}login.php`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password })
      });
      const data = await res.json();
      setLoading(false);

      if (data.success) {
        const userData = {
          userId: data.data.user_id,
          name: data.data.name,
          email: data.data.email,
          token: data.data.token
        };
        localStorage.setItem('app_user', JSON.stringify(userData));
        setUser(userData);
        setCurrentScreen('home');
        showToast(`Welcome back, ${data.data.name}!`);
      } else {
        showToast(data.message || "Login failed", "warning");
      }
    } catch (err) {
      setLoading(false);
      showToast("Server connection error", "warning");
    }
  };

  const handleGoogleSignIn = () => {
    const googleUser = {
      userId: 999,
      name: "Google User",
      email: "googleuser@example.com",
      token: "google_login"
    };
    localStorage.setItem('app_user', JSON.stringify(googleUser));
    setUser(googleUser);
    setCurrentScreen('home');
    showToast("Signed in via Google account!");
  };

  return (
    <div className="auth-container animate-slide-in">
      <div className="auth-header">
        <img src={toothLogo} alt="Logo" style={{ width: '64px', height: '64px', margin: '0 auto 12px auto', display: 'block' }} />
        <h1>Sign In</h1>
        <p>Access your smart dental analyzer</p>
      </div>

      <form onSubmit={handleSignIn}>
        <div className="form-group">
          <label>Email Address</label>
          <input 
            type="email" 
            className="input-field" 
            placeholder="name@example.com" 
            value={email}
            onChange={(e) => setEmail(e.target.value)}
          />
        </div>
        <div className="form-group">
          <label>Password</label>
          <input 
            type="password" 
            className="input-field" 
            placeholder="Enter password" 
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
        </div>

        <div style={{ textAlign: 'right', marginBottom: '24px' }}>
          <span 
            style={{ fontSize: '0.82rem', color: 'var(--primary)', fontWeight: '700', cursor: 'pointer' }}
            onClick={() => setCurrentScreen('forgot')}
          >
            Forgot Password?
          </span>
        </div>

        <button type="submit" className="btn-primary" style={{ width: '100%' }} disabled={loading}>
          {loading ? "Signing In..." : "Sign In"}
        </button>
      </form>

      <div className="auth-divider">OR</div>

      <button className="auth-btn-google" onClick={handleGoogleSignIn}>
        <img src={toothAvatar} alt="Google Avatar" style={{ borderRadius: '50%' }} />
        <span>Continue with Google</span>
      </button>

      <p className="auth-footer-text">
        Don't have an account? <span onClick={() => setCurrentScreen('signup')}>Create Account</span>
      </p>
    </div>
  );
}

// -------------------------------------------------------------------
// SCREEN 4: SIGN UP SCREEN
// -------------------------------------------------------------------
function SignUpScreen({ setUser, setCurrentScreen, showToast }) {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSignUp = async (e) => {
    e.preventDefault();
    if (!name || !email || !password) {
      showToast("Please fill in all fields", "warning");
      return;
    }

    setLoading(true);
    try {
      const res = await fetch(`${API_BASE}register.php`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ name, email, password })
      });
      const data = await res.json();
      setLoading(false);

      if (data.success) {
        const userData = {
          userId: data.data.user_id,
          name: data.data.name,
          email: data.data.email,
          token: data.data.token
        };
        localStorage.setItem('app_user', JSON.stringify(userData));
        setUser(userData);
        setCurrentScreen('home');
        showToast("Account created successfully!");
      } else {
        showToast(data.message || "Registration failed", "warning");
      }
    } catch (err) {
      setLoading(false);
      showToast("Server connection error", "warning");
    }
  };

  return (
    <div className="auth-container animate-slide-in">
      <div className="auth-header">
        <img src={toothLogo} alt="Logo" style={{ width: '64px', height: '64px', margin: '0 auto 12px auto', display: 'block' }} />
        <h1>Create Account</h1>
        <p>Join AICaries for dental diagnostics</p>
      </div>

      <form onSubmit={handleSignUp}>
        <div className="form-group">
          <label>Full Name</label>
          <input 
            type="text" 
            className="input-field" 
            placeholder="John Doe" 
            value={name}
            onChange={(e) => setName(e.target.value)}
          />
        </div>
        <div className="form-group">
          <label>Email Address</label>
          <input 
            type="email" 
            className="input-field" 
            placeholder="john@example.com" 
            value={email}
            onChange={(e) => setEmail(e.target.value)}
          />
        </div>
        <div className="form-group">
          <label>Password</label>
          <input 
            type="password" 
            className="input-field" 
            placeholder="Create password" 
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
        </div>

        <button type="submit" className="btn-primary" style={{ width: '100%', marginTop: '12px' }} disabled={loading}>
          {loading ? "Creating Account..." : "Create Account"}
        </button>
      </form>

      <p className="auth-footer-text">
        Already have an account? <span onClick={() => setCurrentScreen('signin')}>Sign In</span>
      </p>
    </div>
  );
}

// -------------------------------------------------------------------
// SCREEN 5: FORGOT PASSWORD SCREEN
// -------------------------------------------------------------------
function ForgotPasswordScreen({ setCurrentScreen, showToast }) {
  const [email, setEmail] = useState('');
  const [loading, setLoading] = useState(false);

  const handleForgot = async (e) => {
    e.preventDefault();
    if (!email) {
      showToast("Please enter your email", "warning");
      return;
    }

    setLoading(true);
    try {
      const res = await fetch(`${API_BASE}forgot_password.php`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email })
      });
      const data = await res.json();
      setLoading(false);

      if (data.success) {
        showToast("Password reset link sent to your email!");
        setCurrentScreen('signin');
      } else {
        showToast(data.message || "Failed to submit request", "warning");
      }
    } catch (err) {
      setLoading(false);
      showToast("Server connection error", "warning");
    }
  };

  return (
    <div className="auth-container animate-slide-in">
      <div className="auth-header">
        <img src={toothLogo} alt="Logo" style={{ width: '64px', height: '64px', margin: '0 auto 12px auto', display: 'block' }} />
        <h1>Forgot Password</h1>
        <p>Enter email to receive recovery instructions</p>
      </div>

      <form onSubmit={handleForgot}>
        <div className="form-group" style={{ marginBottom: '32px' }}>
          <label>Email Address</label>
          <input 
            type="email" 
            className="input-field" 
            placeholder="name@example.com" 
            value={email}
            onChange={(e) => setEmail(e.target.value)}
          />
        </div>

        <button type="submit" className="btn-primary" style={{ width: '100%' }} disabled={loading}>
          {loading ? "Sending..." : "Send Instructions"}
        </button>
      </form>

      <p className="auth-footer-text">
        Remembered password? <span onClick={() => setCurrentScreen('signin')}>Sign In</span>
      </p>
    </div>
  );
}

// -------------------------------------------------------------------
// SCREEN 5.5: RESET PASSWORD SCREEN
// -------------------------------------------------------------------
function ResetPasswordScreen({ email, token, setCurrentScreen, showToast }) {
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [loading, setLoading] = useState(false);

  const handleReset = async (e) => {
    e.preventDefault();
    if (!newPassword || !confirmPassword) {
      showToast("Please fill in all fields", "warning");
      return;
    }
    if (newPassword !== confirmPassword) {
      showToast("Passwords do not match", "warning");
      return;
    }
    if (newPassword.length < 6) {
      showToast("Password must be at least 6 characters", "warning");
      return;
    }

    setLoading(true);
    try {
      const res = await fetch(`${API_BASE}reset_password.php`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          email,
          token,
          new_password: newPassword
        })
      });
      const data = await res.json();
      setLoading(false);

      if (data.success) {
        showToast("Password reset successful! Please sign in.");
        // Clear URL parameters
        window.history.replaceState({}, document.title, window.location.pathname);
        setCurrentScreen('signin');
      } else {
        showToast(data.message || "Failed to reset password", "warning");
      }
    } catch (err) {
      setLoading(false);
      showToast("Server connection error", "warning");
    }
  };

  return (
    <div className="auth-container animate-slide-in">
      <div className="auth-header">
        <img src={toothLogo} alt="Logo" style={{ width: '64px', height: '64px', margin: '0 auto 12px auto', display: 'block' }} />
        <h1>Reset Password</h1>
        <p>Enter a secure new password for your account</p>
      </div>

      <form onSubmit={handleReset}>
        <div className="form-group">
          <label>New Password</label>
          <input 
            type="password" 
            className="input-field" 
            placeholder="Min 6 characters" 
            value={newPassword}
            onChange={(e) => setNewPassword(e.target.value)}
          />
        </div>
        <div className="form-group" style={{ marginBottom: '32px' }}>
          <label>Confirm New Password</label>
          <input 
            type="password" 
            className="input-field" 
            placeholder="Re-enter password" 
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
          />
        </div>

        <button type="submit" className="btn-primary" style={{ width: '100%' }} disabled={loading}>
          {loading ? "Resetting..." : "Reset Password"}
        </button>
      </form>
    </div>
  );
}

// -------------------------------------------------------------------
// SCREEN 6: HOME SCREEN (DASHBOARD)
// -------------------------------------------------------------------
function HomeScreen({ user, setCurrentScreen, lastAssessmentScore, setSelectedResult, showToast }) {
  const [loading, setLoading] = useState(true);
  const [latestResult, setLatestResult] = useState(null);

  useEffect(() => {
    const fetchLatest = async () => {
      try {
        const res = await fetch(`${API_BASE}get_results.php`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ user_id: user.userId })
        });
        const data = await res.json();
        setLoading(false);

        if (data.success && data.data && data.data.length > 0) {
          const latest = data.data[0];
          setLatestResult(latest);
          if (latest.overall_score) {
            localStorage.setItem('app_assessment_score', latest.overall_score.toString());
          }
        }
      } catch (err) {
        setLoading(false);
      }
    };
    fetchLatest();
  }, [user]);

  const handleLatestClick = () => {
    if (latestResult) {
      let recommendationsList = [];
      try {
        recommendationsList = latestResult.recommendations || [];
      } catch {}

      const answers = typeof latestResult.answers === 'string' 
        ? JSON.parse(latestResult.answers) 
        : latestResult.answers;

      const formattedResult = {
        result_id: latestResult.id,
        score: latestResult.overall_score,
        risk_level: latestResult.risk_level,
        result_type: latestResult.result_type,
        demographic: latestResult.demographic_score || latestResult.overall_score,
        socioeconomic: latestResult.socioeconomic_score || latestResult.overall_score,
        dietary: latestResult.dietary_score || latestResult.overall_score,
        hygiene: latestResult.hygiene_score || (answers && answers.confidence ? Math.round(answers.confidence > 1 ? answers.confidence : answers.confidence * 100) : 0),
        dentalHistory: latestResult.dental_history_score || 100,
        confidence: answers && answers.confidence ? answers.confidence : null,
        prediction: answers && answers.prediction ? answers.prediction : null,
        recommendations: recommendationsList
      };

      setSelectedResult(formattedResult);
      setCurrentScreen('result_detail');
    }
  };

  const handleDentistSearch = () => {
    window.open("https://www.google.com/maps/search/dentist/", "_blank");
  };

  const getRiskClass = (level) => {
    if (!level) return 'low';
    const lvl = level.toLowerCase();
    if (lvl.includes('high')) return 'high';
    if (lvl.includes('mod')) return 'moderate';
    return 'low';
  };

  const firstName = user.name.split(' ')[0] || 'User';

  return (
    <div className="screen-container animate-slide-in">
      <div className="home-welcome-section">
        <div>
          <p>Welcome back,</p>
          <h1>{firstName} 👋</h1>
        </div>
        <div className="btn-icon-header" onClick={() => setCurrentScreen('profile')} style={{ borderRadius: '50%' }}>
          <img src={toothAvatar} alt="Profile" style={{ width: '100%', height: '100%', borderRadius: '50%' }} />
        </div>
      </div>

      <div className="home-dashboard-grid">
        {/* Left Column: Radial score circle */}
        <div className="home-score-card" onClick={handleLatestClick} style={{ cursor: latestResult ? 'pointer' : 'default' }}>
          <span style={{ fontSize: '0.82rem', color: 'var(--text-secondary)', fontWeight: '700', textTransform: 'uppercase' }}>
            Latest Diagnostic Check
          </span>
          
          <div 
            className="circular-progress" 
            style={{ 
              width: '180px',
              height: '180px',
              '--percent': latestResult ? latestResult.overall_score : 0,
              '--fill-color': latestResult ? `var(--risk-${getRiskClass(latestResult.risk_level)})` : 'var(--primary)'
            }}
          >
            <div className="circular-progress-text">
              <span className="value" style={{ fontSize: '3rem' }}>{latestResult ? latestResult.overall_score : '--'}</span>
              <span className="label" style={{ fontSize: '0.8rem' }}>% Score</span>
            </div>
          </div>

          <div style={{ marginTop: '8px' }}>
            {loading ? (
              <span style={{ fontSize: '0.95rem', fontWeight: '600' }}>Loading last score...</span>
            ) : latestResult ? (
              <>
                <h2 style={{ fontSize: '1.5rem', fontWeight: '800' }}>
                  {latestResult.result_type === 'scan' ? 'AI Teeth Scan' : 'Dental Checkup'}
                </h2>
                <span className={`badge-risk ${getRiskClass(latestResult.risk_level)}`} style={{ marginTop: '12px' }}>
                  {latestResult.risk_level} Risk Level
                </span>
              </>
            ) : (
              <>
                <h2 style={{ fontSize: '1.2rem', fontWeight: '800' }}>No Diagnostics Logged</h2>
                <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginTop: '4px' }}>
                  Complete assessment questionnaire or scanning first.
                </p>
              </>
            )}
          </div>
        </div>

        {/* Right Column: Cards list + dentist finder */}
        <div>
          <div className="card-grid">
            <div className="feature-card" onClick={() => setCurrentScreen('assessment')}>
              <img src={icAssessment} alt="Assessment Icon" className="feature-card-img" />
              <h3>Questionnaire</h3>
              <p>Analyze demographic and hygiene risk scores</p>
            </div>

            <div className="feature-card" onClick={() => setCurrentScreen('scan')}>
              <img src={icScan} alt="AI Scan Icon" className="feature-card-img" />
              <h3>AI Caries Scan</h3>
              <p>Scan your teeth with AI camera model</p>
            </div>

            <div className="feature-card" onClick={() => setCurrentScreen('history')}>
              <img src={icHistory} alt="History Icon" className="feature-card-img" />
              <h3>History Reports</h3>
              <p>Access previous dental diagnostic files</p>
            </div>

            <div className="feature-card" onClick={() => setCurrentScreen('chat')}>
              <img src={toothAvatar} alt="Dental Bot Icon" className="feature-card-img" style={{ borderRadius: '50%' }} />
              <h3>AI Dental Bot</h3>
              <p>Discuss problems with Llama 3.3 chatbot</p>
            </div>
          </div>

          {/* Location locator */}
          <div className="card-dentist-cta" onClick={handleDentistSearch}>
            <div className="card-dentist-cta-info">
              <h3>Find Dentist Nearby</h3>
              <p>Locate clinics on Google Maps to consult a local dentist for cavities and pain.</p>
            </div>
            <img src={toothLocation} alt="Location Map" className="card-dentist-cta-img" />
          </div>
        </div>
      </div>
    </div>
  );
}

const questionsList = [
  {
    category: "Demographic Information",
    question: "What is your age group?",
    options: ["Under 18", "18–30", "31–45", "46–60", "60+"],
    key: "age"
  },
  {
    category: "Demographic Information",
    question: "What is your gender?",
    options: ["Male", "Female", "Other", "Prefer not to say"],
    key: "gender"
  },
  {
    category: "Demographic Information",
    question: "Place of residence?",
    options: ["Urban", "Rural"],
    key: "residence"
  },
  {
    category: "Demographic Information",
    question: "Level of education?",
    options: ["No formal education", "Primary school", "Secondary school", "College/University"],
    key: "education"
  },
  {
    category: "Socio-Economic Indicators",
    question: "Monthly household income range?",
    options: ["Below ₹10,000", "₹10,000–₹30,000", "₹30,000–₹60,000", "Above ₹60,000"],
    key: "income"
  },
  {
    category: "Socio-Economic Indicators",
    question: "Do you have access to regular dental care?",
    options: ["Yes", "No"],
    key: "dentalAccess"
  },
  {
    category: "Dietary Habits",
    question: "How often do you consume sugary foods or drinks?",
    options: ["Rarely", "1–2 times/week", "3–5 times/week", "Daily"],
    key: "sugar"
  },
  {
    category: "Dietary Habits",
    question: "Do you snack between meals?",
    options: ["No", "Sometimes", "Daily", "Multiple times daily"],
    key: "snacking"
  },
  {
    category: "Hygiene Practices",
    question: "How many times do you brush daily?",
    options: ["Never", "Once", "Twice", "3 or more times"],
    key: "brushing"
  },
  {
    category: "Hygiene Practices",
    question: "Do you use floss or mouthwash?",
    options: ["No", "Floss only", "Mouthwash only", "Both"],
    key: "hygieneAids"
  },
  {
    category: "Dental History",
    question: "Have you been diagnosed with dental caries before?",
    options: ["Yes", "No"],
    key: "previousCaries"
  },
  {
    category: "Dental History",
    question: "Tooth pain or sensitivity in the past 6 months?",
    options: ["Yes", "No"],
    key: "toothPain"
  }
];

// -------------------------------------------------------------------
// SCREEN 7: DENTAL RISK ASSESSMENT (QUESTIONNAIRE)
// -------------------------------------------------------------------
function AssessmentScreen({ user, setCurrentScreen, setLastAssessmentScore, setSelectedResult, showToast }) {
  const [currentIndex, setCurrentIndex] = useState(0);
  const [answers, setAnswers] = useState({});
  const [saving, setSaving] = useState(false);

  const q = questionsList[currentIndex];
  const total = questionsList.length;
  const progressPercent = Math.round(((currentIndex + 1) * 100) / total);

  const handleSelectOption = (option) => {
    setAnswers(prev => ({ ...prev, [q.key]: option }));
  };

  const handleBack = () => {
    if (currentIndex > 0) {
      setCurrentIndex(currentIndex - 1);
    } else {
      setCurrentScreen('home');
    }
  };

  const handleNext = () => {
    if (currentIndex < total - 1) {
      setCurrentIndex(currentIndex + 1);
    } else {
      calculateAndSave();
    }
  };

  const calculateAndSave = async () => {
    setSaving(true);

    let demographic = 0;
    switch (answers["age"]) {
      case "Under 18": demographic += 5; break;
      case "18–30": demographic += 2; break;
      case "31–45": demographic += 4; break;
      case "46–60": demographic += 6; break;
      case "60+": demographic += 8; break;
    }
    if (answers["gender"] === "Female") demographic += 2;
    if (answers["residence"] === "Rural") demographic += 5;
    switch (answers["education"]) {
      case "No formal education": demographic += 8; break;
      case "Primary school": demographic += 5; break;
      case "Secondary school": demographic += 3; break;
    }
    demographic = Math.min(demographic, 100);

    let socioeconomic = 0;
    switch (answers["income"]) {
      case "Below ₹10,000": socioeconomic += 30; break;
      case "₹10,000–₹30,000": socioeconomic += 20; break;
      case "₹30,000–₹60,000": socioeconomic += 10; break;
    }
    if (answers["dentalAccess"] === "No") socioeconomic += 25;
    socioeconomic = Math.min(socioeconomic, 100);

    let dietary = 0;
    switch (answers["sugar"]) {
      case "1–2 times/week": dietary += 15; break;
      case "3–5 times/week": dietary += 30; break;
      case "Daily": dietary += 50; break;
    }
    switch (answers["snacking"]) {
      case "Sometimes": dietary += 10; break;
      case "Daily": dietary += 20; break;
      case "Multiple times daily": dietary += 35; break;
    }
    dietary = Math.min(dietary, 100);

    let hygiene = 0;
    switch (answers["brushing"]) {
      case "Never": hygiene += 60; break;
      case "Once": hygiene += 30; break;
    }
    switch (answers["hygieneAids"]) {
      case "No": hygiene += 25; break;
      case "Floss only": hygiene += 10; break;
      case "Mouthwash only": hygiene += 10; break;
    }
    hygiene = Math.min(hygiene, 100);

    let dentalHistory = 0;
    if (answers["previousCaries"] === "Yes") dentalHistory += 50;
    if (answers["toothPain"] === "Yes") dentalHistory += 30;
    dentalHistory = Math.min(dentalHistory, 100);

    const overall = Math.min(
      Math.round(
        demographic * 0.10 +
        socioeconomic * 0.25 +
        dietary * 0.25 +
        hygiene * 0.30 +
        dentalHistory * 0.10
      ),
      100
    );

    const risk = overall < 30 ? "Low" : overall < 60 ? "Moderate" : "High";

    const recommendations = [];
    if (hygiene > 40) {
      recommendations.push("Brush your teeth at least twice daily using fluoride toothpaste.");
    }
    if (answers["hygieneAids"] === "No") {
      recommendations.push("Start flossing daily to remove plaque between teeth.");
    }
    if (dietary > 30) {
      recommendations.push("Reduce sugary foods and soft drinks.");
    }
    if (answers["snacking"] === "Daily" || answers["snacking"] === "Multiple times daily") {
      recommendations.push("Avoid frequent snacking between meals.");
    }
    if (answers["dentalAccess"] === "No") {
      recommendations.push("Try to access regular dental care services.");
    }
    if (answers["previousCaries"] === "Yes") {
      recommendations.push("You have a history of caries. Visit a dentist soon.");
    }
    if (answers["toothPain"] === "Yes") {
      recommendations.push("Tooth pain may indicate active decay.");
    }
    recommendations.push("Visit your dentist every 6 months.");
    recommendations.push("Drink fluoridated water regularly.");

    setLastAssessmentScore(overall);
    localStorage.setItem('app_assessment_score', overall.toString());

    try {
      const res = await fetch(`${API_BASE}save_result.php`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          user_id: user.userId,
          overall_score: overall,
          risk_level: risk,
          demographic_score: demographic,
          socioeconomic_score: socioeconomic,
          dietary_score: dietary,
          hygiene_score: hygiene,
          dental_history_score: dentalHistory,
          answers: answers,
          result_type: "assessment",
          recommendations: recommendations
        })
      });

      const data = await res.json();
      setSaving(false);

      if (data.success) {
        const resultId = data.data?.result_id || 0;
        const formattedResult = {
          result_id: resultId,
          score: overall,
          risk_level: risk,
          result_type: "assessment",
          demographic,
          socioeconomic,
          dietary,
          hygiene,
          dentalHistory,
          recommendations
        };
        setSelectedResult(formattedResult);
        setCurrentScreen('result_detail');
        showToast("Assessment results saved!");
      } else {
        showToast(data.message || "Failed to save results", "warning");
      }
    } catch (err) {
      setSaving(false);
      showToast("Server communication error", "warning");
    }
  };

  const isOptionSelected = answers[q.key] !== undefined;

  return (
    <div className="screen-container animate-slide-in">
      <div className="header-bar">
        <div className="btn-icon-header" onClick={handleBack}>
          <ChevronLeft size={18} />
        </div>
        <h2>Teeth Risk Assessment</h2>
        <div style={{ width: '40px' }} />
      </div>

      <div className="progress-bar-container" style={{ marginBottom: '32px' }}>
        <div className="progress-bar-fill" style={{ width: `${progressPercent}%` }}></div>
      </div>

      <div className="quest-wrapper">
        <div className="quest-card">
          <span className="quest-category">{q.category}</span>
          <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)', display: 'block', marginBottom: '8px' }}>
            Question {currentIndex + 1} of {total}
          </span>
          <h2 className="quest-title">{q.question}</h2>

          <div className="quest-options-list">
            {q.options.map((option, idx) => (
              <div 
                key={idx}
                className={`quest-option-item ${answers[q.key] === option ? 'selected' : ''}`}
                onClick={() => handleSelectOption(option)}
              >
                <span>{option}</span>
                <div className="quest-circle"></div>
              </div>
            ))}
          </div>

          <div className="quest-footer">
            <button className="quest-btn-back" onClick={handleBack}>
              Back
            </button>
            <button 
              className="btn-primary" 
              style={{ flex: 1 }}
              disabled={!isOptionSelected || saving}
              onClick={handleNext}
            >
              {saving ? "Saving..." : currentIndex === total - 1 ? "Finish Check" : "Next Question"}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

// -------------------------------------------------------------------
// SCREEN 8: AI TEETH SCAN
// -------------------------------------------------------------------
function ScanScreen({ user, setCurrentScreen, lastAssessmentScore, setSelectedResult, showToast }) {
  const [selectedImage, setSelectedImage] = useState(null);
  const [imageFile, setImageFile] = useState(null);
  const [analyzing, setAnalyzing] = useState(false);
  const fileInputRef = useRef(null);

  const handleFileChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      setImageFile(file);
      setSelectedImage(URL.createObjectURL(file));
    }
  };

  const triggerFileSelect = () => {
    if (fileInputRef.current) {
      fileInputRef.current.click();
    }
  };

  const triggerCameraSelect = () => {
    if (fileInputRef.current) {
      fileInputRef.current.setAttribute("capture", "environment");
      fileInputRef.current.click();
    }
  };

  const handleAnalyze = async () => {
    if (!imageFile) {
      showToast("Please capture or choose a teeth photo first", "warning");
      return;
    }

    setAnalyzing(true);

    const formData = new FormData();
    formData.append("user_id", user.userId);
    formData.append("image", imageFile);

    try {
      const res = await fetch(`${API_BASE}scan_analysis.php`, {
        method: 'POST',
        body: formData
      });
      const data = await res.json();

      if (data.success) {
        const aiScore = data.data.overall_score;
        const confidence = data.data.confidence;
        const prediction = data.data.prediction;

        const combinedScore = Math.round((lastAssessmentScore * 0.4) + (aiScore * 0.6));
        const combinedRisk = combinedScore >= 70 ? "High" : combinedScore >= 40 ? "Moderate" : "Low";

        const formattedResult = {
          result_id: data.data.result_id,
          score: combinedScore,
          risk_level: combinedRisk,
          result_type: "final",
          demographic: aiScore,
          socioeconomic: lastAssessmentScore,
          dietary: combinedScore,
          hygiene: Math.round(confidence > 1 ? confidence : confidence * 100),
          dentalHistory: 100,
          confidence: confidence,
          prediction: prediction,
          recommendations: data.data.recommendations || [],
          scan_image: data.data.image_path ? `${API_BASE}${data.data.image_path}` : selectedImage
        };

        setTimeout(() => {
          setAnalyzing(false);
          setSelectedResult(formattedResult);
          setCurrentScreen('result_detail');
          showToast("AI teeth analysis complete!");
        }, 3000);

      } else {
        setAnalyzing(false);
        showToast(data.message || "AI Analysis failed. Make sure Flask API is running.", "warning");
      }
    } catch (err) {
      setAnalyzing(false);
      showToast("Connection to Flask API failed", "warning");
    }
  };

  return (
    <div className="screen-container animate-slide-in">
      {analyzing && (
        <div className="analysis-overlay">
          <div className="scanner-circle"></div>
          <h2>AI Dental Scanner</h2>
          <p>Processing image to calculate cavity probability scores and diagnose tooth decay... Please wait 10-20 seconds.</p>
        </div>
      )}

      <div className="header-bar">
        <div className="btn-icon-header" onClick={() => setCurrentScreen('home')}>
          <ChevronLeft size={18} />
        </div>
        <h2>Teeth Caries Scan</h2>
        <div style={{ width: '40px' }} />
      </div>

      <input 
        type="file" 
        accept="image/*" 
        style={{ display: 'none' }} 
        ref={fileInputRef} 
        onChange={handleFileChange}
      />

      <div className="scan-wrapper">
        <div className="scan-card">
          <div className="scan-area" onClick={triggerFileSelect}>
            {selectedImage ? (
              <img src={selectedImage} alt="Teeth preview" className="scan-preview-img" />
            ) : (
              <div className="scan-placeholder">
                <Camera size={48} />
                <h3>Upload Teeth Image</h3>
                <p>Click here or drop your photo to review analysis</p>
              </div>
            )}
          </div>

          <div className="scan-actions-grid">
            <div className="scan-action-button" onClick={triggerCameraSelect}>
              <Camera size={20} color="var(--primary)" />
              <h4>Take Camera Photo</h4>
            </div>
            <div className="scan-action-button" onClick={triggerFileSelect}>
              <Globe size={20} color="var(--accent-teal)" />
              <h4>Browse Files</h4>
            </div>
          </div>

          <button 
            className="btn-primary" 
            disabled={!selectedImage || analyzing}
            onClick={handleAnalyze}
          >
            Analyse teeth with AI model
          </button>
        </div>
      </div>
    </div>
  );
}

// -------------------------------------------------------------------
// SCREEN 9: RESULT DETAILS SCREEN (DUAL COLUMN DESKTOP)
// -------------------------------------------------------------------
function ResultDetailScreen({ result, setCurrentScreen, user }) {
  if (!result) return null;

  const [downloading, setDownloading] = useState(false);

  const getRiskClass = (level) => {
    if (!level) return 'low';
    const lvl = level.toLowerCase();
    if (lvl.includes('high')) return 'high';
    if (lvl.includes('mod')) return 'moderate';
    return 'low';
  };

  const handleDentistSearch = () => {
    window.open("https://www.google.com/maps/search/dentist/", "_blank");
  };

  const toBase64 = async (url) => {
    try {
      const response = await fetch(url);
      const blob = await response.blob();
      return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onloadend = () => resolve(reader.result);
        reader.onerror = reject;
        reader.readAsDataURL(blob);
      });
    } catch (e) {
      console.error("Failed to convert image to base64", e);
      return null;
    }
  };

  const handleDownload = async () => {
    if (downloading) return;
    setDownloading(true);

    try {
      let base64Image = null;
      if (result.scan_image) {
        base64Image = await toBase64(result.scan_image);
      }

      const isFinal = result.result_type === 'final' || result.result_type === 'scan';
      const reportDate = new Date().toLocaleDateString('en-US', {
        month: 'long',
        day: 'numeric',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });

      const recommendationsHtml = result.recommendations && result.recommendations.length > 0
        ? result.recommendations.map(rec => `<li>${rec}</li>`).join('')
        : `<li>Brush your teeth at least twice daily with fluoride toothpaste.</li>
           <li>Floss between your teeth every day to remove plaque.</li>
           <li>Reduce sugary foods and soft drinks in your diet.</li>
           <li>Visit your dentist for a check-up every 6 months.</li>`;

      const riskColor = result.risk_level.toLowerCase() === 'high' ? '#ef4444' : result.risk_level.toLowerCase() === 'moderate' ? '#f59e0b' : '#10b981';

      const htmlContent = `
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>AICaries Diagnostic Report - ${user ? user.name : 'Patient'}</title>
  <style>
    body {
      font-family: sans-serif;
      background-color: #f3f4f6;
      color: #1f2937;
      margin: 0;
      padding: 40px 20px;
      display: flex;
      justify-content: center;
    }
    .report-card {
      background: white;
      max-width: 700px;
      width: 100%;
      border-radius: 16px;
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
      padding: 40px;
      box-sizing: border-box;
    }
    .header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      border-bottom: 2px solid #e5e7eb;
      padding-bottom: 20px;
      margin-bottom: 30px;
    }
    .header h1 {
      font-size: 1.8rem;
      margin: 0;
      color: #6366f1;
      font-weight: 800;
    }
    .header .date {
      font-size: 0.9rem;
      color: #6b7280;
    }
    .patient-info {
      background: #f5f3ff;
      border-left: 4px solid #6366f1;
      padding: 16px;
      border-radius: 0 8px 8px 0;
      margin-bottom: 30px;
    }
    .patient-info p {
      margin: 4px 0;
      font-size: 0.95rem;
    }
    .score-container {
      display: flex;
      align-items: center;
      gap: 30px;
      background: #fafafa;
      border: 1px solid #e5e7eb;
      border-radius: 12px;
      padding: 24px;
      margin-bottom: 30px;
    }
    .score-circle {
      width: 100px;
      height: 100px;
      border-radius: 50%;
      background: conic-gradient(${riskColor} ${result.score}%, #e5e7eb 0);
      display: flex;
      justify-content: center;
      align-items: center;
      position: relative;
    }
    .score-circle::after {
      content: '';
      position: absolute;
      width: 80px;
      height: 80px;
      border-radius: 50%;
      background: white;
    }
    .score-value {
      position: absolute;
      font-size: 1.8rem;
      font-weight: 800;
      z-index: 1;
      color: #111827;
    }
    .score-details h2 {
      margin: 0 0 6px 0;
      font-size: 1.3rem;
      color: #111827;
    }
    .badge {
      display: inline-block;
      padding: 6px 16px;
      border-radius: 20px;
      font-size: 0.85rem;
      font-weight: 700;
      text-transform: uppercase;
      color: white;
      background-color: ${riskColor};
    }
    .breakdown {
      margin-bottom: 30px;
    }
    .breakdown h3 {
      font-size: 1.1rem;
      margin-bottom: 16px;
      border-bottom: 1px solid #e5e7eb;
      padding-bottom: 8px;
      color: #374151;
    }
    .breakdown-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;
    }
    .breakdown-label {
      font-size: 0.95rem;
      color: #4b5563;
    }
    .breakdown-bar-container {
      width: 200px;
      background: #e5e7eb;
      height: 8px;
      border-radius: 4px;
      overflow: hidden;
      margin: 0 16px;
      flex-grow: 1;
    }
    .breakdown-bar-fill {
      height: 100%;
      background: #6366f1;
      border-radius: 4px;
    }
    .breakdown-val {
      width: 40px;
      text-align: right;
      font-weight: 700;
      font-size: 0.95rem;
    }
    .scan-image-container {
      text-align: center;
      margin-bottom: 30px;
      background: #fafafa;
      border: 1px solid #e5e7eb;
      border-radius: 12px;
      padding: 24px;
    }
    .scan-image-container h3 {
      margin-top: 0;
      font-size: 1.1rem;
      color: #374151;
      margin-bottom: 16px;
      text-align: left;
      border-bottom: 1px solid #e5e7eb;
      padding-bottom: 8px;
    }
    .scan-img {
      max-width: 100%;
      max-height: 350px;
      border-radius: 8px;
      border: 1px solid #d1d5db;
      box-shadow: 0 2px 8px rgba(0,0,0,0.05);
    }
    .recs {
      background: #fafafa;
      border: 1px solid #e5e7eb;
      border-radius: 12px;
      padding: 24px;
      margin-bottom: 30px;
    }
    .recs h3 {
      margin-top: 0;
      color: #6366f1;
      margin-bottom: 16px;
    }
    .recs ul {
      margin: 0;
      padding-left: 20px;
    }
    .recs li {
      margin-bottom: 10px;
      font-size: 0.95rem;
      line-height: 1.5;
    }
    .actions {
      display: flex;
      justify-content: center;
      margin-top: 40px;
    }
    .btn-print {
      background-color: #6366f1;
      color: white;
      border: none;
      padding: 12px 30px;
      font-size: 1rem;
      font-weight: 700;
      border-radius: 8px;
      cursor: pointer;
      box-shadow: 0 4px 6px rgba(99, 102, 241, 0.2);
    }
    @media print {
      body {
        background-color: white;
        padding: 0;
      }
      .report-card {
        box-shadow: none;
        padding: 0;
      }
      .actions {
        display: none;
      }
    }
  </style>
</head>
<body>
  <div class="report-card">
    <div class="header">
      <h1>AICaries Diagnostic Report</h1>
      <div class="date">${reportDate}</div>
    </div>

    <div class="patient-info">
      <p><strong>Patient Name:</strong> ${user ? user.name : 'N/A'}</p>
      <p><strong>Email Address:</strong> ${user ? user.email : 'N/A'}</p>
      <p><strong>Diagnostic Mode:</strong> ${isFinal ? 'AI Scanner + Assessment' : 'Dental Risk Assessment'}</p>
    </div>

    <div class="score-container">
      <div class="score-circle">
        <span class="score-value">${result.score}%</span>
      </div>
      <div class="score-details">
        <h2>Risk Score</h2>
        <span class="badge">${result.risk_level} Risk</span>
      </div>
    </div>

    <div class="breakdown">
      <h3>Diagnostic Breakdown</h3>
      <div class="breakdown-row">
        <span class="breakdown-label">Demographic Risk Rating</span>
        <div class="breakdown-bar-container">
          <div class="breakdown-bar-fill" style="width: ${result.demographic}%"></div>
        </div>
        <span class="breakdown-val">${result.demographic}%</span>
      </div>
      <div class="breakdown-row">
        <span class="breakdown-label">Socioeconomic Influence</span>
        <div class="breakdown-bar-container">
          <div class="breakdown-bar-fill" style="width: ${result.socioeconomic}%"></div>
        </div>
        <span class="breakdown-val">${result.socioeconomic}%</span>
      </div>
      <div class="breakdown-row">
        <span class="breakdown-label">Sugary Dietary Load</span>
        <div class="breakdown-bar-container">
          <div class="breakdown-bar-fill" style="width: ${result.dietary}%"></div>
        </div>
        <span class="breakdown-val">${result.dietary}%</span>
      </div>
      <div class="breakdown-row">
        <span class="breakdown-label">Hygiene Practices Score</span>
        <div class="breakdown-bar-container">
          <div class="breakdown-bar-fill" style="width: ${result.hygiene}%"></div>
        </div>
        <span class="breakdown-val">${result.hygiene}%</span>
      </div>
      <div class="breakdown-row">
        <span class="breakdown-label">Dental History Assessment</span>
        <div class="breakdown-bar-container">
          <div class="breakdown-bar-fill" style="width: ${result.dentalHistory}%"></div>
        </div>
        <span class="breakdown-val">${result.dentalHistory}%</span>
      </div>
    </div>

    ${isFinal && result.prediction ? `
    <div class="breakdown">
      <h3>AI Teeth Scan Analysis</h3>
      <div class="breakdown-row">
        <span class="breakdown-label">AI Model Prediction</span>
        <span class="breakdown-val" style="color: ${result.prediction === 'caries' ? '#ef4444' : '#10b981'}; font-size: 1rem; width: auto; font-weight: 800;">
          ${result.prediction === 'caries' ? '⚠️ Caries Detected' : '✅ No Caries Detected'}
        </span>
      </div>
      <div class="breakdown-row">
        <span class="breakdown-label">AI Confidence Level</span>
        <span class="breakdown-val">${result.confidence ? Math.round(result.confidence * 100) : 0}%</span>
      </div>
    </div>
    ` : ''}

    ${base64Image ? `
    <div class="scan-image-container">
      <h3>Scanned Teeth Photo</h3>
      <img src="${base64Image}" alt="Teeth Scan" class="scan-img" />
    </div>
    ` : ''}

    <div class="recs">
      <h3>Personalized Recommendations</h3>
      <ul>
        ${recommendationsHtml}
      </ul>
    </div>

    <div class="actions">
      <button class="btn-print" onclick="window.print()">Print / Save as PDF</button>
    </div>
  </div>
</body>
</html>
      `;

      const blob = new Blob([htmlContent], { type: 'text/html' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `AICaries_Diagnostic_Report_${new Date().toISOString().slice(0, 10)}.html`;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      URL.revokeObjectURL(url);
    } catch (e) {
      console.error(e);
    } finally {
      setDownloading(false);
    }
  };

  return (
    <div className="screen-container animate-slide-in">
      <div className="header-bar">
        <div className="btn-icon-header" onClick={() => setCurrentScreen('home')}>
          <ChevronLeft size={18} />
        </div>
        <h2>Diagnostic Report</h2>
        <div 
          className="btn-icon-header" 
          onClick={handleDownload} 
          title={downloading ? "Generating..." : "Download Report"}
          style={{ cursor: 'pointer', opacity: downloading ? 0.5 : 1 }}
        >
          {downloading ? (
            <div className="spinner" style={{ width: '16px', height: '16px', border: '2px solid var(--text-dark)', borderTopColor: 'transparent', borderRadius: '50%', animation: 'spin 0.8s linear infinite' }}></div>
          ) : (
            <Download size={18} />
          )}
        </div>
      </div>

      <div className="result-wrapper">
        {/* Left Column: Big score badge */}
        <div className="result-score-card">
          <span style={{ fontSize: '0.82rem', color: 'var(--text-secondary)', fontWeight: '700', textTransform: 'uppercase', marginBottom: '16px' }}>
            OVERALL RATING
          </span>
          <div 
            className="circular-progress" 
            style={{ 
              width: '180px',
              height: '180px',
              '--percent': result.score,
              '--fill-color': `var(--risk-${getRiskClass(result.risk_level)})`
            }}
          >
            <div className="circular-progress-text">
              <span className="value" style={{ fontSize: '3rem' }}>{result.score}</span>
              <span className="label" style={{ fontSize: '0.8rem' }}>% Score</span>
            </div>
          </div>

          <h3 style={{ fontSize: '1.4rem', fontWeight: '800', marginTop: '20px' }}>
            {result.result_type === 'final' ? 'AI Combined Report' : 'Assessment Result'}
          </h3>
          
          <span className={`badge-risk ${getRiskClass(result.risk_level)}`} style={{ marginTop: '12px' }}>
            {result.risk_level} Risk Level
          </span>

          <button 
            className="btn-primary" 
            style={{ marginTop: '28px', width: '100%', background: 'transparent', color: 'var(--primary)', border: '1.5px solid var(--primary)', boxShadow: 'none' }}
            onClick={handleDentistSearch}
          >
            Locate nearest dentist
          </button>
        </div>

        {/* Right Column: Breakdown scores & recommendations */}
        <div className="result-breakdowns-card">
          <h3 className="breakdown-title" style={{ fontFamily: 'var(--font-heading)', fontSize: '1.15rem', marginBottom: '24px' }}>
            Breakdown Indices
          </h3>
          
          <div className="breakdown-row">
            <div className="breakdown-row-info">
              <span>Demographic Risk Weight</span>
              <span>{result.demographic}%</span>
            </div>
            <div className="progress-bar-container">
              <div className="progress-bar-fill" style={{ width: `${result.demographic}%` }}></div>
            </div>
          </div>

          <div className="breakdown-row">
            <div className="breakdown-row-info">
              <span>Socioeconomic Weight</span>
              <span>{result.socioeconomic}%</span>
            </div>
            <div className="progress-bar-container">
              <div className="progress-bar-fill" style={{ width: `${result.socioeconomic}%` }}></div>
            </div>
          </div>

          <div className="breakdown-row">
            <div className="breakdown-row-info">
              <span>Sugars & Diet Index</span>
              <span>{result.dietary}%</span>
            </div>
            <div className="progress-bar-container">
              <div className="progress-bar-fill" style={{ width: `${result.dietary}%` }}></div>
            </div>
          </div>

          <div className="breakdown-row">
            <div className="breakdown-row-info">
              <span>Hygiene Habits Index</span>
              <span>{result.hygiene}%</span>
            </div>
            <div className="progress-bar-container">
              <div className="progress-bar-fill" style={{ width: `${result.hygiene}%` }}></div>
            </div>
          </div>

          <div className="breakdown-row" style={{ marginBottom: '32px' }}>
            <div className="breakdown-row-info">
              <span>Tooth Pain & Caries History</span>
              <span>{result.dentalHistory}%</span>
            </div>
            <div className="progress-bar-container">
              <div className="progress-bar-fill" style={{ width: `${result.dentalHistory}%` }}></div>
            </div>
          </div>

          {result.result_type === 'final' && result.confidence !== null && (
            <div style={{ padding: '16px 20px', borderRadius: '16px', backgroundColor: 'var(--bg-app)', border: '1px solid var(--border-color)', display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '28px' }}>
              <div>
                <span style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', display: 'block', fontWeight: '700' }}>AI SYSTEM PREDICTION</span>
                <span style={{ fontSize: '1rem', fontWeight: '800', textTransform: 'capitalize' }}>
                  {result.prediction === 'caries' ? '🚨 Decay Detected' : '✅ No Caries Detected'}
                </span>
              </div>
              <div style={{ textAlign: 'right' }}>
                <span style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', display: 'block', fontWeight: '700' }}>ACCURACY</span>
                <span className="button-text" style={{ fontSize: '1rem', fontWeight: '800', color: 'var(--primary)' }}>{Math.round(result.confidence > 1 ? result.confidence : result.confidence * 100)}%</span>
              </div>
            </div>
          )}

          {/* Scanned Teeth Photo Section */}
          {result.scan_image && (
            <div style={{ marginTop: '24px', padding: '20px', display: 'flex', flexDirection: 'column', alignItems: 'center', backgroundColor: 'var(--bg-app)', border: '1px solid var(--border-color)', borderRadius: '16px', marginBottom: '28px' }}>
              <h3 className="breakdown-title" style={{ width: '100%', textAlign: 'left', marginBottom: '16px', fontSize: '1.05rem', fontWeight: '700' }}>Scanned Teeth Photo</h3>
              <img 
                src={result.scan_image} 
                alt="Teeth Scan" 
                style={{ 
                  maxWidth: '100%', 
                  maxHeight: '280px', 
                  borderRadius: '12px', 
                  border: '1px solid var(--border-color)',
                  boxShadow: '0 4px 12px rgba(0, 0, 0, 0.05)'
                }} 
              />
            </div>
          )}

          <h3 className="breakdown-title" style={{ display: 'flex', alignItems: 'center', gap: '8px', fontFamily: 'var(--font-heading)', fontSize: '1.15rem', marginTop: '24px' }}>
            <Sparkles size={18} color="var(--primary)" />
            <span>Oral Health Recommendations</span>
          </h3>

          <div className="recs-list">
            {result.recommendations && result.recommendations.length > 0 ? (
              result.recommendations.map((rec, index) => (
                <div key={index} className="rec-item">
                  <Smile size={16} color="var(--primary)" style={{ flexShrink: 0, marginTop: '2px' }} />
                  <p>{rec}</p>
                </div>
              ))
            ) : (
              <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>Teeth indicators are completely normal.</p>
            )}
          </div>
        </div>
      </div>

      <div style={{ display: 'flex', justifyContent: 'center', marginTop: '32px' }}>
        <button className="btn-primary" style={{ minWidth: '200px' }} onClick={() => setCurrentScreen('home')}>
          Back to Dashboard
        </button>
      </div>
    </div>
  );
}

// -------------------------------------------------------------------
// SCREEN 10: HISTORY LIST SCREEN
// -------------------------------------------------------------------
function HistoryScreen({ user, setCurrentScreen, setSelectedResult, showToast }) {
  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchHistory = async () => {
    try {
      const res = await fetch(`${API_BASE}get_results.php`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ user_id: user.userId })
      });
      const data = await res.json();
      setLoading(false);
      if (data.success) {
        setResults(data.data || []);
      }
    } catch (err) {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchHistory();
  }, [user]);

  const handleDelete = async (e, id) => {
    e.stopPropagation();
    if (!confirm("Are you sure you want to delete this report?")) return;

    try {
      const res = await fetch(`${API_BASE}delete_results.php`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ result_id: id })
      });
      const data = await res.json();
      if (data.success) {
        setResults(prev => prev.filter(item => item.id !== id));
        showToast("Report deleted successfully");
      } else {
        showToast(data.message || "Failed to delete report", "warning");
      }
    } catch (err) {
      showToast("Server connection error during deletion", "warning");
    }
  };

  const handleItemClick = async (item) => {
    try {
      const res = await fetch(`${API_BASE}get_result_detail.php`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ result_id: item.id })
      });
      const data = await res.json();
      if (data.success) {
        const detail = data.data;
        const answers = typeof detail.answers === 'string' 
          ? JSON.parse(detail.answers) 
          : detail.answers;

        const formattedResult = {
          result_id: detail.id,
          score: detail.overall_score,
          risk_level: detail.risk_level,
          result_type: detail.result_type,
          demographic: detail.demographic_score || detail.overall_score,
          socioeconomic: detail.socioeconomic_score || detail.overall_score,
          dietary: detail.dietary_score || detail.overall_score,
          hygiene: detail.hygiene_score || (answers && answers.confidence ? Math.round(answers.confidence > 1 ? answers.confidence : answers.confidence * 100) : 0),
          dentalHistory: detail.dental_history_score || 100,
          confidence: answers && answers.confidence ? answers.confidence : null,
          prediction: answers && answers.prediction ? answers.prediction : null,
          recommendations: detail.recommendations || [],
          scan_image: answers && answers.image ? `${API_BASE}${answers.image}` : null
        };

        setSelectedResult(formattedResult);
        setCurrentScreen('result_detail');
      } else {
        showToast(data.message || "Failed to load report details", "warning");
      }
    } catch (err) {
      showToast("Server connection error", "warning");
    }
  };

  const formatDate = (dateStr) => {
    if (!dateStr) return '';
    try {
      const date = new Date(dateStr);
      return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
    } catch {
      return dateStr;
    }
  };

  const getRiskClass = (level) => {
    if (!level) return 'low';
    const lvl = level.toLowerCase();
    if (lvl.includes('high')) return 'high';
    if (lvl.includes('mod')) return 'moderate';
    return 'low';
  };

  return (
    <div className="screen-container animate-slide-in">
      <div className="header-bar">
        <h2>Report History</h2>
        <div style={{ width: '40px' }} />
      </div>

      <div className="history-list-wrapper">
        {loading ? (
          <div style={{ display: 'flex', justifyContent: 'center', padding: '60px' }} className="spinner">
            <Activity size={32} color="var(--primary)" />
          </div>
        ) : results.length === 0 ? (
          <div className="history-empty">
            <History size={48} />
            <h3>No Records Logged</h3>
            <p>Your previous scan reports and questionnaire diagnostics will appear here.</p>
          </div>
        ) : (
          <div className="history-list">
            {results.map((item) => (
              <div key={item.id} className="history-item" onClick={() => handleItemClick(item)}>
                <div className="history-item-info">
                  <span className="history-item-date">{formatDate(item.created_at)}</span>
                  <span className="history-item-type">
                    {item.result_type === 'scan' ? 'AI Photo Scanning' : 'Diagnostic Checkup'}
                  </span>
                  <span className={`badge-risk ${getRiskClass(item.risk_level)}`} style={{ padding: '3px 8px', fontSize: '0.68rem', marginTop: '6px' }}>
                    {item.risk_level}
                  </span>
                </div>
                <div className="history-item-score-badge">
                  <div style={{ textAlign: 'right' }}>
                    <span style={{ fontSize: '0.68rem', color: 'var(--text-muted)', display: 'block' }}>SCORE</span>
                    <span className="history-item-score" style={{ color: `var(--risk-${getRiskClass(item.risk_level)})` }}>{item.overall_score}%</span>
                  </div>
                  <div className="btn-delete-history" onClick={(e) => handleDelete(e, item.id)}>
                    <Trash2 size={16} />
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

// -------------------------------------------------------------------
// SCREEN 11: MORE / HEALTH TIPS SCREEN
// -------------------------------------------------------------------
const healthTips = [
  {
    title: "Brushing Techniques",
    desc: "Hold your toothbrush at a 45-degree angle to your gums. Use gentle, circular strokes rather than back-and-forth sawing movements. Brush inner, outer, and chewing surfaces.",
    category: "Hygiene"
  },
  {
    title: "Sugary Snacks Warning",
    desc: "Plaque bacteria consume sugar to produce lactic acid, which dissolves tooth enamel. Restrict sweets to meal times and drink water immediately afterward.",
    category: "Diet"
  },
  {
    title: "Daily Flossing Power",
    desc: "Brushing misses up to 40% of tooth surfaces (between teeth). Use dental floss or interdental brushes at least once daily to prevent interproximal cavities.",
    category: "Hygiene"
  },
  {
    title: "Fluoride Importance",
    desc: "Fluoride remineralizes weak spots in tooth enamel, reversing early stages of decay. Always use a toothpaste containing fluoride and drink fluoridated tap water.",
    category: "General"
  },
  {
    title: "Regular Dentist Checks",
    desc: "Early dental caries can be reverse-remineralized or treated without drilling. Visit a clinic every 6 months for professional scale/clean and examination.",
    category: "Prevention"
  }
];

function TipsScreen() {
  return (
    <div className="screen-container animate-slide-in">
      <div className="header-bar">
        <h2>Dental Health Advice</h2>
        <div style={{ width: '40px' }} />
      </div>

      <div style={{ padding: '0 12px', display: 'flex', flexDirection: 'column', gap: '18px', maxWidth: '800px', margin: '0 auto', width: '100%' }}>
        {healthTips.map((tip, idx) => (
          <div key={idx} className="rec-item" style={{ flexDirection: 'column', gap: '8px', padding: '20px', backgroundColor: 'var(--bg-card)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', width: '100%', alignItems: 'center' }}>
              <h3 style={{ fontSize: '1.05rem', fontWeight: '750', fontFamily: 'var(--font-heading)', color: 'var(--primary)' }}>
                {tip.title}
              </h3>
              <span style={{ fontSize: '0.68rem', fontWeight: '700', textTransform: 'uppercase', padding: '4px 8px', borderRadius: '8px', backgroundColor: 'var(--primary-glow)', color: 'var(--primary)' }}>
                {tip.category}
              </span>
            </div>
            <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', lineHeight: 1.45 }}>{tip.desc}</p>
          </div>
        ))}
      </div>
    </div>
  );
}

// -------------------------------------------------------------------
// SCREEN 12: USER PROFILE SCREEN
// -------------------------------------------------------------------
function ProfileScreen({ user, setUser, handleLogout, setCurrentScreen, showToast }) {
  const [name, setName] = useState(user.name);
  const [email, setEmail] = useState(user.email);
  const [updating, setUpdating] = useState(false);

  const handleUpdate = async (e) => {
    e.preventDefault();
    if (!name || !email) {
      showToast("Fields cannot be empty", "warning");
      return;
    }

    setUpdating(true);
    try {
      const res = await fetch(`${API_BASE}update_profile.php`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          user_id: user.userId,
          name: name,
          email: email
        })
      });
      const data = await res.json();
      setUpdating(false);

      if (data.success) {
        const updatedUser = { ...user, name: name, email: email };
        localStorage.setItem('app_user', JSON.stringify(updatedUser));
        setUser(updatedUser);
        showToast("Profile details updated!");
      } else {
        showToast(data.message || "Failed to update profile", "warning");
      }
    } catch (err) {
      setUpdating(false);
      showToast("Server connection failure", "warning");
    }
  };

  return (
    <div className="screen-container animate-slide-in">
      <div className="header-bar">
        <h2>My Settings</h2>
        <div className="btn-icon-header" onClick={handleLogout} title="Sign Out">
          <LogOut size={16} />
        </div>
      </div>

      <div className="profile-wrapper">
        <div className="profile-card">
          <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', marginBottom: '32px' }}>
            <div style={{ width: '90px', height: '90px', borderRadius: '50%', backgroundColor: 'var(--primary-glow)', color: 'var(--primary)', display: 'flex', justifyContent: 'center', alignItems: 'center', marginBottom: '16px', border: '2px solid var(--primary)' }}>
              <img src={toothAvatar} alt="user avatar" style={{ width: '100%', height: '100%', borderRadius: '50%', objectFit: 'cover' }} />
            </div>
            <h2 style={{ fontSize: '1.4rem', fontFamily: 'var(--font-heading)', fontWeight: '800' }}>{user.name}</h2>
            <span style={{ fontSize: '0.82rem', color: 'var(--text-muted)' }}>{user.email}</span>
          </div>

          <form onSubmit={handleUpdate}>
            <div className="form-group">
              <label>Full Name</label>
              <input 
                type="text" 
                className="input-field" 
                value={name}
                onChange={(e) => setName(e.target.value)}
              />
            </div>
            <div className="form-group" style={{ marginBottom: '28px' }}>
              <label>Email Address</label>
              <input 
                type="email" 
                className="input-field" 
                value={email}
                onChange={(e) => setEmail(e.target.value)}
              />
            </div>

            <button type="submit" className="btn-primary" style={{ width: '100%' }} disabled={updating}>
              {updating ? "Saving Details..." : "Save Profile Details"}
            </button>
          </form>

          <button 
            className="btn-primary" 
            style={{ marginTop: '16px', width: '100%', background: 'transparent', color: 'var(--risk-high)', border: '1.5px solid var(--risk-high)', boxShadow: 'none' }}
            onClick={handleLogout}
          >
            Sign Out of Account
          </button>
        </div>
      </div>
    </div>
  );
}

// -------------------------------------------------------------------
// SCREEN 13: GROQ LLM CHATBOT SCREEN
// -------------------------------------------------------------------
function ChatScreen({ user, setCurrentScreen, showToast }) {
  const [messages, setMessages] = useState([
    { role: 'model', text: "Hello! I am your AICaries assistant. Ask me anything about cavity symptoms, gum care, diet recommendations, or model results." }
  ]);
  const [inputVal, setInputVal] = useState('');
  const [sending, setSending] = useState(false);
  const scrollRef = useRef(null);

  useEffect(() => {
    if (scrollRef.current) {
      scrollRef.current.scrollTop = scrollRef.current.scrollHeight;
    }
  }, [messages]);

  const handleSend = async (e) => {
    e.preventDefault();
    if (!inputVal.trim()) return;

    const userMsg = { role: 'user', text: inputVal.trim() };
    setMessages(prev => [...prev, userMsg]);
    setInputVal('');
    setSending(true);

    const chatHistory = messages.slice(-10);

    try {
      const res = await fetch(`${API_BASE}chat.php`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          message: userMsg.text,
          history: chatHistory
        })
      });
      const data = await res.json();
      setSending(false);

      if (data.success && data.data && data.data.reply) {
        setMessages(prev => [...prev, { role: 'model', text: data.data.reply }]);
      } else {
        setMessages(prev => [...prev, { role: 'model', text: "Sorry, I am having trouble connecting to the dental chatbot right now. Please try again." }]);
      }
    } catch (err) {
      setSending(false);
      setMessages(prev => [...prev, { role: 'model', text: "Network connection error. Ensure local server cURL is working." }]);
    }
  };

  return (
    <div className="screen-container animate-slide-in" style={{ height: '100%', paddingBottom: 0 }}>
      <div className="header-bar">
        <h2>Dental AI Bot</h2>
        <div style={{ width: '40px' }} />
      </div>

      <div className="chat-container">
        <div className="chat-messages-area" ref={scrollRef}>
          {messages.map((msg, idx) => (
            <div key={idx} className={`chat-bubble ${msg.role === 'model' ? 'bot' : 'user'}`}>
              {msg.text}
            </div>
          ))}
          {sending && (
            <div className="chat-bubble bot" style={{ display: 'flex', gap: '6px', alignItems: 'center' }}>
              <div className="spinner"><Activity size={12} /></div>
              <span>Toothie is typing...</span>
            </div>
          )}
        </div>

        <form className="chat-input-bar" onSubmit={handleSend}>
          <input 
            type="text" 
            className="chat-input-field" 
            placeholder="Ask about cavities, plaque, brushing, or checkups..." 
            value={inputVal}
            onChange={(e) => setInputVal(e.target.value)}
            disabled={sending}
          />
          <button type="submit" className="chat-btn-send" disabled={!inputVal.trim() || sending}>
            <Send size={18} />
          </button>
        </form>
      </div>
    </div>
  );
}

export default App;
