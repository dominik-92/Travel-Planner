const STORAGE_KEY = "travel-planner-trips";

const tripForm = document.getElementById("trip-form");
const itineraryForm = document.getElementById("itinerary-form");
const expenseForm = document.getElementById("expense-form");
const exportButton = document.getElementById("export-data-button");

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

function loadTrips() {
  const raw = localStorage.getItem(STORAGE_KEY);
  state.trips = raw ? JSON.parse(raw) : [];
}

function saveTrips() {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(state.trips));
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
  saveTrips();
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

function removeItineraryItem(tripId, itemId) {
  const trip = state.trips.find((entry) => entry.id === tripId);
  if (!trip) return;
  trip.itinerary = trip.itinerary.filter((item) => item.id !== itemId);
  saveTrips();
  renderTripDetails();
}

function removeExpense(tripId, expenseId) {
  const trip = state.trips.find((entry) => entry.id === tripId);
  if (!trip) return;
  trip.expenses = trip.expenses.filter((item) => item.id !== expenseId);
  saveTrips();
  renderTripDetails();
}

function generateId() {
  return `${Date.now()}-${Math.random().toString(36).slice(2, 8)}`;
}

function buildDestinationInfo(destination) {
  const normalized = destination.trim().toLowerCase();
  const basicInfo = {
    weather: "Check local reports for the latest forecast.",
    currency: "Local currency may vary by country.",
    tips:
      "Bring comfortable shoes, stay hydrated, and verify transport options in advance.",
  };

  const mapping = [
    {
      pattern: /(paris|france|europe)/i,
      weather: "Mild and changeable – pack a light layer.",
      currency: "Euro (€)",
      tips: "Book museums early and use metro passes for savings.",
    },
    {
      pattern: /(london|uk|england|britain)/i,
      weather: "Unpredictable weather – carry a compact umbrella.",
      currency: "Pound Sterling (£)",
      tips: "Plan around tube hours and enjoy pub meals in the evening.",
    },
    {
      pattern: /(new york|usa|united states|america)/i,
      weather: "Seasonal: check forecast before packing.",
      currency: "US Dollar ($)",
      tips: "Buy transit cards ahead and reserve popular attractions early.",
    },
    {
      pattern: /(tokyo|japan)/i,
      weather: "Often humid in summer; cool in autumn.",
      currency: "Japanese Yen (¥)",
      tips: "Carry cash for small shops and follow local etiquette.",
    },
    {
      pattern: /(sydney|australia)/i,
      weather: "Sunny days are common; sunscreen is essential.",
      currency: "Australian Dollar (A$)",
      tips: "Respect wildlife and plan for longer travel distances.",
    },
  ];

  const matched = mapping.find((entry) => entry.pattern.test(normalized));
  return matched || basicInfo;
}

function resetTripForm() {
  tripForm.reset();
  document.getElementById("budget").value = 0;
}

function createTrip(event) {
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
    id: generateId(),
    name,
    destination,
    startDate,
    endDate,
    budget: Number.isFinite(budget) ? budget : 0,
    notes,
    destinationNotes: notes,
    itinerary: [],
    expenses: [],
    destinationInfo: null,
    createdAt: new Date().toISOString(),
  };

  state.trips.push(trip);
  saveTrips();
  renderTrips();
  updateTripSummary();
  resetTripForm();
  openTripDetails(trip.id);
}

function addItineraryItem(event) {
  event.preventDefault();
  const trip = getActiveTrip();
  if (!trip) return;

  const day = Number(itineraryDay.value);
  const time = document.getElementById("itinerary-time").value;
  const title = document.getElementById("itinerary-title").value.trim();
  const description = document.getElementById("itinerary-description").value.trim();

  if (!day || !time || !title) return;

  trip.itinerary.push({
    id: generateId(),
    day,
    time,
    title,
    description,
  });

  saveTrips();
  renderTripDetails();
  itineraryForm.reset();
}

function addExpense(event) {
  event.preventDefault();
  const trip = getActiveTrip();
  if (!trip) return;

  const category = document.getElementById("expense-category").value;
  const amount = Number(document.getElementById("expense-amount").value);
  const description = document.getElementById("expense-description").value.trim();

  if (!category || !Number.isFinite(amount) || amount <= 0) return;

  trip.expenses.push({
    id: generateId(),
    category,
    amount,
    description,
    addedAt: new Date().toISOString(),
  });

  saveTrips();
  renderTripDetails();
  expenseForm.reset();
}

function loadDestinationInfo() {
  const trip = getActiveTrip();
  if (!trip) return;
  trip.destinationInfo = buildDestinationInfo(trip.destination);
  saveTrips();
  renderTripDetails();
}

function saveTravelerNotes() {
  const trip = getActiveTrip();
  if (!trip) return;
  trip.destinationNotes = destinationNotes.value.trim();
  saveTrips();
  renderTripDetails();
}

function deleteActiveTrip() {
  if (!state.activeTripId) return;
  state.trips = state.trips.filter((trip) => trip.id !== state.activeTripId);
  state.activeTripId = null;
  saveTrips();
  renderTrips();
  updateTripSummary();
  showTripDetailsPanel(false);
}

function exportTrips() {
  const data = JSON.stringify(state.trips, null, 2);
  const blob = new Blob([data], { type: "application/json" });
  const url = URL.createObjectURL(blob);
  const anchor = document.createElement("a");
  anchor.href = url;
  anchor.download = "travel-plans.json";
  anchor.click();
  URL.revokeObjectURL(url);
}

function init() {
  loadTrips();
  renderTrips();
  updateTripSummary();

  tripForm.addEventListener("submit", createTrip);
  itineraryForm.addEventListener("submit", addItineraryItem);
  expenseForm.addEventListener("submit", addExpense);
  loadDestinationButton.addEventListener("click", loadDestinationInfo);
  saveNotesButton.addEventListener("click", saveTravelerNotes);
  deleteTripButton.addEventListener("click", deleteActiveTrip);
  exportButton.addEventListener("click", exportTrips);
}

init();