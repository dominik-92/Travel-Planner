const API_BASE = "http://localhost:8080/api/trips";

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

const loadDestinationButton = document.getElementById("load-destination-info");
const saveNotesButton = document.getElementById("save-notes-button");
const destinationWeather = document.getElementById("destination-weather");
const destinationCurrency = document.getElementById("destination-currency");
const destinationTips = document.getElementById("destination-tips");
const destinationNotes = document.getElementById("destination-notes");
const deleteTripButton = document.getElementById("delete-trip-button");

let state = {
  trips: [],
  activeTripId: null,
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

function formatCurrency(value) {
  return `€${Number(value).toFixed(2)}`;
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
      <p>${trip.destination}</p>
      <p>${formatDate(trip.startDate)} → ${formatDate(trip.endDate)}</p>
    </div>
    <div>
      <p><strong>Budget:</strong> ${formatCurrency(trip.budget)}</p>
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

  trip.expenses
    .sort((a, b) => new Date(a.addedAt) - new Date(b.addedAt))
    .forEach((expense) => {
      const itemElement = document.createElement("div");
      itemElement.className = "list-item";
      itemElement.innerHTML = `
        <div>
          <strong>${expense.category}</strong> · ${formatCurrency(expense.amount)}
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
  const spent = trip.expenses.reduce((total, item) => total + Number(item.amount), 0);
  const remaining = Math.max(0, Number(trip.budget) - spent);
  const ratio = trip.budget > 0 ? Math.min(100, (spent / trip.budget) * 100) : 0;

  budgetTotal.textContent = formatCurrency(trip.budget);
  budgetSpent.textContent = `Spent: ${formatCurrency(spent)}`;
  budgetRemaining.textContent = `Remaining: ${formatCurrency(remaining)}`;
  budgetProgress.style.width = `${ratio}%`;
  budgetProgress.style.background = ratio > 100 ? "#ef4444" : "linear-gradient(90deg, #10b981 0%, #22c55e 100%)";
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
}

async function createTrip(event) {
  event.preventDefault();
  const formData = new FormData(tripForm);
  const name = formData.get("tripName").trim();
  const destination = formData.get("destination").trim();
  const startDate = formData.get("startDate");
  const endDate = formData.get("endDate");
  const budget = Number(formData.get("budget"));
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

async function init() {
  if (!checkAuth()) return;

  usernameDisplay.textContent = localStorage.getItem("username") || "";

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
}

init();
