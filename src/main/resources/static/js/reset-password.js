const AUTH_API = "/api/auth";

const resetForm = document.getElementById("reset-form");
const messageEl = document.getElementById("reset-message");
const languageSwitch = document.getElementById("language-switch");

function getResetToken() {
  return new URLSearchParams(window.location.search).get("token") || "";
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

async function handleSubmit(event) {
  event.preventDefault();

  const newPassword = document.getElementById("new-password").value;
  const confirmPassword = document.getElementById("confirm-password").value;

  if (newPassword !== confirmPassword) {
    showMessage(I18n.t("account.passwordMismatch"));
    return;
  }

  try {
    const response = await fetch(`${AUTH_API}/reset-password`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ token: getResetToken(), newPassword }),
    });
    const data = await response.json().catch(() => ({}));

    if (!response.ok) {
      showMessage(data.error || I18n.t("reset.invalidToken"));
      return;
    }

    resetForm.classList.add("hidden");
    showMessage(I18n.t("reset.success"), "success");
  } catch {
    showMessage(I18n.t("auth.networkError"));
  }
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

resetForm.addEventListener("submit", handleSubmit);

(async function () {
  try {
    Theme.init();
  } catch {
    // ignore theme failures
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

  if (!getResetToken()) {
    resetForm.classList.add("hidden");
    showMessage(I18n.t("reset.invalidToken"));
  }
})();
