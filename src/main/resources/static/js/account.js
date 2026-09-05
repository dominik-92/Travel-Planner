const USER_API = "/api/user";
const CURRENCY_API_BASE = "/api/currencies";

const messageEl = document.getElementById("account-message");
const profileForm = document.getElementById("profile-form");
const passwordForm = document.getElementById("password-form");
const deleteAccountButton = document.getElementById("delete-account-button");
const saveTravelButton = document.getElementById("save-travel-button");
const languageSwitch = document.getElementById("language-switch");
const languageSelect = document.getElementById("profile-language");
const defaultCurrencySelect = document.getElementById("default-currency");
const usernameDisplay = document.getElementById("username-display");
const logoutButton = document.getElementById("logout-button");

const confirmModal = document.getElementById("confirm-modal");
const confirmMessage = document.getElementById("confirm-message");
const confirmOk = document.getElementById("confirm-ok");
const confirmCancel = document.getElementById("confirm-cancel");

let currencies = [];

function getToken() {
  return localStorage.getItem("token");
}

function checkAuth() {
  if (!getToken()) {
    window.location.href = "login.html";
    return false;
  }
  return true;
}

function showMessage(text, type = "error") {
  messageEl.textContent = text;
  messageEl.classList.remove("hidden");
  if (type === "success") {
    messageEl.style.background = "var(--success-soft)";
    messageEl.style.borderColor = "var(--success)";
    messageEl.style.color = "var(--success)";
  } else {
    messageEl.style.background = "";
    messageEl.style.borderColor = "";
    messageEl.style.color = "";
  }
}

function hideMessage() {
  messageEl.classList.add("hidden");
}

async function fetchJson(url, options) {
  const token = getToken();
  const headers = {
    "Content-Type": "application/json",
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...(options?.headers || {}),
  };
  const response = await fetch(url, { ...options, headers });
  if (response.status === 401 || response.status === 403) {
    logout();
    return null;
  }
  if (!response.ok) {
    const data = await response.json().catch(() => ({}));
    throw new Error(data.error || "Request failed");
  }
  return response.status === 204 ? null : response.json();
}

function logout() {
  localStorage.removeItem("token");
  localStorage.removeItem("username");
  localStorage.removeItem("language");
  window.location.href = "login.html";
}

function storeSession(data) {
  if (data && data.token) {
    localStorage.setItem("token", data.token);
    localStorage.setItem("username", data.username);
    if (data.language) localStorage.setItem("language", data.language);
  }
}

// ---- Pane navigation ------------------------------------------------------

const PANES = ["profile", "appearance", "travel", "security"];

function activePane() {
  const hash = window.location.hash.replace("#", "");
  return PANES.includes(hash) ? hash : "profile";
}

function switchPane(pane) {
  document.querySelectorAll(".settings-nav-item").forEach((btn) => {
    btn.classList.toggle("active", btn.dataset.pane === pane);
  });
  document.querySelectorAll(".settings-pane").forEach((section) => {
    section.classList.toggle("active", section.dataset.pane === pane);
  });
  if (window.location.hash !== `#${pane}`) {
    history.replaceState(null, "", `#${pane}`);
  }
}

// ---- Theme ----------------------------------------------------------------

function renderThemeSegment() {
  const current = Theme.getStored();
  document.querySelectorAll(".settings-segment-btn").forEach((btn) => {
    btn.classList.toggle("active", btn.dataset.themeValue === current);
  });
}

// ---- Language -------------------------------------------------------------

async function persistLanguage(lang) {
  const token = getToken();
  if (!token) return;
  try {
    const response = await fetch(`${USER_API}/language`, {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify({ language: lang }),
    });
    if (response.ok) {
      const data = await response.json();
      localStorage.setItem("token", data.token);
      localStorage.setItem("language", data.language);
    }
  } catch {
    // ignore
  }
}

async function applyLanguage(lang) {
  await I18n.setLanguage(lang);
  I18n.applyLanguageToPage();
  document.querySelectorAll(".lang-btn").forEach((b) => {
    b.classList.toggle("active", b.dataset.lang === lang);
  });
  languageSelect.value = lang;
}

// ---- Profile / currencies -------------------------------------------------

async function loadCurrencies() {
  try {
    currencies = await fetchJson(CURRENCY_API_BASE, { method: "GET" }) || [];
    populateCurrencySelect();
  } catch {
    currencies = [];
  }
}

function populateCurrencySelect() {
  const selected = defaultCurrencySelect.value;
  defaultCurrencySelect.innerHTML = "";
  const lang = I18n.getLanguage();
  currencies.forEach((c) => {
    const option = document.createElement("option");
    option.value = c.code;
    const displayName =
      lang === "pl" && c.namePl
        ? c.namePl
        : lang === "es" && c.nameEs
        ? c.nameEs
        : c.name;
    option.textContent = `${c.code} - ${displayName}`;
    if (c.code === selected || (!selected && c.code === "PLN")) option.selected = true;
    defaultCurrencySelect.append(option);
  });
}

async function loadProfile() {
  try {
    const profile = await fetchJson(`${USER_API}/profile`, { method: "GET" });
    if (!profile) return;
    document.getElementById("profile-username").value = profile.username || "";
    document.getElementById("profile-email").value = profile.email || "";
    languageSelect.value = profile.language || "en";
    defaultCurrencySelect.value = profile.currency || "PLN";
    usernameDisplay.textContent = profile.username || localStorage.getItem("username") || "";
  } catch {
    // ignore; fields stay editable
  }
}

// ---- Handlers -------------------------------------------------------------

async function handleProfileSubmit(event) {
  event.preventDefault();
  hideMessage();

  const email = document.getElementById("profile-email").value.trim();

  try {
    const data = await fetchJson(`${USER_API}/profile`, {
      method: "PUT",
      body: JSON.stringify({ email }),
    });
    storeSession(data);
    showMessage(I18n.t("account.profileSaved"), "success");
  } catch (err) {
    showMessage(err.message);
  }
}

async function handleSaveTravel() {
  hideMessage();
  const currency = defaultCurrencySelect.value;

  try {
    const data = await fetchJson(`${USER_API}/profile`, {
      method: "PUT",
      body: JSON.stringify({ currency }),
    });
    storeSession(data);
    showMessage(I18n.t("account.preferencesSaved"), "success");
  } catch (err) {
    showMessage(err.message);
  }
}

async function handlePasswordSubmit(event) {
  event.preventDefault();
  hideMessage();

  const currentPassword = document.getElementById("current-password").value;
  const newPassword = document.getElementById("new-password").value;
  const confirmPassword = document.getElementById("confirm-password").value;

  if (newPassword !== confirmPassword) {
    showMessage(I18n.t("account.passwordMismatch"));
    return;
  }

  try {
    const data = await fetchJson(`${USER_API}/password`, {
      method: "PUT",
      body: JSON.stringify({ currentPassword, newPassword }),
    });
    storeSession(data);
    passwordForm.reset();
    showMessage(I18n.t("account.passwordChanged"), "success");
  } catch (err) {
    showMessage(err.message);
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

let confirmResolver = null;

function showConfirm(message) {
  return new Promise((resolve) => {
    confirmResolver = resolve;
    confirmMessage.textContent = message;
    confirmModal.classList.add("open");
    document.body.classList.add("modal-open");
  });
}

function closeConfirm(result) {
  confirmModal.classList.remove("open");
  document.body.classList.remove("modal-open");
  if (confirmResolver) confirmResolver(result);
  confirmResolver = null;
}

async function handleDeleteAccount() {
  const ok = await showConfirm(I18n.t("account.deleteConfirm"));
  if (!ok) return;

  try {
    await fetchJson(USER_API, { method: "DELETE" });
    logout();
  } catch (err) {
    showMessage(err.message);
  }
}

// ---- Bindings -------------------------------------------------------------

document.querySelectorAll(".settings-nav-item").forEach((btn) => {
  btn.addEventListener("click", () => switchPane(btn.dataset.pane));
});

document.querySelectorAll(".settings-segment-btn").forEach((btn) => {
  btn.addEventListener("click", () => {
    Theme.set(btn.dataset.themeValue);
    renderThemeSegment();
  });
});

languageSelect.addEventListener("change", async () => {
  await applyLanguage(languageSelect.value);
  await persistLanguage(languageSelect.value);
});

if (languageSwitch) {
  languageSwitch.querySelectorAll(".lang-btn").forEach((btn) => {
    btn.addEventListener("click", async () => {
      await applyLanguage(btn.dataset.lang);
      await persistLanguage(btn.dataset.lang);
    });
  });
}

profileForm.addEventListener("submit", handleProfileSubmit);
passwordForm.addEventListener("submit", handlePasswordSubmit);
saveTravelButton.addEventListener("click", handleSaveTravel);
deleteAccountButton.addEventListener("click", handleDeleteAccount);
logoutButton.addEventListener("click", logout);

confirmOk.addEventListener("click", () => closeConfirm(true));
confirmCancel.addEventListener("click", () => closeConfirm(false));
confirmModal.addEventListener("click", (e) => {
  if (e.target === confirmModal) closeConfirm(false);
});

(async function () {
  if (!checkAuth()) return;

  try {
    Theme.init();
  } catch {
    // ignore theme failures
  }

  await I18n.init();

  switchPane(activePane());
  renderThemeSegment();

  if (languageSwitch) {
    const lang = I18n.getLanguage();
    languageSwitch.querySelectorAll(".lang-btn").forEach((btn) => {
      btn.classList.toggle("active", btn.dataset.lang === lang);
    });
  }

  setupPasswordToggles();
  await Promise.all([loadProfile(), loadCurrencies()]);
})();
