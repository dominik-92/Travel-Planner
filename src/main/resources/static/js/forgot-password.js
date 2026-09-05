const AUTH_API = "/api/auth";

const forgotForm = document.getElementById("forgot-form");
const messageEl = document.getElementById("forgot-message");
const languageSwitch = document.getElementById("language-switch");

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

async function handleSubmit(event) {
  event.preventDefault();
  hideMessage();

  const email = document.getElementById("forgot-email").value.trim();

  try {
    const response = await fetch(`${AUTH_API}/forgot-password`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email }),
    });
    const data = await response.json().catch(() => ({}));

    if (!response.ok) {
      showMessage(data.error || I18n.t("auth.networkError"));
      return;
    }

    forgotForm.classList.add("hidden");
    showMessage(data.message || I18n.t("forgot.sent"), "success");
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

forgotForm.addEventListener("submit", handleSubmit);

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
})();
