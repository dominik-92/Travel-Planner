const API_BASE = "http://localhost:8080/api/trips";
const CURRENCY_API_BASE = "http://localhost:8080/api/currencies";
const COUNTRY_API_BASE = "http://localhost:8080/api/countries";
const USER_API = "http://localhost:8080/api/user";

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

const tripForm = document.getElementById("trip-form");
const itineraryForm = document.getElementById("itinerary-form");
const expenseForm = document.getElementById("expense-form");
const exportButton = document.getElementById("export-data-button");
const logoutButton = document.getElementById("logout-button");
const usernameDisplay = document.getElementById("username-display");
const languageSelect = document.getElementById("language-select");

const tripsList = document.getElementById("trips-list");
const tripSummary = document.getElementById("trip-summary");
const tripDetailsPanel = document.getElementById("trip-details-panel");
const activeTripCard = document.getElementById("active-trip-card");
const itineraryDay = document.getElementById("itinerary-day");
const itineraryList = document.getElementById("itinerary-list");
const expenseList = document.getElementById("expense-list");

const budgetTotal = document.getElementById("budget-total");
const budgetSpent = document.getElementById("budget-spent");
const budgetRemaining = document.getElementById("budget-remaining");
const budgetProgress = document.getElementById("budget-progress");
const budgetPlnEquivalent = document.getElementById("budget-pln-equivalent");

const loadDestinationButton = document.getElementById("load-destination-info");
const saveNotesButton = document.getElementById("save-notes-button");
const destinationWeather = document.getElementById("destination-weather");
const destinationCurrency = document.getElementById("destination-currency");
const destinationTips = document.getElementById("destination-tips");
const destinationNotes = document.getElementById("destination-notes");
const deleteTripButton = document.getElementById("delete-trip-button");
const tripCurrencySelect = document.getElementById("trip-currency");
const tripCountryHidden = document.getElementById("trip-country");
const countrySearchInput = document.getElementById("country-search");
const countryDropdown = document.getElementById("country-dropdown");
const countrySelectWrapper = document.getElementById("country-select-wrapper");
const destinationInput = document.getElementById("destination");

let state = {
  trips: [],
  activeTripId: null,
  currencies: [],
  countries: [],
};

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
  return new Intl.NumberFormat(getLocale(), {
    style: "currency",
    currency: currencyCode,
  }).format(Number(value));
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

function pluralForm(count, lang) {
  if (lang === "pl") {
    if (count === 1) return 0;
    if (count >= 2 && count <= 4 && count % 1 === 0) return 1;
    return 2;
  }
  return count === 1 ? 0 : 1;
}

function updateTripSummary() {
  if (state.trips.length === 0) {
    tripSummary.textContent = I18n.t("trip.noTrips");
    return;
  }

  const totalTrips = state.trips.length;
  const upcoming = state.trips.filter((trip) => new Date(trip.startDate) >= new Date()).length;
  const lang = I18n.getLanguage();
  const pluralIdx = pluralForm(totalTrips, lang);

  let pluralSuffix;
  if (lang === "pl") {
    pluralSuffix = ["ka", "ki", "k"][pluralIdx];
  } else {
    pluralSuffix = pluralIdx === 0 ? "" : "s";
  }

  const tripsSaved = I18n.t("trip.tripsSaved", { 0: totalTrips, 1: pluralSuffix });
  const upcomingText = I18n.t("trip.upcoming", { 0: upcoming });
  const sep = I18n.t("trip.summarySeparator");
  tripSummary.textContent = `${tripsSaved}${sep}${upcomingText}`;
}

function renderTrips() {
  tripsList.innerHTML = "";

  if (state.trips.length === 0) {
    const empty = document.createElement("p");
    empty.textContent = I18n.t("trip.emptyDashboard");
    empty.style.color = "var(--muted)";
    tripsList.append(empty);
    return;
  }

  state.trips.forEach((trip) => {
    const card = document.createElement("article");
    card.className = "trip-card";

    const info = document.createElement("div");
    const title = document.createElement("h3");
    title.textContent = trip.name;
    const meta = document.createElement("p");
    meta.textContent = `${trip.destination} · ${formatDate(trip.startDate)} - ${formatDate(trip.endDate)}`;

    info.append(title, meta);

    const actions = document.createElement("div");
    const viewButton = document.createElement("button");
    viewButton.textContent = I18n.t("trip.viewTrip");
    viewButton.className = "btn-secondary";
    viewButton.addEventListener("click", () => openTripDetails(trip.id));

    actions.append(viewButton);
    card.append(info, actions);

    tripsList.append(card);
  });
}

function showTripDetailsPanel(show) {
  tripDetailsPanel.classList.toggle("hidden", !show);
}

function openTripDetails(tripId) {
  state.activeTripId = tripId;
  renderTripDetails();
}

function renderTripDetails() {
  const trip = getActiveTrip();

  if (!trip) {
    showTripDetailsPanel(false);
    return;
  }

  showTripDetailsPanel(true);
  activeTripCard.innerHTML = `
    <div>
      <h3>${trip.name}</h3>
      <p>${trip.destination}${trip.country ? ` (${trip.country})` : ""}</p>
      <p>${formatDate(trip.startDate)} → ${formatDate(trip.endDate)}</p>
    </div>
    <div>
      <p><strong>${I18n.t("trip.budgetLabel")}</strong> ${formatCurrency(trip.budget, trip.currency)}</p>
      <p><strong>${I18n.t("trip.currencyLabel")}</strong> ${trip.currency || "PLN"}</p>
      <p><strong>${I18n.t("trip.notesLabel")}</strong> ${trip.notes || I18n.t("trip.noNotes")}</p>
    </div>
  `;

  destinationNotes.value = trip.destinationNotes || "";
  renderItinerary(trip);
  renderExpenses(trip);
  renderBudget(trip);
  renderDestinationInfo(trip);
  populateItineraryDayOptions(trip);
}

function renderItinerary(trip) {
  itineraryList.innerHTML = "";
  if (!trip.itinerary || trip.itinerary.length === 0) {
    const message = document.createElement("p");
    message.textContent = I18n.t("itinerary.empty");
    message.style.color = "var(--muted)";
    itineraryList.append(message);
    return;
  }

  trip.itinerary
    .sort((a, b) => a.day - b.day || a.time.localeCompare(b.time))
    .forEach((item) => {
      const itemElement = document.createElement("div");
      itemElement.className = "list-item";
      itemElement.innerHTML = `
        <div>
          <strong>${I18n.t("itinerary.dayFormat", { 0: item.day })}</strong> · ${item.time}
        </div>
        <div>
          <p>${item.title}</p>
          <span>${item.description || I18n.t("itinerary.noDetails")}</span>
        </div>
      `;

      const removeButton = document.createElement("button");
      removeButton.textContent = I18n.t("itinerary.remove");
      removeButton.className = "btn-secondary";
      removeButton.addEventListener("click", () => {
        removeItineraryItem(trip.id, item.id);
      });

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
    const message = document.createElement("p");
    message.textContent = I18n.t("expense.empty");
    message.style.color = "var(--muted)";
    expenseList.append(message);
    return;
  }

  const currency = trip.currency || "PLN";

  trip.expenses
    .sort((a, b) => new Date(a.addedAt) - new Date(b.addedAt))
    .forEach((expense) => {
      const plnAmount = expense.amount * (expense.rateToPln || 1);
      const plnText = currency !== "PLN" ? ` (≈ ${formatCurrency(plnAmount, "PLN")})` : "";

      const itemElement = document.createElement("div");
      itemElement.className = "list-item";
      itemElement.innerHTML = `
        <div>
          <strong>${expense.category}</strong> · ${formatCurrency(expense.amount, currency)}
          <span class="currency-conversion">${plnText}</span>
        </div>
        <div>
          <p>${expense.description || I18n.t("expense.noDescription")}</p>
          <span>${new Date(expense.addedAt).toLocaleString()}</span>
        </div>
      `;

      const removeButton = document.createElement("button");
      removeButton.textContent = I18n.t("expense.remove");
      removeButton.className = "btn-secondary";
      removeButton.addEventListener("click", () => {
        removeExpense(trip.id, expense.id);
      });

      itemElement.append(removeButton);
      expenseList.append(itemElement);
    });
}

function renderBudget(trip) {
  const currency = trip.currency || "PLN";
  const spentInCurrency = trip.expenses.reduce((total, item) => total + Number(item.amount), 0);
  const spentInPln = trip.expenses.reduce((total, item) => total + Number(item.amount) * (item.rateToPln || 1), 0);
  const remaining = Math.max(0, Number(trip.budget) - spentInCurrency);
  const ratio = trip.budget > 0 ? Math.min(100, (spentInCurrency / trip.budget) * 100) : 0;

  budgetTotal.textContent = formatCurrency(trip.budget, currency);
  budgetSpent.textContent = I18n.t("budget.spent", { 0: formatCurrency(spentInCurrency, currency) });
  budgetRemaining.textContent = I18n.t("budget.remaining", { 0: formatCurrency(remaining, currency) });
  budgetProgress.style.width = `${ratio}%`;
  budgetProgress.style.background = ratio > 100 ? "#ef4444" : "linear-gradient(90deg, #10b981 0%, #22c55e 100%)";

  if (currency !== "PLN") {
    budgetPlnEquivalent.textContent = I18n.t("budget.plnSpent", { 0: formatCurrency(spentInPln, "PLN") });
  } else {
    budgetPlnEquivalent.textContent = "";
  }
}

function renderDestinationInfo(trip) {
  destinationWeather.textContent = trip.destinationInfo?.weather || I18n.t("destination.na");
  destinationCurrency.textContent = trip.destinationInfo?.currency || I18n.t("destination.na");
  destinationTips.textContent = trip.destinationInfo?.tips || I18n.t("destination.na");
}

async function removeItineraryItem(tripId, itemId) {
  try {
    const updatedTrip = await fetchJson(`${API_BASE}/${tripId}/itinerary/${itemId}`, { method: "DELETE" });
    const index = state.trips.findIndex((t) => t.id === tripId);
    if (index !== -1) state.trips[index] = updatedTrip;
    renderTripDetails();
  } catch {
    // ignore
  }
}

async function removeExpense(tripId, expenseId) {
  try {
    const updatedTrip = await fetchJson(`${API_BASE}/${tripId}/expenses/${expenseId}`, { method: "DELETE" });
    const index = state.trips.findIndex((t) => t.id === tripId);
    if (index !== -1) state.trips[index] = updatedTrip;
    renderTripDetails();
  } catch {
    // ignore
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
    renderTrips();
    updateTripSummary();
    resetTripForm();
    openTripDetails(created.id);
  } catch {
    // ignore
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
    itineraryForm.reset();
  } catch {
    // ignore
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
    expenseForm.reset();
  } catch {
    // ignore
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
  } catch {
    // ignore
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
    renderTripDetails();
  } catch {
    // ignore
  }
}

async function deleteActiveTrip() {
  if (!state.activeTripId) return;

  try {
    await fetchJson(`${API_BASE}/${state.activeTripId}`, { method: "DELETE" });
    state.trips = state.trips.filter((trip) => trip.id !== state.activeTripId);
    state.activeTripId = null;
    renderTrips();
    updateTripSummary();
    showTripDetailsPanel(false);
  } catch {
    // ignore
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
  } catch {
    alert(I18n.t("export.failed"));
  }
}

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
    populateCountryDropdown();
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
    const displayName = lang === "pl" && c.namePl ? c.namePl
      : lang === "es" && c.nameEs ? c.nameEs
      : c.name;
    option.textContent = `${c.code} - ${displayName}`;
    if (c.code === selected || (!selected && c.code === "PLN")) option.selected = true;
    tripCurrencySelect.append(option);
  });
}

let highlightedCountryIndex = -1;

function populateCountryDropdown() {
  renderCountryOptions(state.countries);
}

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

  countries.forEach((c, index) => {
    const option = document.createElement("div");
    option.className = "searchable-select-option";
    if (c.country === tripCountryHidden.value) {
      option.classList.add("selected");
    }
    option.textContent = c.country;
    option.dataset.currencyCode = c.currencyCode;
    option.dataset.country = c.country;
    option.dataset.index = String(index);
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
  if (currencyCode) {
    tripCurrencySelect.value = currencyCode;
  }
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
  if (!tripCountryHidden.value) {
    countrySearchInput.value = "";
  } else {
    countrySearchInput.value = tripCountryHidden.value;
  }
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

async function init() {
  if (!checkAuth()) return;

  await I18n.init();

  usernameDisplay.textContent = localStorage.getItem("username") || "";

  if (languageSelect) {
    languageSelect.value = I18n.getLanguage();
    languageSelect.addEventListener("change", async (e) => {
      await I18n.setLanguage(e.target.value);
      refreshAll();
    });
  }

  await loadCurrencies();
  await loadCountries();
  await loadTrips();
  refreshAll();

  tripForm.addEventListener("submit", createTrip);
  itineraryForm.addEventListener("submit", addItineraryItem);
  expenseForm.addEventListener("submit", addExpense);
  loadDestinationButton.addEventListener("click", loadDestinationInfo);
  saveNotesButton.addEventListener("click", saveTravelerNotes);
  deleteTripButton.addEventListener("click", deleteActiveTrip);
  exportButton.addEventListener("click", exportTrips);
  logoutButton.addEventListener("click", logout);
  countrySearchInput.addEventListener("focus", openCountryDropdown);
  countrySearchInput.addEventListener("input", openCountryDropdown);
  countrySearchInput.addEventListener("keydown", navigateCountryOptions);
  countrySearchInput.addEventListener("blur", closeCountryDropdown);
}

function refreshAll() {
  renderTrips();
  updateTripSummary();
  renderTripDetails();
  if (state.currencies.length > 0) {
    populateCurrencyDropdown();
  }
}

init();
