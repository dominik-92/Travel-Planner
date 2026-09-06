package com.example.travelplanner.service;

import com.example.travelplanner.dto.CategoryExpenseSummary;
import com.example.travelplanner.dto.ExpenseSummaryResponse;
import com.example.travelplanner.model.DestinationInfo;
import com.example.travelplanner.model.ExchangeRate;
import com.example.travelplanner.model.Expense;
import com.example.travelplanner.model.ItineraryItem;
import com.example.travelplanner.model.Trip;
import com.example.travelplanner.model.User;
import com.example.travelplanner.repository.TripRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class TripService {

    private final TripRepository tripRepository;
    private final CurrencyService currencyService;
    private final ExchangeRateService exchangeRateService;

    public TripService(TripRepository tripRepository, CurrencyService currencyService,
                       ExchangeRateService exchangeRateService) {
        this.tripRepository = tripRepository;
        this.currencyService = currencyService;
        this.exchangeRateService = exchangeRateService;
    }

    @Transactional(readOnly = true)
    public List<Trip> findAllByUser(User user) {
        return tripRepository.findByUserId(user.getId());
    }

    @Transactional(readOnly = true)
    public Trip findByIdAndUser(String id, User user) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Trip not found"));
        if (trip.getUser() == null || !trip.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Access denied");
        }
        return trip;
    }

    public Trip createTrip(Trip trip, User user) {
        validateTrip(trip);
        if (trip.getId() == null || trip.getId().isBlank()) {
            trip.setId(generateId());
        }
        if (trip.getCreatedAt() == null || trip.getCreatedAt().isBlank()) {
            trip.setCreatedAt(Instant.now().toString());
        }
        if (trip.getDestinationNotes() == null) {
            trip.setDestinationNotes(trip.getNotes());
        }
        trip.setUser(user);
        return tripRepository.save(trip);
    }

    public Trip updateTrip(String id, Trip updates, User user) {
        Trip trip = findByIdAndUser(id, user);
        validateTrip(updates);
        trip.setName(updates.getName());
        trip.setDestination(updates.getDestination());
        trip.setCountry(updates.getCountry());
        trip.setStartDate(updates.getStartDate());
        trip.setEndDate(updates.getEndDate());
        trip.setBudget(updates.getBudget());
        trip.setCurrency(updates.getCurrency());
        trip.setNotes(updates.getNotes());
        return tripRepository.save(trip);
    }

    private void validateTrip(Trip trip) {
        if (trip.getName() == null || trip.getName().isBlank()) {
            throw validationError("NAME_REQUIRED", "Trip name must not be blank");
        }
        if (trip.getDestination() == null || trip.getDestination().isBlank()) {
            throw validationError("DESTINATION_REQUIRED", "Destination must not be blank");
        }
        if (trip.getCountry() == null || trip.getCountry().isBlank()) {
            throw validationError("COUNTRY_REQUIRED", "Country must not be blank");
        }
        if (trip.getStartDate() == null || trip.getStartDate().isBlank()) {
            throw validationError("START_DATE_REQUIRED", "Start date must not be blank");
        }
        if (trip.getEndDate() == null || trip.getEndDate().isBlank()) {
            throw validationError("END_DATE_REQUIRED", "End date must not be blank");
        }
        if (trip.getEndDate().compareTo(trip.getStartDate()) < 0) {
            throw validationError("END_DATE_BEFORE_START", "End date must not be before start date");
        }
        if (trip.getBudget() <= 0) {
            throw validationError("BUDGET_REQUIRED", "Budget must be greater than zero");
        }
    }

    private IllegalArgumentException validationError(String code, String message) {
        return new IllegalArgumentException(code + "|" + message);
    }

    public void deleteTrip(String id, User user) {
        Trip trip = findByIdAndUser(id, user);
        tripRepository.deleteById(trip.getId());
    }

    public Trip addItineraryItem(String tripId, ItineraryItem item, User user) {
        var trip = findByIdAndUser(tripId, user);
        var finalItem = (item.getId() == null || item.getId().isBlank())
                ? new ItineraryItem(generateId(), item.getDay(), item.getTime(), item.getTitle(), item.getDescription())
                : item;
        trip.addItineraryItem(finalItem);
        tripRepository.save(trip);
        return trip;
    }

    public Trip removeItineraryItem(String tripId, String itemId, User user) {
        var trip = findByIdAndUser(tripId, user);
        trip.getItinerary().removeIf(item -> item.getId().equals(itemId));
        tripRepository.save(trip);
        return trip;
    }

    public Trip addExpense(String tripId, Expense expense, User user) {
        var trip = findByIdAndUser(tripId, user);
        if (expense.getId() == null || expense.getId().isBlank()) {
            expense = new Expense(generateId(), expense.getCategory(), expense.getAmount(), expense.getDescription(), expense.getAddedAt());
        }
        if (expense.getAddedAt() == null || expense.getAddedAt().isBlank()) {
            expense.setAddedAt(Instant.now().toString());
        }
        double rate = currencyService.getHistoricalRate(trip.getCurrency(), expense.getAddedAt());
        expense.setRateToPln(rate);
        trip.addExpense(expense);
        tripRepository.save(trip);
        return trip;
    }

    public Trip removeExpense(String tripId, String expenseId, User user) {
        var trip = findByIdAndUser(tripId, user);
        trip.getExpenses().removeIf(expense -> expense.getId().equals(expenseId));
        tripRepository.save(trip);
        return trip;
    }

    public Trip loadDestinationInfo(String tripId, User user) {
        var trip = findByIdAndUser(tripId, user);
        var info = buildDestinationInfo(trip.getDestination());
        trip.setDestinationInfo(info);
        if (info.getCurrencyCode() != null) {
            trip.setCurrency(info.getCurrencyCode());
        }
        tripRepository.save(trip);
        return trip;
    }

    public Trip updateDestinationNotes(String tripId, String destinationNotes, User user) {
        var trip = findByIdAndUser(tripId, user);
        trip.setDestinationNotes(destinationNotes);
        tripRepository.save(trip);
        return trip;
    }

    @Transactional(readOnly = true)
    public ExpenseSummaryResponse getExpenseSummary(String tripId, User user) {
        Trip trip = findByIdAndUser(tripId, user);

        String currency = normalizeCurrency(trip.getCurrency());
        String reportingCurrency = normalizeCurrency(user.getCurrency());

        Map<String, Double> amountsByCategory = new LinkedHashMap<>();
        for (Expense expense : trip.getExpenses()) {
            if (expense.getCategory() == null || expense.getCategory().isBlank()) {
                continue;
            }
            amountsByCategory.merge(expense.getCategory(), expense.getAmount(), Double::sum);
        }

        double totalSpent = amountsByCategory.values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();

        Conversion conversion = resolveConversion(currency, reportingCurrency, trip.getStartDate());

        List<CategoryExpenseSummary> categories = new ArrayList<>();
        for (Map.Entry<String, Double> entry : amountsByCategory.entrySet()) {
            double amount = round2(entry.getValue());
            if (amount <= 0) {
                continue;
            }
            double percentage = trip.getBudget() > 0 ? round2(amount / trip.getBudget() * 100) : 0;
            Double amountInReporting = conversion.rate() == null
                    ? null
                    : round2(amount * conversion.rate());
            categories.add(new CategoryExpenseSummary(entry.getKey(), amount, percentage, amountInReporting));
        }
        categories.sort((a, b) -> Double.compare(b.amount(), a.amount()));

        Double totalInReporting = conversion.rate() == null
                ? null
                : round2(totalSpent * conversion.rate());

        return new ExpenseSummaryResponse(
                currency,
                reportingCurrency,
                round2(totalSpent),
                totalInReporting,
                conversion.approximate(),
                conversion.provider(),
                conversion.rateDate(),
                categories);
    }

    private record Conversion(Double rate, Boolean approximate, String provider, String rateDate) {
    }

    private Conversion resolveConversion(String currency, String reportingCurrency, String startDateStr) {
        if (currency.equalsIgnoreCase(reportingCurrency)) {
            return new Conversion(null, null, null, null);
        }

        LocalDate today = LocalDate.now();
        LocalDate startDate = parseDate(startDateStr);
        boolean future = startDate != null && startDate.isAfter(today);
        LocalDate effectiveDate = (startDate == null || future) ? today : startDate;

        Optional<ExchangeRate> resolved = exchangeRateService.resolve(currency, reportingCurrency, effectiveDate);
        if (resolved.isEmpty()) {
            return new Conversion(null, null, null, null);
        }

        ExchangeRate rate = resolved.get();
        return new Conversion(
                rate.rate(),
                future,
                rate.provider(),
                rate.date() != null ? rate.date().toString() : null);
    }

    private String normalizeCurrency(String code) {
        return (code == null || code.isBlank()) ? "PLN" : code.trim().toUpperCase();
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value.substring(0, 10));
        } catch (Exception e) {
            return null;
        }
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private DestinationInfo buildDestinationInfo(String destination) {
        var normalized = destination == null ? "" : destination.trim().toLowerCase();

        var defaults = new DestinationInfo(
                "Check local reports for the latest forecast.",
                "Local currency may vary by country.",
                "PLN",
                "Bring comfortable shoes, stay hydrated, and verify transport options in advance."
        );

        return switch (normalized) {
            case String s when s.matches(".*(paris|france|europe).*") ->
                    new DestinationInfo(
                            "Mild and changeable – pack a light layer.",
                            "Euro (€)",
                            "EUR",
                            "Book museums early and use metro passes for savings."
                    );
            case String s when s.matches(".*(london|uk|england|britain).*") ->
                    new DestinationInfo(
                            "Unpredictable weather – carry a compact umbrella.",
                            "Pound Sterling (£)",
                            "GBP",
                            "Plan around tube hours and enjoy pub meals in the evening."
                    );
            case String s when s.matches(".*(new york|usa|united states|america).*") ->
                    new DestinationInfo(
                            "Seasonal: check forecast before packing.",
                            "US Dollar ($)",
                            "USD",
                            "Buy transit cards ahead and reserve popular attractions early."
                    );
            case String s when s.matches(".*(tokyo|japan).*") ->
                    new DestinationInfo(
                            "Often humid in summer; cool in autumn.",
                            "Japanese Yen (¥)",
                            "JPY",
                            "Carry cash for small shops and follow local etiquette."
                    );
            case String s when s.matches(".*(sydney|australia).*") ->
                    new DestinationInfo(
                            "Sunny days are common; sunscreen is essential.",
                            "Australian Dollar (A$)",
                            "AUD",
                            "Respect wildlife and plan for longer travel distances."
                    );
            default -> defaults;
        };
    }

    private String generateId() {
        return System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 6);
    }
}
