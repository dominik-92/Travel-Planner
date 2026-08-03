const SUPPORTED_LANGUAGES = ["pl", "en", "es"];
const DEFAULT_LANGUAGE = "en";

const I18n = {
  lang: DEFAULT_LANGUAGE,
  translations: {},

  getLanguage() {
    return this.lang;
  },

  async init() {
    const stored = localStorage.getItem("language");
    if (stored && SUPPORTED_LANGUAGES.includes(stored)) {
      this.lang = stored;
    }
    await this.loadTranslations();
    this.translatePage();
    this.updateHtmlLang();
  },

  async loadTranslations() {
    try {
      const response = await fetch(`js/i18n/${this.lang}.json`, { cache: "no-cache" });
      if (response.ok) {
        this.translations = await response.json();
      }
    } catch {
      this.translations = {};
    }
  },

  t(key, params) {
    let text = this.translations[key];
    if (text === undefined) {
      return key;
    }
    if (params) {
      Object.keys(params).forEach((k) => {
        text = text.replace(`{${k}}`, params[k]);
      });
    }
    return text;
  },

  async setLanguage(lang) {
    if (!SUPPORTED_LANGUAGES.includes(lang) || lang === this.lang) return;
    this.lang = lang;
    localStorage.setItem("language", lang);
    await this.loadTranslations();
    this.translatePage();
    this.updateHtmlLang();
    if (typeof onLanguageChanged === "function") {
      await onLanguageChanged(lang);
    }
  },

  translatePage() {
    document.querySelectorAll("[data-i18n]").forEach((el) => {
      el.textContent = this.t(el.getAttribute("data-i18n"));
    });
    document.querySelectorAll("[data-i18n-placeholder]").forEach((el) => {
      el.setAttribute("placeholder", this.t(el.getAttribute("data-i18n-placeholder")));
    });
    document.querySelectorAll("[data-i18n-value]").forEach((el) => {
      el.setAttribute("value", this.t(el.getAttribute("data-i18n-value")));
    });
  },

  updateHtmlLang() {
    document.documentElement.lang = this.lang;
  },

  detectBrowserLanguage() {
    const browser = (navigator.language || "").slice(0, 2).toLowerCase();
    return SUPPORTED_LANGUAGES.includes(browser) ? browser : DEFAULT_LANGUAGE;
  },

  applyLanguageToPage() {
    this.translatePage();
    this.updateHtmlLang();
  },
};
