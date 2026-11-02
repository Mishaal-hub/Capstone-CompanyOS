import { useEffect, useState } from "react";
import { Routes, Route, Navigate, useNavigate } from "react-router-dom";
import { api } from "./api";
import Sidebar from "./components/Sidebar";
import Login from "./Login";

import Dashboard from "./pages/Dashboard";
import Tasks from "./pages/Tasks";
import Projects from "./pages/Projects";
import MATE from "./pages/MATE";
import Goals from "./pages/Goals";
import Team from "./pages/Team";
import Growth from "./pages/Growth";
import Activity from "./pages/Activity";
import Settings from "./pages/Settings";
import Company from "./pages/Company";
import Calendar from "./pages/Calendar";
import Notifications from "./pages/Notifications";

import "./App.css";

export default function App() {
  const [loggedIn, setLoggedIn] = useState(false);
  const [username, setUsername] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem("companyos_token");
    const currentUser = localStorage.getItem("companyos_current_user");

    if (token && currentUser) {
      setUsername(currentUser);
      setLoggedIn(true);
    }

    setLoading(false);
  }, []);

  function handleLogin(user) {
    setUsername(user);
    setLoggedIn(true);
  }

  function handleLogout() {
    localStorage.removeItem("companyos_token");
    localStorage.removeItem("companyos_current_user");

    setUsername("");
    setLoggedIn(false);
  }

  if (loading) {
    return (
      <div className="flex items-center justify-center h-screen">
        <div className="animate-spin w-8 h-8 border-4 border-blue-600 border-t-transparent rounded-full" />
      </div>
    );
  }

  if (!loggedIn) {
    return <Login onLogin={handleLogin} />;
  }

  return (
    <div className="app">
      <Sidebar username={username} onLogout={handleLogout} />

      <main className="ml-60 lg:ml-0">
        <Routes>

          {/* Dashboard */}
          <Route
            path="/"
            element={<Dashboard username={username} />}
          />

          {/* Tasks */}
          <Route
            path="/tasks"
            element={<Tasks username={username} />}
          />

          {/* Projects */}
          <Route
            path="/projects"
            element={<Projects username={username} />}
          />

          {/* MATE */}
          <Route
            path="/mate"
            element={<MATE username={username} />}
          />

          {/* Goals */}
          <Route
            path="/goals"
            element={<Goals username={username} />}
          />

          {/* Team */}
          <Route
            path="/team"
            element={<Team username={username} />}
          />

          {/* Growth */}
          <Route
            path="/growth"
            element={<Growth username={username} />}
          />

          {/* Activity */}
          <Route
            path="/activity"
            element={<Activity username={username} />}
          />

          {/* Calendar */}
          <Route
            path="/calendar"
            element={<Calendar username={username} />}
          />

          {/* Settings */}
          <Route
            path="/settings"
            element={<Settings username={username} />}
          />

          {/* Notifications */}
          <Route
            path="/notifications"
            element={<Notifications username={username} />}
          />

          {/* Company */}
          <Route
            path="/companies"
            element={<Company username={username} />}
          />

          {/* Unknown page */}
          <Route
            path="*"
            element={<Navigate to="/" />}
          />

        </Routes>
      </main>
    </div>
  );
}