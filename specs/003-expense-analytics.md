# 003 — Expense Analytics & Category Breakdown

> **Status**: Proposed

## 1. Overview & motivation

Expenses are tracked per trip with a `category` field, but the UI only shows a
single "spent vs. budget" bar. Users cannot see how their spending is distributed
across categories. This spec adds per-category aggregation and a breakdown
visualization to improve budget insight.

## 2. Functional requirements

- **FR-01** Provide per-category spending totals for a trip (sum of `amount`
  grouped by `category`).
- **FR-02** Provide the percentage of the trip budget consumed by each category.
- **FR-03** Provide the PLN-equivalent total per category (using the existing
  `rateToPln` field) when the trip currency is not PLN.
- **FR-04** Categories with zero spending may be omitted from the response.
- **FR-05** The owner-only rule applies; non-owners receive `403`.

## 3. API endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `GET` | `/api/trips/{tripId}/expenses/summary` | Bearer | Category breakdown |

Example response:

```json
{
  "currency": "EUR",
  "totalSpent": 540.0,
  "totalSpentPln": 2300.0,
  "categories": [
    { "category": "Accommodation", "amount": 300.0, "percentage": 30.0 },
    { "category": "Food", "amount": 240.0, "percentage": 24.0 }
  ]
}
```

## 4. Data model / DB changes

- No schema changes required; aggregation can be computed in `TripService` from
  the existing `Expense` collection (or via a JPQL projection in
  `TripRepository` for large datasets).

## 5. UI requirements

- On the Budget tab, render a category breakdown (list and/or simple bar chart)
  below the existing progress bar.
- Categories use the existing i18n labels (`expense.categoryAccommodation`, etc.).
- Keep rendering client-side from the summary endpoint to avoid duplicating
  logic in `app.js`.

## 6. Non-functional requirements

- Aggregation must be server-side to keep payloads small.
- Results must reflect the current saved state immediately after add/remove.
- i18n keys for any new labels added to all three locale files.

## 7. Out of scope

- Historical / cross-trip analytics.
- Exporting analytics as a chart image.
- Budget allocation limits per category.
