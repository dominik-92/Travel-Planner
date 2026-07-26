const AUTH_API = "http://localhost:8080/api/auth";

const tabLogin = document.getElementById("tab-login");
const tabRegister = document.getElementById("tab-register");
const loginForm = document.getElementById("login-form");
const registerForm = document.getElementById("register-form");
const authError = document.getElementById("auth-error");

function showError(message) {
  authError.textContent = message;
  authError.classList.remove("hidden");
}

function hideError() {
  authError.classList.add("hidden");
}

function switchToLogin() {
  tabLogin.classList.add("active");
  tabRegister.classList.remove("active");
  loginForm.classList.remove("hidden");
  registerForm.classList.add("hidden");
  hideError();
}

function switchToRegister() {
  tabRegister.classList.add("active");
  tabLogin.classList.remove("active");
  registerForm.classList.remove("hidden");
  loginForm.classList.add("hidden");
  hideError();
}

async function handleLogin(event) {
  event.preventDefault();
  hideError();

  const username = document.getElementById("login-username").value.trim();
  const password = document.getElementById("login-password").value;

  if (!username || !password) {
    showError("Please fill in all fields");
    return;
  }

  try {
    const response = await fetch(`${AUTH_API}/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ username, password }),
    });

    const data = await response.json();

    if (!response.ok) {
      showError(data.error || "Login failed");
      return;
    }

    localStorage.setItem("token", data.token);
    localStorage.setItem("username", data.username);
    window.location.href = "index.html";
  } catch {
    showError("Network error. Is the server running?");
  }
}

async function handleRegister(event) {
  event.preventDefault();
  hideError();

  const username = document.getElementById("register-username").value.trim();
  const email = document.getElementById("register-email").value.trim();
  const password = document.getElementById("register-password").value;

  if (!username || !email || !password) {
    showError("Please fill in all fields");
    return;
  }

  try {
    const response = await fetch(`${AUTH_API}/register`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ username, email, password }),
    });

    const data = await response.json();

    if (!response.ok) {
      showError(data.error || "Registration failed");
      return;
    }

    localStorage.setItem("token", data.token);
    localStorage.setItem("username", data.username);
    window.location.href = "index.html";
  } catch {
    showError("Network error. Is the server running?");
  }
}

tabLogin.addEventListener("click", switchToLogin);
tabRegister.addEventListener("click", switchToRegister);
loginForm.addEventListener("submit", handleLogin);
registerForm.addEventListener("submit", handleRegister);

if (localStorage.getItem("token")) {
  window.location.href = "index.html";
}
