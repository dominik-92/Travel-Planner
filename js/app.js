const API_BASE = "http://localhost:8080/api/trips";
const CURRENCY_API_BASE = "http://localhost:8080/api/currencies";
const COUNTRY_API_BASE = "http://localhost:8080/api/countries";

function getToken() {
  return localStorage.getItem("token");
}

function logout() {
  localStorage.removeItem("token");
  localStorage.removeItem("username");
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

function formatCurrency(value, currencyCode = "PLN") {
  return new Intl.NumberFormat("pl-PL", {
    style: "currency",
    currency: currencyCode,
  }).format(Number(value));
}

function formatDate(date) {
  const dt = new Date(date);
  if (Number.isNaN(dt.getTime())) return "Invalid date";
  return dt.toLocaleDateString(undefined, {
    year: "numeric",
    month: "short",
    day: "numeric",
  });
}

function getActiveTrip() {
  return state.trips.find((trip) => trip.id === state.activeTripId) || null;
}

function updateTripSummary() {
  if (state.trips.length === 0) {
    tripSummary.textContent = "No trips created yet.";
    return;
  }

  const totalTrips = state.trips.length;
  const upcoming = state.trips.filter((trip) => new Date(trip.startDate) >= new Date()).length;
  tripSummary.textContent = `${totalTrips} trip${totalTrips === 1 ? "" : "s"} saved • ${upcoming} upcoming`;
}

function renderTrips() {
  tripsList.innerHTML = "";

  if (state.trips.length === 0) {
    const empty = document.createElement("p");
    empty.textContent = "Create your first trip to see it on the dashboard.";
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
    viewButton.textContent = "View Trip";
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
      <p><strong>Budget:</strong> ${formatCurrency(trip.budget, trip.currency)}</p>
      <p><strong>Currency:</strong> ${trip.currency || "PLN"}</p>
      <p><strong>Notes:</strong> ${trip.notes || "No notes yet"}</p>
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
    message.textContent = "No itinerary items added.";
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
          <strong>Day ${item.day}</strong> · ${item.time}
        </div>
        <div>
          <p>${item.title}</p>
          <span>${item.description || "No additional details"}</span>
        </div>
      `;

      const removeButton = document.createElement("button");
      removeButton.textContent = "Remove";
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
    option.textContent = `Day ${day}`;
    itineraryDay.append(option);
  }
}

function renderExpenses(trip) {
  expenseList.innerHTML = "";
  if (!trip.expenses || trip.expenses.length === 0) {
    const message = document.createElement("p");
    message.textContent = "No expenses recorded.";
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
          <p>${expense.description || "No description"}</p>
          <span>${new Date(expense.addedAt).toLocaleString()}</span>
        </div>
      `;

      const removeButton = document.createElement("button");
      removeButton.textContent = "Remove";
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
  budgetSpent.textContent = `Spent: ${formatCurrency(spentInCurrency, currency)}`;
  budgetRemaining.textContent = `Remaining: ${formatCurrency(remaining, currency)}`;
  budgetProgress.style.width = `${ratio}%`;
  budgetProgress.style.background = ratio > 100 ? "#ef4444" : "linear-gradient(90deg, #10b981 0%, #22c55e 100%)";

  if (currency !== "PLN") {
    budgetPlnEquivalent.textContent = `≈ ${formatCurrency(spentInPln, "PLN")} PLN spent`;
  } else {
    budgetPlnEquivalent.textContent = "";
  }
}

function renderDestinationInfo(trip) {
  destinationWeather.textContent = trip.destinationInfo?.weather || "N/A";
  destinationCurrency.textContent = trip.destinationInfo?.currency || "N/A";
  destinationTips.textContent = trip.destinationInfo?.tips || "N/A";
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
    alert("Failed to export trips. Is the backend running?");
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
  tripCurrencySelect.innerHTML = "";
  state.currencies.forEach((c) => {
    const option = document.createElement("option");
    option.value = c.code;
    option.textContent = `${c.code} - ${c.name}`;
    if (c.code === "PLN") option.selected = true;
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
    empty.textContent = "No countries found";
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


async function init() {
  if (!checkAuth()) return;

  usernameDisplay.textContent = localStorage.getItem("username") || "";

  await loadCurrencies();
  await loadCountries();
  await loadTrips();
  renderTrips();
  updateTripSummary();

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

init();
