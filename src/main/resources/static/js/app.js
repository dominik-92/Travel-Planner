const API_BASE = "http://localhost:8080/api/trips";
const CURRENCY_API_BASE = "http://localhost:8080/api/currencies";
const COUNTRY_API_BASE = "http://localhost:8080/api/countries";
const USER_API = "http://localhost:8080/api/user";

const ICONS = {
  calendar:
    '<svg class="icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2"/><path d="M16 2v4M8 2v4M3 10h18"/></svg>',
  wallet:
    '<svg class="icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="6" width="20" height="14" rx="2"/><path d="M2 10h20"/></svg>',
};

function getToken() {
  return localStorage.getItem("token");
}

function logout() {
  localStorage.removeItem("token");
  localStorage.removeItem("username");
  localStorage.removeItem("language");
  window.location.href = "login.html";
}

function checkAuth() {
  if (!getToken()) {
    window.location.href = "login.html";
    return false;
  }
  return true;
}

function escapeHtml(value) {
  return String(value ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#39;");
}

// ---- DOM refs -----------------------------------------------------------

const usernameDisplay = document.getElementById("username-display");
const languageSwitch = document.getElementById("language-switch");
const exportButton = document.getElementById("export-data-button");
const logoutButton = document.getElementById("logout-button");

const dashboardView = document.getElementById("dashboard-view");
const detailView = document.getElementById("detail-view");
const newTripButton = document.getElementById("new-trip-button");
const emptyCta = document.getElementById("empty-cta");
const tripsEmpty = document.getElementById("trips-empty");
const tripsList = document.getElementById("trips-list");
const statTrips = document.getElementById("stat-trips");
const statUpcoming = document.getElementById("stat-upcoming");
const statPast = document.getElementById("stat-past");

const tripModal = document.getElementById("trip-modal");
const closeTripModalBtn = document.getElementById("close-trip-modal");
const tripForm = document.getElementById("trip-form");
const resetTripButton = document.getElementById("reset-trip-button");

const backButton = document.getElementById("back-button");
const deleteTripButton = document.getElementById("delete-trip-button");
const activeTripCard = document.getElementById("active-trip-card");

const tabItinerary = document.getElementById("tab-itinerary");
const tabBudget = document.getElementById("tab-budget");
const tabDestination = document.getElementById("tab-destination");
const panelItinerary = document.getElementById("panel-itinerary");
const panelBudget = document.getElementById("panel-budget");
const panelDestination = document.getElementById("panel-destination");

const itineraryForm = document.getElementById("itinerary-form");
const itineraryDay = document.getElementById("itinerary-day");
const itineraryList = document.getElementById("itinerary-list");

const budgetTotal = document.getElementById("budget-total");
const budgetSpent = document.getElementById("budget-spent");
const budgetRemaining = document.getElementById("budget-remaining");
const budgetProgress = document.getElementById("budget-progress");
const budgetPlnEquivalent = document.getElementById("budget-pln-equivalent");

const expenseForm = document.getElementById("expense-form");
const expenseList = document.getElementById("expense-list");

const loadDestinationButton = document.getElementById("load-destination-info");
const saveNotesButton = document.getElementById("save-notes-button");
const destinationWeather = document.getElementById("destination-weather");
const destinationCurrency = document.getElementById("destination-currency");
const destinationTips = document.getElementById("destination-tips");
const destinationNotes = document.getElementById("destination-notes");

const tripCurrencySelect = document.getElementById("trip-currency");
const tripCountryHidden = document.getElementById("trip-country");
const countrySearchInput = document.getElementById("country-search");
const countryDropdown = document.getElementById("country-dropdown");
const countrySelectWrapper = document.getElementById("country-select-wrapper");

const confirmModal = document.getElementById("confirm-modal");
const confirmMessage = document.getElementById("confirm-message");
const confirmOk = document.getElementById("confirm-ok");
const confirmCancel = document.getElementById("confirm-cancel");

const toastContainer = document.getElementById("toast-container");

let state = {
  trips: [],
  activeTripId: null,
  activeTab: "itinerary",
  filter: "all",
  currencies: [],
  countries: [],
};

// ---- API / formatting ----------------------------------------------------

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
    return;
  }
  if (!response.ok) {
    const errorMessage = await response.text();
    throw new Error(errorMessage || "Request failed");
  }
  return response.status === 204 ? null : response.json();
}

async function loadTrips() {
  try {
    state.trips = await fetchJson(API_BASE, { method: "GET" });
  } catch {
    state.trips = [];
  }
}

function getLocale() {
  const lang = I18n.getLanguage();
  if (lang === "pl") return "pl-PL";
  if (lang === "es") return "es-ES";
  return "en-US";
}

function formatCurrency(value, currencyCode = "PLN") {
  try {
    return new Intl.NumberFormat(getLocale(), {
      style: "currency",
      currency: currencyCode,
    }).format(Number(value));
  } catch {
    return new Intl.NumberFormat(getLocale(), {
      style: "decimal",
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(Number(value));
  }
}

function formatDate(date) {
  const dt = new Date(date);
  if (Number.isNaN(dt.getTime())) return I18n.t("date.invalid");
  return dt.toLocaleDateString(getLocale(), {
    year: "numeric",
    month: "short",
    day: "numeric",
  });
}

function getActiveTrip() {
  return state.trips.find((trip) => trip.id === state.activeTripId) || null;
}

function tripSpent(trip) {
  return (trip.expenses || []).reduce((s, e) => s + Number(e.amount), 0);
}

function tripRatio(trip) {
  const budget = Number(trip.budget);
  if (budget <= 0) return 0;
  return Math.min(100, (tripSpent(trip) / budget) * 100);
}

// ---- Toast ---------------------------------------------------------------

function showToast(message, type = "success") {
  const toast = document.createElement("div");
  toast.className = `toast toast-${type}`;
  toast.textContent = message;
  toastContainer.append(toast);
  requestAnimationFrame(() => toast.classList.add("show"));
  setTimeout(() => {
    toast.classList.remove("show");
    setTimeout(() => toast.remove(), 250);
  }, 3200);
}

// ---- View switching ------------------------------------------------------

function showDashboard() {
  state.activeTripId = null;
  dashboardView.classList.remove("hidden");
  detailView.classList.add("hidden");
  window.scrollTo({ top: 0 });
}

function showDetail() {
  dashboardView.classList.add("hidden");
  detailView.classList.remove("hidden");
  window.scrollTo({ top: 0 });
}

function switchTab(name) {
  state.activeTab = name;
  tabItinerary.classList.toggle("active", name === "itinerary");
  tabBudget.classList.toggle("active", name === "budget");
  tabDestination.classList.toggle("active", name === "destination");
  panelItinerary.classList.toggle("hidden", name !== "itinerary");
  panelBudget.classList.toggle("hidden", name !== "budget");
  panelDestination.classList.toggle("hidden", name !== "destination");
}

// ---- Modal ---------------------------------------------------------------

function openTripModal() {
  tripModal.classList.add("open");
  document.body.classList.add("modal-open");
  setTimeout(() => document.getElementById("trip-name").focus(), 50);
}

function closeTripModal() {
  tripModal.classList.remove("open");
  document.body.classList.remove("modal-open");
  resetTripForm();
}

// ---- Confirm -------------------------------------------------------------

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

// ---- Dashboard rendering -------------------------------------------------

function renderStats() {
  const now = new Date();
  statTrips.textContent = state.trips.length;
  statUpcoming.textContent = state.trips.filter(
    (trip) => new Date(trip.startDate) >= now
  ).length;
  statPast.textContent = state.trips.filter(
    (trip) => new Date(trip.endDate) < now
  ).length;
}

function filteredTrips() {
  const now = new Date();
  if (state.filter === "upcoming") {
    return state.trips.filter((trip) => new Date(trip.startDate) >= now);
  }
  if (state.filter === "past") {
    return state.trips.filter((trip) => new Date(trip.endDate) < now);
  }
  return state.trips;
}

function setFilter(filter) {
  state.filter = filter;
  document.querySelectorAll(".stat-card").forEach((el) => {
    el.classList.toggle("active", el.dataset.filter === filter);
  });
  renderTrips();
  if (state.trips.length > 0) {
    document.querySelector(".section-head").scrollIntoView({ behavior: "smooth", block: "start" });
  }
}

function renderTrips() {
  tripsList.innerHTML = "";
  const trips = filteredTrips();
  const hasAnyTrips = state.trips.length > 0;
  const hasTrips = trips.length > 0;

  tripsEmpty.classList.toggle("hidden", hasAnyTrips);
  
  if (!hasTrips) {
    tripsList.classList.add("hidden");
    if (hasAnyTrips) {
      tripsList.classList.remove("hidden");
      const msg = document.createElement("p");
      msg.className = "empty-line";
      msg.textContent = I18n.t("trip.noMatch");
      tripsList.append(msg);
    }
    return;
  }

  tripsList.classList.remove("hidden");

  trips.forEach((trip) => {
    const currency = trip.currency || "PLN";
    const spent = tripSpent(trip);
    const ratio = tripRatio(trip);

    const card = document.createElement("article");
    card.className = "trip-card";
    card.innerHTML = `
      <div class="trip-card-title">
        <h3>${escapeHtml(trip.name)}</h3>
        <p>${escapeHtml(trip.destination)}${trip.country ? ` · ${escapeHtml(trip.country)}` : ""}</p>
      </div>
      <div class="trip-card-meta">
        ${ICONS.calendar}
        <span>${formatDate(trip.startDate)} → ${formatDate(trip.endDate)}</span>
      </div>
      <div class="trip-card-budget">
        <div class="progress">
          <div class="progress-fill${ratio > 100 ? " over" : ""}" style="width:${ratio}%"></div>
        </div>
        <div class="budget-line">
          <span>${escapeHtml(I18n.t("details.spent"))}: <strong>${formatCurrency(spent, currency)}</strong></span>
          <span><strong>${formatCurrency(trip.budget, currency)}</strong></span>
        </div>
      </div>
      <button class="btn btn-primary btn-full trip-open-btn" type="button">${escapeHtml(I18n.t("trip.openTrip"))}</button>
    `;

    const openBtn = card.querySelector(".trip-open-btn");
    openBtn.addEventListener("click", (e) => {
      e.stopPropagation();
      openTripDetails(trip.id);
    });
    card.addEventListener("click", () => openTripDetails(trip.id));
    tripsList.append(card);
  });
}

// ---- Trip detail ---------------------------------------------------------

function openTripDetails(tripId) {
  state.activeTripId = tripId;
  state.activeTab = "itinerary";
  renderTripDetails();
  showDetail();
}

function renderTripDetails() {
  const trip = getActiveTrip();
  if (!trip) return;

  activeTripCard.innerHTML = `
    <h2>${escapeHtml(trip.name)}</h2>
    <div class="hero-sub">${escapeHtml(trip.destination)}${trip.country ? ` · ${escapeHtml(trip.country)}` : ""}</div>
    <div class="detail-hero-meta">
      <span class="hero-meta-item">${ICONS.calendar} ${formatDate(trip.startDate)} → ${formatDate(trip.endDate)}</span>
      <span class="hero-meta-item">${ICONS.wallet} ${formatCurrency(trip.budget, trip.currency || "PLN")}</span>
    </div>
  `;

  destinationNotes.value = trip.destinationNotes || "";
  populateItineraryDayOptions(trip);
  renderItinerary(trip);
  renderExpenses(trip);
  renderBudget(trip);
  renderDestinationInfo(trip);
  switchTab(state.activeTab);
}

function renderItinerary(trip) {
  itineraryList.innerHTML = "";
  if (!trip.itinerary || trip.itinerary.length === 0) {
    itineraryList.append(emptyLine(I18n.t("itinerary.empty")));
    return;
  }

  trip.itinerary
    .sort((a, b) => a.day - b.day || a.time.localeCompare(b.time))
    .forEach((item) => {
      const itemElement = document.createElement("div");
      itemElement.className = "list-item";
      itemElement.innerHTML = `
        <div class="list-item-body">
          <div class="list-item-title">${escapeHtml(item.title)}</div>
          <div class="list-item-sub">${I18n.t("itinerary.dayFormat", { 0: item.day })} · ${escapeHtml(item.time)}${item.description ? ` — ${escapeHtml(item.description)}` : ""}</div>
        </div>
      `;

      const removeButton = document.createElement("button");
      removeButton.textContent = I18n.t("itinerary.remove");
      removeButton.className = "btn btn-ghost";
      removeButton.addEventListener("click", () => removeItineraryItem(trip.id, item.id));

      itemElement.append(removeButton);
      itineraryList.append(itemElement);
    });
}

function populateItineraryDayOptions(trip) {
  itineraryDay.innerHTML = "";
  const start = new Date(trip.startDate);
  const end = new Date(trip.endDate);
  const days = Math.max(1, Math.ceil((end - start) / (1000 * 60 * 60 * 24)) + 1);

  for (let day = 1; day <= days; day += 1) {
    const option = document.createElement("option");
    option.value = String(day);
    option.textContent = I18n.t("itinerary.dayFormat", { 0: day });
    itineraryDay.append(option);
  }
}

function renderExpenses(trip) {
  expenseList.innerHTML = "";
  if (!trip.expenses || trip.expenses.length === 0) {
    expenseList.append(emptyLine(I18n.t("expense.empty")));
    return;
  }

  const currency = trip.currency || "PLN";

  trip.expenses
    .sort((a, b) => new Date(a.addedAt) - new Date(b.addedAt))
    .forEach((expense) => {
      const plnAmount = expense.amount * (expense.rateToPln || 1);
      const plnText =
        currency !== "PLN" ? ` (≈ ${formatCurrency(plnAmount, "PLN")})` : "";

      const itemElement = document.createElement("div");
      itemElement.className = "list-item";
      itemElement.innerHTML = `
        <div class="list-item-body">
          <div class="list-item-title">${escapeHtml(expense.category)} · ${formatCurrency(expense.amount, currency)}</div>
          <div class="list-item-sub">${escapeHtml(expense.description || I18n.t("expense.noDescription"))}${plnText}</div>
        </div>
      `;

      const removeButton = document.createElement("button");
      removeButton.textContent = I18n.t("expense.remove");
      removeButton.className = "btn btn-ghost";
      removeButton.addEventListener("click", () => removeExpense(trip.id, expense.id));

      itemElement.append(removeButton);
      expenseList.append(itemElement);
    });
}

function renderBudget(trip) {
  const currency = trip.currency || "PLN";
  const spentInCurrency = tripSpent(trip);
  const spentInPln = (trip.expenses || []).reduce(
    (total, item) => total + Number(item.amount) * (item.rateToPln || 1),
    0
  );
  const remaining = Math.max(0, Number(trip.budget) - spentInCurrency);
  const ratio = trip.budget > 0 ? Math.min(100, (spentInCurrency / trip.budget) * 100) : 0;
  const over = spentInCurrency > Number(trip.budget);

  budgetTotal.textContent = formatCurrency(trip.budget, currency);
  budgetSpent.textContent = I18n.t("budget.spent", { 0: formatCurrency(spentInCurrency, currency) });
  budgetRemaining.textContent = I18n.t("budget.remaining", { 0: formatCurrency(remaining, currency) });
  budgetProgress.style.width = `${ratio}%`;
  budgetProgress.classList.toggle("over", over);

  budgetPlnEquivalent.textContent =
    currency !== "PLN"
      ? I18n.t("budget.plnSpent", { 0: formatCurrency(spentInPln, "PLN") })
      : "";
}

function renderDestinationInfo(trip) {
  destinationWeather.textContent = trip.destinationInfo?.weather || I18n.t("destination.na");
  destinationCurrency.textContent = trip.destinationInfo?.currency || I18n.t("destination.na");
  destinationTips.textContent = trip.destinationInfo?.tips || I18n.t("destination.na");
}

function emptyLine(text) {
  const el = document.createElement("p");
  el.className = "empty-line";
  el.textContent = text;
  return el;
}

// ---- Actions -------------------------------------------------------------

async function removeItineraryItem(tripId, itemId) {
  try {
    const updatedTrip = await fetchJson(`${API_BASE}/${tripId}/itinerary/${itemId}`, { method: "DELETE" });
    const index = state.trips.findIndex((t) => t.id === tripId);
    if (index !== -1) state.trips[index] = updatedTrip;
    renderTripDetails();
    renderTrips();
    showToast(I18n.t("toast.removed"));
  } catch {
    showToast(I18n.t("toast.error"), "error");
  }
}

async function removeExpense(tripId, expenseId) {
  try {
    const updatedTrip = await fetchJson(`${API_BASE}/${tripId}/expenses/${expenseId}`, { method: "DELETE" });
    const index = state.trips.findIndex((t) => t.id === tripId);
    if (index !== -1) state.trips[index] = updatedTrip;
    renderTripDetails();
    renderTrips();
    showToast(I18n.t("toast.removed"));
  } catch {
    showToast(I18n.t("toast.error"), "error");
  }
}

function resetTripForm() {
  tripForm.reset();
  document.getElementById("budget").value = 0;
  tripCountryHidden.value = "";
  countrySearchInput.value = "";
  countryDropdown.classList.remove("open");
}

async function createTrip(event) {
  event.preventDefault();
  const formData = new FormData(tripForm);
  const name = formData.get("tripName").trim();
  const destination = formData.get("destination").trim();
  const startDate = formData.get("startDate");
  const endDate = formData.get("endDate");
  const budget = Number(formData.get("budget"));
  const currency = tripCurrencySelect.value || "PLN";
  const country = tripCountryHidden.value || "";
  const notes = formData.get("notes").trim();

  if (!name || !destination || !startDate || !endDate) {
    return;
  }

  const trip = {
    name,
    destination,
    startDate,
    endDate,
    budget: Number.isFinite(budget) ? budget : 0,
    currency,
    country,
    notes,
  };

  try {
    const created = await fetchJson(API_BASE, {
      method: "POST",
      body: JSON.stringify(trip),
    });
    state.trips.push(created);
    closeTripModal();
    renderStats();
    renderTrips();
    showToast(I18n.t("toast.tripCreated"));
    openTripDetails(created.id);
  } catch {
    showToast(I18n.t("toast.error"), "error");
  }
}

async function addItineraryItem(event) {
  event.preventDefault();
  const trip = getActiveTrip();
  if (!trip) return;

  const day = Number(itineraryDay.value);
  const time = document.getElementById("itinerary-time").value;
  const title = document.getElementById("itinerary-title").value.trim();
  const description = document.getElementById("itinerary-description").value.trim();

  if (!day || !time || !title) return;

  try {
    const updatedTrip = await fetchJson(`${API_BASE}/${trip.id}/itinerary`, {
      method: "POST",
      body: JSON.stringify({ day, time, title, description }),
    });
    const index = state.trips.findIndex((t) => t.id === trip.id);
    if (index !== -1) state.trips[index] = updatedTrip;
    renderTripDetails();
    renderTrips();
    itineraryForm.reset();
    showToast(I18n.t("toast.itineraryAdded"));
  } catch {
    showToast(I18n.t("toast.error"), "error");
  }
}

async function addExpense(event) {
  event.preventDefault();
  const trip = getActiveTrip();
  if (!trip) return;

  const category = document.getElementById("expense-category").value;
  const amount = Number(document.getElementById("expense-amount").value);
  const description = document.getElementById("expense-description").value.trim();

  if (!category || !Number.isFinite(amount) || amount <= 0) return;

  try {
    const updatedTrip = await fetchJson(`${API_BASE}/${trip.id}/expenses`, {
      method: "POST",
      body: JSON.stringify({ category, amount, description }),
    });
    const index = state.trips.findIndex((t) => t.id === trip.id);
    if (index !== -1) state.trips[index] = updatedTrip;
    renderTripDetails();
    renderTrips();
    expenseForm.reset();
    showToast(I18n.t("toast.expenseAdded"));
  } catch {
    showToast(I18n.t("toast.error"), "error");
  }
}

async function loadDestinationInfo() {
  const trip = getActiveTrip();
  if (!trip) return;

  try {
    const updatedTrip = await fetchJson(`${API_BASE}/${trip.id}/destination-info`, { method: "POST" });
    const index = state.trips.findIndex((t) => t.id === trip.id);
    if (index !== -1) state.trips[index] = updatedTrip;
    renderTripDetails();
    showToast(I18n.t("toast.infoLoaded"));
  } catch {
    showToast(I18n.t("toast.error"), "error");
  }
}

async function saveTravelerNotes() {
  const trip = getActiveTrip();
  if (!trip) return;

  try {
    const updatedTrip = await fetchJson(`${API_BASE}/${trip.id}/notes`, {
      method: "POST",
      body: JSON.stringify({ destinationNotes: destinationNotes.value.trim() }),
    });
    const index = state.trips.findIndex((t) => t.id === trip.id);
    if (index !== -1) state.trips[index] = updatedTrip;
    showToast(I18n.t("toast.notesSaved"));
  } catch {
    showToast(I18n.t("toast.error"), "error");
  }
}

async function deleteActiveTrip() {
  if (!state.activeTripId) return;

  const ok = await showConfirm(I18n.t("delete.confirm"));
  if (!ok) return;

  try {
    await fetchJson(`${API_BASE}/${state.activeTripId}`, { method: "DELETE" });
    state.trips = state.trips.filter((trip) => trip.id !== state.activeTripId);
    showDashboard();
    renderStats();
    renderTrips();
    showToast(I18n.t("toast.tripDeleted"));
  } catch {
    showToast(I18n.t("toast.error"), "error");
  }
}

async function exportTrips() {
  try {
    const trips = await fetchJson(API_BASE, { method: "GET" });
    const data = JSON.stringify(trips, null, 2);
    const blob = new Blob([data], { type: "application/json" });
    const url = URL.createObjectURL(blob);
    const anchor = document.createElement("a");
    anchor.href = url;
    anchor.download = "travel-plans.json";
    anchor.click();
    URL.revokeObjectURL(url);
    showToast(I18n.t("toast.exported"));
  } catch {
    showToast(I18n.t("export.failed"), "error");
  }
}

// ---- Currencies & countries ---------------------------------------------

async function loadCurrencies() {
  try {
    state.currencies = await fetchJson(CURRENCY_API_BASE, { method: "GET" });
    populateCurrencyDropdown();
  } catch {
    state.currencies = [];
  }
}

async function loadCountries() {
  try {
    state.countries = await fetchJson(COUNTRY_API_BASE, { method: "GET" });
  } catch {
    state.countries = [];
  }
}

function populateCurrencyDropdown() {
  const selected = tripCurrencySelect.value;
  tripCurrencySelect.innerHTML = "";
  const lang = I18n.getLanguage();
  state.currencies.forEach((c) => {
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
    tripCurrencySelect.append(option);
  });
}

let highlightedCountryIndex = -1;

function renderCountryOptions(countries) {
  countryDropdown.innerHTML = "";
  highlightedCountryIndex = -1;

  if (countries.length === 0) {
    const empty = document.createElement("div");
    empty.className = "searchable-select-empty";
    empty.textContent = I18n.t("country.noCountries");
    countryDropdown.append(empty);
    return;
  }

  countries.forEach((c) => {
    const option = document.createElement("div");
    option.className = "searchable-select-option";
    if (c.country === tripCountryHidden.value) option.classList.add("selected");
    option.textContent = c.country;
    option.dataset.currencyCode = c.currencyCode;
    option.dataset.country = c.country;
    option.addEventListener("mousedown", (e) => {
      e.preventDefault();
      selectCountry(c.country, c.currencyCode);
    });
    countryDropdown.append(option);
  });
}

function selectCountry(countryName, currencyCode) {
  tripCountryHidden.value = countryName;
  countrySearchInput.value = countryName;
  countryDropdown.classList.remove("open");
  if (currencyCode) tripCurrencySelect.value = currencyCode;
}

function openCountryDropdown() {
  const query = countrySearchInput.value.toLowerCase().trim();
  const filtered = state.countries.filter((c) =>
    c.country.toLowerCase().includes(query)
  );
  renderCountryOptions(filtered);
  countryDropdown.classList.add("open");
}

function closeCountryDropdown() {
  countryDropdown.classList.remove("open");
  highlightedCountryIndex = -1;
  countrySearchInput.value = tripCountryHidden.value || "";
}

function navigateCountryOptions(e) {
  const options = countryDropdown.querySelectorAll(".searchable-select-option");
  if (!options.length) return;

  if (e.key === "ArrowDown") {
    e.preventDefault();
    highlightedCountryIndex = Math.min(highlightedCountryIndex + 1, options.length - 1);
  } else if (e.key === "ArrowUp") {
    e.preventDefault();
    highlightedCountryIndex = Math.max(highlightedCountryIndex - 1, 0);
  } else if (e.key === "Enter") {
    e.preventDefault();
    if (highlightedCountryIndex >= 0 && highlightedCountryIndex < options.length) {
      const opt = options[highlightedCountryIndex];
      selectCountry(opt.dataset.country, opt.dataset.currencyCode);
    }
    return;
  } else if (e.key === "Escape") {
    closeCountryDropdown();
    return;
  } else {
    return;
  }

  options.forEach((opt, i) => {
    opt.classList.toggle("highlighted", i === highlightedCountryIndex);
  });
  if (highlightedCountryIndex >= 0) {
    options[highlightedCountryIndex].scrollIntoView({ block: "nearest" });
  }
}

// ---- Language ------------------------------------------------------------

async function onLanguageChanged(lang) {
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

// ---- Init ----------------------------------------------------------------

function renderAll() {
  renderStats();
  renderTrips();
  if (getActiveTrip()) renderTripDetails();
}

function bindStatFilters() {
  document.querySelectorAll(".stat-card").forEach((card) => {
    card.addEventListener("click", () => setFilter(card.dataset.filter));
    card.addEventListener("keydown", (e) => {
      if (e.key === "Enter" || e.key === " ") {
        e.preventDefault();
        setFilter(card.dataset.filter);
      }
    });
  });
}

function bindEvents() {
  newTripButton.addEventListener("click", openTripModal);
  emptyCta.addEventListener("click", openTripModal);
  closeTripModalBtn.addEventListener("click", closeTripModal);
  resetTripButton.addEventListener("click", resetTripForm);
  tripModal.addEventListener("click", (e) => {
    if (e.target === tripModal) closeTripModal();
  });
  tripForm.addEventListener("submit", createTrip);

  backButton.addEventListener("click", showDashboard);
  deleteTripButton.addEventListener("click", deleteActiveTrip);

  tabItinerary.addEventListener("click", () => switchTab("itinerary"));
  tabBudget.addEventListener("click", () => switchTab("budget"));
  tabDestination.addEventListener("click", () => switchTab("destination"));

  itineraryForm.addEventListener("submit", addItineraryItem);
  expenseForm.addEventListener("submit", addExpense);
  loadDestinationButton.addEventListener("click", loadDestinationInfo);
  saveNotesButton.addEventListener("click", saveTravelerNotes);
  exportButton.addEventListener("click", exportTrips);
  logoutButton.addEventListener("click", logout);

  confirmOk.addEventListener("click", () => closeConfirm(true));
  confirmCancel.addEventListener("click", () => closeConfirm(false));
  confirmModal.addEventListener("click", (e) => {
    if (e.target === confirmModal) closeConfirm(false);
  });

  countrySearchInput.addEventListener("focus", openCountryDropdown);
  countrySearchInput.addEventListener("input", openCountryDropdown);
  countrySearchInput.addEventListener("keydown", navigateCountryOptions);
  countrySearchInput.addEventListener("blur", closeCountryDropdown);

  document.addEventListener("keydown", (e) => {
    if (e.key === "Escape") {
      if (confirmModal.classList.contains("open")) closeConfirm(false);
      else if (tripModal.classList.contains("open")) closeTripModal();
    }
  });

  bindStatFilters();
}

async function init() {
  if (!checkAuth()) return;

  // Attach event listeners first so the UI is interactive immediately,
  // even while the async data below is still loading.
  bindEvents();

  try {
    Theme.init();
  } catch {
    // ignore theme failures
  }

  await I18n.init();

  usernameDisplay.textContent = localStorage.getItem("username") || "";

  if (languageSwitch) {
    const updateActiveLang = () => {
      const lang = I18n.getLanguage();
      languageSwitch.querySelectorAll(".lang-btn").forEach((btn) => {
        btn.classList.toggle("active", btn.dataset.lang === lang);
      });
    };
    updateActiveLang();
    languageSwitch.querySelectorAll(".lang-btn").forEach((btn) => {
      btn.addEventListener("click", async () => {
        await I18n.setLanguage(btn.dataset.lang);
        updateActiveLang();
        renderAll();
        if (state.currencies.length > 0) populateCurrencyDropdown();
      });
    });
  }

  await Promise.all([loadCurrencies(), loadCountries(), loadTrips()]);
  renderAll();
}

init();
