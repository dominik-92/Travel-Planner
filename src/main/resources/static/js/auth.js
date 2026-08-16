const AUTH_API = "/api/auth";
const USER_API = "/api/user";

const tabLogin = document.getElementById("tab-login");
const tabRegister = document.getElementById("tab-register");
const loginForm = document.getElementById("login-form");
const registerForm = document.getElementById("register-form");
const authError = document.getElementById("auth-error");
const languageSwitch = document.getElementById("language-switch");

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
    showError(I18n.t("auth.fillAllFields"));
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
      showError(data.error || I18n.t("auth.loginFailed"));
      return;
    }

    localStorage.setItem("token", data.token);
    localStorage.setItem("username", data.username);
    localStorage.setItem("language", data.language);
    window.location.href = "index.html";
  } catch {
    showError(I18n.t("auth.networkError"));
  }
}

async function handleRegister(event) {
  event.preventDefault();
  hideError();

  const username = document.getElementById("register-username").value.trim();
  const email = document.getElementById("register-email").value.trim();
  const password = document.getElementById("register-password").value;

  if (!username || !email || !password) {
    showError(I18n.t("auth.fillAllFields"));
    return;
  }

  try {
    const response = await fetch(`${AUTH_API}/register`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        username,
        email,
        password,
        language: I18n.getLanguage(),
      }),
    });

    const data = await response.json();

    if (!response.ok) {
      showError(data.error || I18n.t("auth.registerFailed"));
      return;
    }

    localStorage.setItem("token", data.token);
    localStorage.setItem("username", data.username);
    localStorage.setItem("language", data.language);
    window.location.href = "index.html";
  } catch {
    showError(I18n.t("auth.networkError"));
  }
}

function setupPasswordToggles() {
  document.querySelectorAll(".password-toggle").forEach((btn) => {
    btn.addEventListener("click", () => {
      const input = document.getElementById(btn.dataset.toggleTarget);
      if (!input) return;
      const reveal = input.type === "password";
      input.type = reveal ? "text" : "password";
      const titleKey = reveal ? "auth.hidePassword" : "auth.showPassword";
      btn.setAttribute("title", I18n.t(titleKey));
      btn.setAttribute("aria-label", I18n.t(titleKey));
    });
  });
}

if (languageSwitch) {
  languageSwitch.querySelectorAll(".lang-btn").forEach((btn) => {
    btn.addEventListener("click", async () => {
      await I18n.setLanguage(btn.dataset.lang);
      languageSwitch.querySelectorAll(".lang-btn").forEach((b) => {
        b.classList.toggle("active", b === btn);
      });
      I18n.applyLanguageToPage();
    });
  });
}

tabLogin.addEventListener("click", switchToLogin);
tabRegister.addEventListener("click", switchToRegister);
loginForm.addEventListener("submit", handleLogin);
registerForm.addEventListener("submit", handleRegister);

(async function () {
  try {
    Theme.init();
  } catch {
    // ignore theme failures
  }

  if (localStorage.getItem("token")) {
    window.location.href = "index.html";
    return;
  }

  if (!localStorage.getItem("language")) {
    const detected = I18n.detectBrowserLanguage();
    I18n.lang = detected;
  }
  await I18n.init();

  if (languageSwitch) {
    const lang = I18n.getLanguage();
    languageSwitch.querySelectorAll(".lang-btn").forEach((btn) => {
      btn.classList.toggle("active", btn.dataset.lang === lang);
    });
  }

  setupPasswordToggles();
})();
