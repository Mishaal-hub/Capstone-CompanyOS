import { useState, useEffect, useRef, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import {
  api,
  verifyGoogleOtp,
  resendGoogleOtp,
  verifyLocalOtp,
  resendLocalOtp,
  forgotPassword,
  verifyResetOtp,
  resetPassword,
  resendResetOtp,
} from "./api";
import "./Login.css";

// ─────────────────────────────────────────────────────────────────────────────
// Constants
// ─────────────────────────────────────────────────────────────────────────────
const GOOGLE_CLIENT_ID = import.meta.env.VITE_GOOGLE_CLIENT_ID || "";

const OTP_LENGTH    = 6;
const RESEND_COOLDOWN = 60; // seconds

// Password strength scoring
function scorePassword(pw) {
  if (!pw) return 0;
  let score = 0;
  if (pw.length >= 8)  score++;
  if (pw.length >= 12) score++;
  if (/[A-Z]/.test(pw)) score++;
  if (/[a-z]/.test(pw)) score++;
  if (/\d/.test(pw))    score++;
  if (/[^A-Za-z0-9]/.test(pw)) score++;
  return score; // 0-6
}
function strengthLabel(s) {
  if (s <= 1) return { label: "Weak",      cls: "strength--weak"   };
  if (s <= 3) return { label: "Fair",      cls: "strength--fair"   };
  if (s <= 4) return { label: "Good",      cls: "strength--good"   };
  return        { label: "Strong",    cls: "strength--strong" };
}

// ─────────────────────────────────────────────────────────────────────────────
// Resend countdown hook
// ─────────────────────────────────────────────────────────────────────────────
function useResendCooldown(initial = 0) {
  const [seconds, setSeconds] = useState(initial);
  const ref = useRef(null);

  const start = useCallback((s = RESEND_COOLDOWN) => {
    setSeconds(s);
  }, []);

  useEffect(() => {
    if (seconds <= 0) { clearInterval(ref.current); return; }
    ref.current = setInterval(() => setSeconds(p => (p <= 1 ? (clearInterval(ref.current), 0) : p - 1)), 1000);
    return () => clearInterval(ref.current);
  }, [seconds]);

  return [seconds, start];
}

// ─────────────────────────────────────────────────────────────────────────────
// OTP input — stable refs so typing never loses focus
// ─────────────────────────────────────────────────────────────────────────────
function OtpBoxes({ value, onChange, disabled }) {
  const refs = useRef([]);
  const digits = Array.from({ length: OTP_LENGTH }, (_, i) => value[i] || "");

  const focusBox = (index) => {
    refs.current[index]?.focus();
    refs.current[index]?.select();
  };

  const handleChange = (i, e) => {
    const pasted = e.target.value.replace(/\D/g, "");
    if (!pasted) {
      const next = digits.slice();
      next[i] = "";
      onChange(next.join(""));
      return;
    }

    const next = digits.slice();
    pasted.split("").slice(0, OTP_LENGTH - i).forEach((digit, offset) => {
      next[i + offset] = digit;
    });
    onChange(next.join(""));

    const nextIndex = Math.min(i + pasted.length, OTP_LENGTH - 1);
    focusBox(nextIndex);
  };

  const handleKeyDown = (i, e) => {
    if (e.key === "Backspace") {
      e.preventDefault();
      const next = digits.slice();
      if (next[i]) {
        next[i] = "";
        onChange(next.join(""));
      } else if (i > 0) {
        next[i - 1] = "";
        onChange(next.join(""));
        focusBox(i - 1);
      }
    } else if (e.key === "ArrowLeft" && i > 0) {
      e.preventDefault();
      focusBox(i - 1);
    } else if (e.key === "ArrowRight" && i < OTP_LENGTH - 1) {
      e.preventDefault();
      focusBox(i + 1);
    }
  };

  const handlePaste = (e) => {
    e.preventDefault();
    const pasted = e.clipboardData.getData("text").replace(/\D/g, "").slice(0, OTP_LENGTH);
    onChange(pasted);
    if (pasted.length) focusBox(Math.min(pasted.length, OTP_LENGTH - 1));
  };

  return (
    <div className="otp-boxes" role="group" aria-label="One-time password input">
      {digits.map((digit, i) => (
        <input
          key={i}
          ref={(el) => { refs.current[i] = el; }}
          className={`otp-box${digit ? " otp-box--filled" : ""}`}
          type="text"
          inputMode="numeric"
          autoComplete={i === 0 ? "one-time-code" : "off"}
          maxLength={OTP_LENGTH}
          value={digit}
          onChange={(e) => handleChange(i, e)}
          onKeyDown={(e) => handleKeyDown(i, e)}
          onPaste={handlePaste}
          disabled={disabled}
          aria-label={`Digit ${i + 1}`}
        />
      ))}
    </div>
  );
}

// ─────────────────────────────────────────────────────────────────────────────
// Stable password field — defined outside Login so React preserves focus
// ─────────────────────────────────────────────────────────────────────────────
function PasswordField({ id, label, value, onChange, show, onToggle, placeholder }) {
  const strength = scorePassword(value);
  const { label: strengthText, cls } = strengthLabel(strength);

  return (
    <div className="auth-field">
      <label htmlFor={id}>{label}</label>
      <div className="auth-input-wrap">
        <input
          id={id}
          type={show ? "text" : "password"}
          value={value}
          onChange={(e) => onChange(e.target.value)}
          placeholder={placeholder || ""}
          autoComplete={id.includes("confirm") ? "new-password" : id === "login-pw" ? "current-password" : "new-password"}
        />
        <button
          type="button"
          className="auth-eye"
          onMouseDown={(e) => e.preventDefault()}
          onClick={onToggle}
          aria-label={show ? "Hide password" : "Show password"}
        >
          {show ? "🙈" : "👁"}
        </button>
      </div>
      {value && id !== "confirm-pw" && id !== "confirm-new" && (
        <div className="strength-bar">
          <div className={`strength-track ${cls}`} style={{ width: `${Math.max((strength / 6) * 100, 8)}%` }} />
          <span className={`strength-label ${cls}`}>{strengthText}</span>
        </div>
      )}
    </div>
  );
}

// ─────────────────────────────────────────────────────────────────────────────
// Main component
// ─────────────────────────────────────────────────────────────────────────────
export default function Login({ onLogin }) {
  const navigate = useNavigate();

  // ── screen state ──────────────────────────────────────────────────────────
  // "login" | "register" | "verify-email" | "forgot" | "verify-reset" | "reset-password" | "reset-success"
  const [screen, setScreen] = useState("login");

  // ── form fields ───────────────────────────────────────────────────────────
  const [email,       setEmail]       = useState("");
  const [password,    setPassword]    = useState("");
  const [confirmPw,   setConfirmPw]   = useState("");
  const [fullName,    setFullName]    = useState("");
  const [otp,         setOtp]         = useState("");
  const [verifiedResetCode, setVerifiedResetCode] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmNew,  setConfirmNew]  = useState("");

  // ── show/hide password ───────────────────────────────────────────────────
  const [showPw,      setShowPw]      = useState(false);
  const [showNewPw,   setShowNewPw]   = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);

  // ── feedback ──────────────────────────────────────────────────────────────
  const [error,   setError]   = useState("");
  const [success, setSuccess] = useState("");
  const [loading, setLoading] = useState(false);

  // ── Google-callback pending ───────────────────────────────────────────────
  const [googleSession, setGoogleSession] = useState(""); // sessionId from URL
  const [googleLoading, setGoogleLoading] = useState(false);

  // ── Resend cooldowns ──────────────────────────────────────────────────────
  const [verifyCooldown, startVerifyCooldown] = useResendCooldown(0);
  const [resetCooldown,  startResetCooldown]  = useResendCooldown(0);

  // ── shared email stored across screens ───────────────────────────────────
  // (e.g. the email used for registration, carried into verify-email)
  const pendingEmail = useRef("");

  // ─────────────────────────────────────────────────────────────────────────
  // Google callback: read google_code from URL on mount
  // ─────────────────────────────────────────────────────────────────────────
  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const gc = params.get("google_code");
    if (gc) {
      window.history.replaceState({}, "", window.location.pathname);
      setGoogleSession(gc);
      setScreen("verify-google");
    }
  }, []);

  // ─────────────────────────────────────────────────────────────────────────
  // Helpers
  // ─────────────────────────────────────────────────────────────────────────
  const clearFeedback = () => { setError(""); setSuccess(""); };

  const go = (s) => { clearFeedback(); setOtp(""); setScreen(s); };

  const setErr = (msg) => { setError(msg); setSuccess(""); };
  const setOk  = (msg) => { setSuccess(msg); setError(""); };

  // ─────────────────────────────────────────────────────────────────────────
  // Handlers
  // ─────────────────────────────────────────────────────────────────────────

  // ── Login ──────────────────────────────────────────────────────────────
  const handleLogin = async (e) => {
    e.preventDefault();
    clearFeedback();
    if (!email || !password) { setErr("Please enter your email and password."); return; }
    setLoading(true);
    try {
      const data = await api.post("/auth/login", { username: email, password });
      localStorage.setItem("companyos_token",        data.token);
      localStorage.setItem("companyos_current_user", data.username);
      onLogin(data.username);
      navigate("/");
    } catch (err) {
      const msg = err.message || "Login failed.";
      if (msg.toLowerCase().includes("verify your email")) {
        pendingEmail.current = email;
        setErr("Your email is not verified. Enter the code we sent you.");
        go("verify-email");
      } else {
        setErr(msg);
      }
    } finally {
      setLoading(false);
    }
  };

  // ── Register ───────────────────────────────────────────────────────────
  const handleRegister = async (e) => {
    e.preventDefault();
    clearFeedback();
    if (!email || !password || !confirmPw) { setErr("Please fill in all fields."); return; }
    if (password !== confirmPw) { setErr("Passwords do not match."); return; }
    if (scorePassword(password) < 3) {
      setErr("Password is too weak. Use at least 8 characters with uppercase, lowercase, numbers and symbols.");
      return;
    }
    setLoading(true);
    try {
      await api.post("/auth/register", {
        username:         email,
        password,
        fullName:         fullName.trim() || email.split("@")[0],
        organizationName: (fullName.trim() || email.split("@")[0]) + "'s Workspace",
      });
      pendingEmail.current = email;
      setOk("Account created! We sent a verification code to " + email + ".");
      setOtp("");
      startVerifyCooldown(RESEND_COOLDOWN);
      go("verify-email");
    } catch (err) {
      setErr(err.message || "Registration failed.");
    } finally {
      setLoading(false);
    }
  };

  // ── Verify email OTP ───────────────────────────────────────────────────
  const handleVerifyEmail = async (e) => {
    e.preventDefault();
    clearFeedback();
    if (otp.length !== OTP_LENGTH) { setErr("Please enter the complete 6-digit code."); return; }
    setLoading(true);
    try {
      await verifyLocalOtp(pendingEmail.current, otp);
      setOk("Email verified! You can now sign in.");
      setOtp("");
      setTimeout(() => go("login"), 1400);
    } catch (err) {
      const msg = err.message || "Verification failed.";
      if (msg.includes("expired"))        setErr("Your code has expired. Click Resend to get a new one.");
      else if (msg.includes("attempt"))   setErr(msg);
      else                                setErr("Incorrect code — please try again.");
    } finally {
      setLoading(false);
    }
  };

  const handleResendVerify = async () => {
    if (verifyCooldown > 0) return;
    clearFeedback();
    setLoading(true);
    try {
      await resendLocalOtp(pendingEmail.current);
      setOk("A new code has been sent to " + pendingEmail.current + ".");
      setOtp("");
      startVerifyCooldown(RESEND_COOLDOWN);
    } catch (err) {
      setErr(err.message || "Failed to resend code.");
    } finally {
      setLoading(false);
    }
  };

  // ── Google OTP ─────────────────────────────────────────────────────────
  const handleVerifyGoogle = async (e) => {
    e.preventDefault();
    clearFeedback();
    if (otp.length !== OTP_LENGTH) { setErr("Please enter the complete 6-digit code."); return; }
    setLoading(true);
    try {
      const data = await verifyGoogleOtp(googleSession, otp);
      localStorage.setItem("companyos_token",        data.token);
      localStorage.setItem("companyos_current_user", data.username);
      onLogin(data.username);
      navigate("/");
    } catch (err) {
      const msg = err.message || "Verification failed.";
      if (msg.includes("expired"))       setErr("Your code has expired. Click Resend to get a new one.");
      else if (msg.includes("Too many")) setErr("Too many attempts. Please sign in again.");
      else                               setErr("Incorrect code — please try again.");
    } finally {
      setLoading(false);
    }
  };

  const handleResendGoogle = async () => {
    if (verifyCooldown > 0) return;
    clearFeedback();
    setLoading(true);
    try {
      await resendGoogleOtp(googleSession);
      setOk("A new code has been sent.");
      setOtp("");
      startVerifyCooldown(RESEND_COOLDOWN);
    } catch (err) {
      setErr(err.message || "Failed to resend code.");
    } finally {
      setLoading(false);
    }
  };

  // ── Forgot password ────────────────────────────────────────────────────
  const handleForgotPassword = async (e) => {
    e.preventDefault();
    clearFeedback();
    if (!email) { setErr("Please enter your email address."); return; }
    setLoading(true);
    try {
      await forgotPassword(email);
      pendingEmail.current = email;
      setOk("If that email is registered, a code is on its way.");
      setOtp("");
      startResetCooldown(RESEND_COOLDOWN);
      go("verify-reset");
    } catch (err) {
      setErr(err.message || "Request failed. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  // ── Verify reset OTP ───────────────────────────────────────────────────
  const handleVerifyReset = async (e) => {
    e.preventDefault();
    clearFeedback();
    if (otp.length !== OTP_LENGTH) { setErr("Please enter the complete 6-digit code."); return; }
    setLoading(true);
    try {
      await verifyResetOtp(pendingEmail.current, otp);
      setVerifiedResetCode(otp);
      setOk("Code verified. Choose a new password.");
      setOtp("");
      go("reset-password");
    } catch (err) {
      const msg = err.message || "Verification failed.";
      if (msg.includes("expired")) setErr("Your code has expired. Click Resend to get a new one.");
      else if (msg.includes("attempt")) setErr(msg);
      else setErr("Incorrect code — please try again.");
    } finally {
      setLoading(false);
    }
  };

  const handleResendReset = async () => {
    if (resetCooldown > 0) return;
    clearFeedback();
    setLoading(true);
    try {
      await resendResetOtp(pendingEmail.current);
      setOk("A new code has been sent to " + pendingEmail.current + ".");
      setOtp("");
      startResetCooldown(RESEND_COOLDOWN);
    } catch (err) {
      setErr(err.message || "Failed to resend code.");
    } finally {
      setLoading(false);
    }
  };

  // ── Reset password ─────────────────────────────────────────────────────
  const handleResetPassword = async (e) => {
    e.preventDefault();
    clearFeedback();
    if (!newPassword || !confirmNew) { setErr("Please fill in both password fields."); return; }
    if (newPassword !== confirmNew)  { setErr("Passwords do not match."); return; }
    if (scorePassword(newPassword) < 3) {
      setErr("Password is too weak. Use at least 8 characters with uppercase, lowercase, numbers and symbols.");
      return;
    }
    setLoading(true);
    try {
      await resetPassword(pendingEmail.current, verifiedResetCode, newPassword);
      go("reset-success");
    } catch (err) {
      setErr(err.message || "Reset failed. Please start again.");
    } finally {
      setLoading(false);
    }
  };

  // ─────────────────────────────────────────────────────────────────────────
  // Shared UI atoms
  // ─────────────────────────────────────────────────────────────────────────

  const Feedback = () => (
    <>
      {error   && <p className="auth-msg auth-msg--error"   role="alert">{error}</p>}
      {success && <p className="auth-msg auth-msg--success">{success}</p>}
    </>
  );

  const SubmitBtn = ({ label, loadingLabel }) => (
    <button type="submit" className="auth-btn auth-btn--primary" disabled={loading}>
      {loading ? <><span className="auth-spinner" aria-hidden="true" />{loadingLabel || "Please wait…"}</> : label}
    </button>
  );

  const ResendControl = ({ cooldown, onResend }) => (
    <div className="auth-resend">
      <span className="auth-resend__hint">Didn't receive it?</span>
      {cooldown > 0
        ? <span className="auth-resend__timer">Resend in {cooldown}s</span>
        : <button type="button" className="auth-link" onClick={onResend} disabled={loading}>Resend code</button>}
    </div>
  );


  // ─────────────────────────────────────────────────────────────────────────
  // Left panel branding
  // ─────────────────────────────────────────────────────────────────────────
  const LeftPanel = () => (
    <div className="auth-panel auth-panel--left">
      <div className="auth-panel__inner">
        <div className="auth-panel__logo">◆</div>
        <h2 className="auth-panel__title">CompanyOS</h2>
        <p className="auth-panel__tagline">
          An Adaptive Business Operating System<br />
          powered by MATE Intelligence
        </p>
        <div className="auth-panel__dots" aria-hidden="true">
          <span /><span /><span />
        </div>
      </div>
    </div>
  );

  // ─────────────────────────────────────────────────────────────────────────
  // Screens
  // ─────────────────────────────────────────────────────────────────────────

  // ── LOGIN ──────────────────────────────────────────────────────────────
  if (screen === "login") return (
    <div className="auth-page">
      <div className="auth-card">
        <LeftPanel />
        <div className="auth-form-section">
          <div className="auth-logo-mobile">◆ CompanyOS</div>
          <div className="auth-form-content">
            <h1 className="auth-heading">Welcome back</h1>
            <p className="auth-subheading">Sign in to manage your organisation.</p>

            {GOOGLE_CLIENT_ID && (
              <>
                <button
                  type="button"
                  className={`auth-google-btn${googleLoading ? " auth-google-btn--loading" : ""}`}
                  onClick={() => { setGoogleLoading(true); window.location.href = "http://localhost:8080/api/auth/google"; }}
                  disabled={googleLoading}
                  aria-label="Continue with Google"
                >
                  {googleLoading
                    ? <span className="auth-spinner" aria-hidden="true" />
                    : <GoogleIcon />}
                  <span>{googleLoading ? "Redirecting…" : "Continue with Google"}</span>
                </button>
                <div className="auth-divider"><span>or</span></div>
              </>
            )}

            <form onSubmit={handleLogin} noValidate>
              <div className="auth-field">
                <label htmlFor="login-email">Email address</label>
                <input id="login-email" type="email" value={email}
                  onChange={e => setEmail(e.target.value)} placeholder="you@company.com"
                  autoComplete="email" required />
              </div>

              <PasswordField id="login-pw" label="Password" value={password}
                onChange={setPassword} show={showPw} onToggle={() => setShowPw(p => !p)}
                placeholder="Enter your password" />

              <div className="auth-row">
                <button type="button" className="auth-link auth-link--small"
                  onClick={() => { pendingEmail.current = email; go("forgot"); }}>
                  Forgot password?
                </button>
              </div>

              <Feedback />
              <SubmitBtn label="Sign in" loadingLabel="Signing in…" />
            </form>

            <p className="auth-switch">
              New to CompanyOS?{" "}
              <button type="button" className="auth-link" onClick={() => go("register")}>
                Create an account
              </button>
            </p>
          </div>
        </div>
      </div>
    </div>
  );

  // ── REGISTER ───────────────────────────────────────────────────────────
  if (screen === "register") return (
    <div className="auth-page">
      <div className="auth-card">
        <LeftPanel />
        <div className="auth-form-section">
          <div className="auth-logo-mobile">◆ CompanyOS</div>
          <div className="auth-form-content">
            <h1 className="auth-heading">Create account</h1>
            <p className="auth-subheading">Start your CompanyOS workspace today.</p>

            <form onSubmit={handleRegister} noValidate>
              <div className="auth-field">
                <label htmlFor="reg-name">Full name <span className="auth-optional">(optional)</span></label>
                <input id="reg-name" type="text" value={fullName}
                  onChange={e => setFullName(e.target.value)} placeholder="Your name"
                  autoComplete="name" />
              </div>

              <div className="auth-field">
                <label htmlFor="reg-email">Work email</label>
                <input id="reg-email" type="email" value={email}
                  onChange={e => setEmail(e.target.value)} placeholder="you@company.com"
                  autoComplete="email" required />
              </div>

              <PasswordField id="reg-pw" label="Password" value={password}
                onChange={setPassword} show={showPw} onToggle={() => setShowPw(p => !p)}
                placeholder="Min. 8 characters" />

              <PasswordField id="confirm-pw" label="Confirm password" value={confirmPw}
                onChange={setConfirmPw} show={showConfirm} onToggle={() => setShowConfirm(p => !p)}
                placeholder="Repeat your password" />

              {confirmPw && password !== confirmPw && (
                <p className="auth-field-error">Passwords do not match.</p>
              )}

              <Feedback />
              <SubmitBtn label="Create account" loadingLabel="Creating account…" />
            </form>

            <p className="auth-switch">
              Already have an account?{" "}
              <button type="button" className="auth-link" onClick={() => go("login")}>Sign in</button>
            </p>
          </div>
        </div>
      </div>
    </div>
  );

  // ── VERIFY EMAIL ───────────────────────────────────────────────────────
  if (screen === "verify-email") return (
    <div className="auth-page">
      <div className="auth-card auth-card--narrow">
        <div className="auth-form-section auth-form-section--full">
          <div className="auth-logo-top">◆ CompanyOS</div>
          <div className="auth-form-content">
            <div className="auth-icon-badge">✉</div>
            <h1 className="auth-heading">Check your inbox</h1>
            <p className="auth-subheading">
              We sent a 6-digit verification code to{" "}
              <strong>{pendingEmail.current}</strong>.
            </p>

            <form onSubmit={handleVerifyEmail} noValidate>
              <OtpBoxes value={otp} onChange={setOtp} disabled={loading} />
              <Feedback />
              <SubmitBtn label="Verify email" loadingLabel="Verifying…" />
            </form>

            <ResendControl cooldown={verifyCooldown} onResend={handleResendVerify} />

            <button type="button" className="auth-back" onClick={() => go("login")}>
              ← Back to sign in
            </button>
          </div>
        </div>
      </div>
    </div>
  );

  // ── VERIFY GOOGLE OTP ──────────────────────────────────────────────────
  if (screen === "verify-google") return (
    <div className="auth-page">
      <div className="auth-card auth-card--narrow">
        <div className="auth-form-section auth-form-section--full">
          <div className="auth-logo-top">◆ CompanyOS</div>
          <div className="auth-form-content">
            <div className="auth-icon-badge auth-icon-badge--google">G</div>
            <h1 className="auth-heading">Google sign-in</h1>
            <p className="auth-subheading">
              We sent a verification code to your Google email address to confirm it&apos;s you.
            </p>

            <form onSubmit={handleVerifyGoogle} noValidate>
              <OtpBoxes value={otp} onChange={setOtp} disabled={loading} />
              <Feedback />
              <SubmitBtn label="Verify & sign in" loadingLabel="Verifying…" />
            </form>

            <ResendControl cooldown={verifyCooldown} onResend={handleResendGoogle} />

            <button type="button" className="auth-back" onClick={() => go("login")}>
              ← Back to sign in
            </button>
          </div>
        </div>
      </div>
    </div>
  );

  // ── FORGOT PASSWORD ────────────────────────────────────────────────────
  if (screen === "forgot") return (
    <div className="auth-page">
      <div className="auth-card auth-card--narrow">
        <div className="auth-form-section auth-form-section--full">
          <div className="auth-logo-top">◆ CompanyOS</div>
          <div className="auth-form-content">
            <div className="auth-icon-badge">🔑</div>
            <h1 className="auth-heading">Forgot your password?</h1>
            <p className="auth-subheading">
              Enter your registered email and we&apos;ll send you a reset code.
            </p>

            <form onSubmit={handleForgotPassword} noValidate>
              <div className="auth-field">
                <label htmlFor="forgot-email">Email address</label>
                <input id="forgot-email" type="email" value={email}
                  onChange={e => setEmail(e.target.value)} placeholder="you@company.com"
                  autoComplete="email" required />
              </div>
              <Feedback />
              <SubmitBtn label="Send reset code" loadingLabel="Sending…" />
            </form>

            <button type="button" className="auth-back" onClick={() => go("login")}>
              ← Back to sign in
            </button>
          </div>
        </div>
      </div>
    </div>
  );

  // ── VERIFY RESET OTP ───────────────────────────────────────────────────
  if (screen === "verify-reset") return (
    <div className="auth-page">
      <div className="auth-card auth-card--narrow">
        <div className="auth-form-section auth-form-section--full">
          <div className="auth-logo-top">◆ CompanyOS</div>
          <div className="auth-form-content">
            <div className="auth-icon-badge">🔐</div>
            <h1 className="auth-heading">Enter reset code</h1>
            <p className="auth-subheading">
              We sent a 6-digit code to <strong>{pendingEmail.current}</strong>.
              Enter it below to continue.
            </p>

            <form onSubmit={handleVerifyReset} noValidate>
              <OtpBoxes value={otp} onChange={setOtp} disabled={loading} />
              <Feedback />
              <SubmitBtn label="Verify code" loadingLabel="Verifying…" />
            </form>

            <ResendControl cooldown={resetCooldown} onResend={handleResendReset} />

            <button type="button" className="auth-back" onClick={() => go("forgot")}>
              ← Try a different email
            </button>
          </div>
        </div>
      </div>
    </div>
  );

  // ── RESET PASSWORD ─────────────────────────────────────────────────────
  if (screen === "reset-password") return (
    <div className="auth-page">
      <div className="auth-card auth-card--narrow">
        <div className="auth-form-section auth-form-section--full">
          <div className="auth-logo-top">◆ CompanyOS</div>
          <div className="auth-form-content">
            <div className="auth-icon-badge">🛡</div>
            <h1 className="auth-heading">Set new password</h1>
            <p className="auth-subheading">
              Choose a strong password for your CompanyOS account.
            </p>

            <form onSubmit={handleResetPassword} noValidate>
              <PasswordField id="new-pw" label="New password" value={newPassword}
                onChange={setNewPassword} show={showNewPw} onToggle={() => setShowNewPw(p => !p)}
                placeholder="Min. 8 characters" />

              <PasswordField id="confirm-new" label="Confirm new password" value={confirmNew}
                onChange={setConfirmNew} show={showConfirm} onToggle={() => setShowConfirm(p => !p)}
                placeholder="Repeat new password" />

              {confirmNew && newPassword !== confirmNew && (
                <p className="auth-field-error">Passwords do not match.</p>
              )}

              <Feedback />
              <SubmitBtn label="Reset password" loadingLabel="Resetting…" />
            </form>
          </div>
        </div>
      </div>
    </div>
  );

  // ── RESET SUCCESS ──────────────────────────────────────────────────────
  if (screen === "reset-success") return (
    <div className="auth-page">
      <div className="auth-card auth-card--narrow">
        <div className="auth-form-section auth-form-section--full">
          <div className="auth-logo-top">◆ CompanyOS</div>
          <div className="auth-form-content auth-form-content--center">
            <div className="auth-success-icon">✓</div>
            <h1 className="auth-heading">Password reset</h1>
            <p className="auth-subheading">
              Your password has been updated successfully.
            </p>
            <button type="button" className="auth-btn auth-btn--primary"
              onClick={() => { go("login"); setPassword(""); }}>
              Continue to sign in
            </button>
          </div>
        </div>
      </div>
    </div>
  );

  // Fallback
  return null;
}

// ─────────────────────────────────────────────────────────────────────────────
// Google icon SVG
// ─────────────────────────────────────────────────────────────────────────────
function GoogleIcon() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
      <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="#4285F4"/>
      <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853"/>
      <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" fill="#FBBC05"/>
      <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335"/>
    </svg>
  );
}

