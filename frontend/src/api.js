const API = "http://localhost:8080/api";

function getToken() {
  return localStorage.getItem("companyos_token");
}

async function request(url, options = {}) {
  const headers = {
    "Content-Type": "application/json",
    ...options.headers,
  };
  const token = getToken();
  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }
  const res = await fetch(`${API}${url}`, { ...options, headers });
  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error(err.error || err.message || `Request failed: ${res.status}`);
  }
  return res;
}

async function parseJson(res) {
  if (res.status === 204) return null;
  const text = await res.text();
  return text ? JSON.parse(text) : null;
}

export const api = {
  get: (url) => request(url, { method: "GET" }).then(parseJson),
  post: (url, body) =>
    request(url, { method: "POST", body: JSON.stringify(body) }).then(parseJson),
  put: (url, body) =>
    request(url, { method: "PUT", body: JSON.stringify(body) }).then(parseJson),
  patch: (url, body) =>
    request(url, { method: "PATCH", body: JSON.stringify(body) }).then(parseJson),
  delete: (url) =>
    request(url, { method: "DELETE" }).then(parseJson),
};

export function getDashboardStats() {
  return api.get("/dashboard/overview");
}

export function getTasks(params = {}) {
  const q = new URLSearchParams(params).toString();
  return api.get(`/tasks${q ? "?" + q : ""}`);
}

export function createTask(data) {
  return api.post("/tasks", data);
}

export function updateTask(id, data) {
  return api.put(`/tasks/${id}`, data);
}

export function updateTaskStatus(id, status) {
  return api.patch(`/tasks/${id}/status`, { status });
}

export function deleteTask(id) {
  return api.delete(`/tasks/${id}`);
}

export function getProjects() {
  return api.get("/projects");
}

export function getProject(id) {
  return api.get(`/projects/${id}`);
}

export function createProject(data) {
  return api.post("/projects", data);
}

export function updateProject(id, data) {
  return api.put(`/projects/${id}`, data);
}

export function deleteProject(id) {
  return api.delete(`/projects/${id}`);
}

export function getCompanies() {
  return api.get("/companies");
}

export function getGoals() {
  return api.get("/goals");
}

export function createGoal(data) {
  return api.post("/goals", data);
}

export function updateGoal(id, data) {
  return api.put(`/goals/${id}`, data);
}

export function getActivity() {
  return api.get("/dashboard/overview").then((d) => d.recentActivity || []);
}

export function login(data) {
  return api.post("/auth/login", data);
}

export function register(data) {
  return api.post("/auth/register", data);
}

export function verifyGoogleOtp(code, otp) {
  return api.post("/auth/google/verify-otp", { code, otp });
}

export function resendGoogleOtp(code) {
  return api.post("/auth/google/resend-otp", { code });
}

export function verifyLocalOtp(username, code) {
  return request(`/auth/verify?username=${encodeURIComponent(username)}&code=${encodeURIComponent(code)}`, { method: "POST" })
    .then(r => r.text());
}

export function resendLocalOtp(username) {
  return request(`/auth/resend-verification?username=${encodeURIComponent(username)}`, { method: "POST" })
    .then(r => r.text());
}

export function forgotPassword(email) {
  return request("/auth/forgot-password", {
    method: "POST",
    body: JSON.stringify({ email }),
  }).then(r => r.text());
}

export function verifyResetOtp(email, code) {
  return request("/auth/verify-reset-otp", {
    method: "POST",
    body: JSON.stringify({ email, code }),
  }).then(r => r.text());
}

export function resetPassword(email, code, newPassword) {
  return request("/auth/reset-password", {
    method: "POST",
    body: JSON.stringify({ email, code, newPassword }),
  }).then(r => r.text());
}

export function resendResetOtp(email) {
  return request(`/auth/forgot-password`, {
    method: "POST",
    body: JSON.stringify({ email }),
  }).then(r => r.text());
}
