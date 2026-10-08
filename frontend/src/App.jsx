import { useEffect, useState } from "react";
import { Routes, Route, Navigate } from "react-router-dom";
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
            <Route path="/" element={<Dashboard username={username} />} />
            <Route path="/tasks" element={<Tasks username={username} />} />
            <Route path="/projects" element={<Projects username={username} />} />
            <Route path="/mate" element={<MATE username={username} />} />
            <Route path="/goals" element={<Goals username={username} />} />
            <Route path="/team" element={<Team username={username} />} />
            <Route path="/growth" element={<Growth username={username} />} />
            <Route path="/activity" element={<Activity username={username} />} />
            <Route path="/settings" element={<Settings username={username} />} />
            <Route path="/calendar" element={<div className="space-y-6"><h1 className="text-2xl font-bold text-gray-900">Calendar</h1><p className="text-gray-500">Coming soon.</p></div>} />
            <Route path="/companies" element={<Company username={username} />} />
            <Route path="*" element={<Navigate to="/" />} />
        </Routes>
        </main>
      </div>
    )
  }
