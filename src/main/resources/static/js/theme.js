const Theme = {
  getStored() {
    return localStorage.getItem("theme") || "system";
  },

  resolve() {
    const stored = this.getStored();
    if (stored === "dark" || stored === "light") return stored;
    return window.matchMedia("(prefers-color-scheme: dark)").matches
      ? "dark"
      : "light";
  },

  apply() {
    document.documentElement.setAttribute("data-theme", this.resolve());
  },

  toggle() {
    const next = this.resolve() === "dark" ? "light" : "dark";
    localStorage.setItem("theme", next);
    this.apply();
  },

  set(value) {
    const normalized = ["light", "dark", "system"].includes(value) ? value : "system";
    localStorage.setItem("theme", normalized);
    this.apply();
  },

  init() {
    this.apply();

    const media = window.matchMedia("(prefers-color-scheme: dark)");
    const onChange = () => {
      if (this.getStored() === "system") this.apply();
    };
    if (media.addEventListener) media.addEventListener("change", onChange);
    else if (media.addListener) media.addListener(onChange);

    document.querySelectorAll("[data-theme-toggle]").forEach((btn) => {
      btn.addEventListener("click", () => this.toggle());
    });
  },
};
