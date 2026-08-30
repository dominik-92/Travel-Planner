# 002 — Trip Editing (Full CRUD)

> **Status**: Proposed

## 1. Overview & motivation

Trips can currently be created (`POST /api/trips`) and deleted
(`DELETE /api/trips/{tripId}`) but not edited. Users who make a mistake in a
trip name, date range, budget or destination must delete and recreate the trip,
losing its itinerary and expenses. This spec adds update capability to complete
trip CRUD.

## 2. Functional requirements

- **FR-01** A user can update the mutable fields of their own trip: `name`,
  `destination`, `country`, `startDate`, `endDate`, `budget`, `currency` and
  `notes`.
- **FR-02** Updating a trip must not overwrite its `id`, `createdAt`, `user`,
  itinerary or expenses.
- **FR-03** Validation: `endDate` must not be before `startDate`; `budget` must
  be non-negative; `name` and `destination` must not be blank.
- **FR-04** Only the owning user may edit a trip; otherwise `403` is returned.

## 3. API endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `PUT` | `/api/trips/{tripId}` | Bearer | Update a trip (body: `Trip` partial JSON) |

`TripController` gains a `@PutMapping` delegating to a new
`TripService.updateTrip(...)`. The existing `findByIdAndUser` guard is reused
for ownership checks.

## 4. Data model / DB changes

- No schema changes required; the `Trip` entity already has all required fields.

## 5. UI requirements

- Add an "Edit" action on the trip detail view (next to "Delete Trip").
- Reuse the existing `trip-modal` with an edit mode; pre-fill fields from the
  active trip and change the submit button label.
- On successful update, refresh `state.trips`, re-render stats/dashboard, and
  show the existing success toast.

## 6. Non-functional requirements

- Reuse `GlobalExceptionHandler` for `404` (not found) and `403` (not owner).
- Server-side validation must be enforced, not just client-side.
- i18n keys for the edit flow added to `en.json`, `pl.json`, `es.json`.

## 7. Out of scope

- Bulk editing of multiple trips.
- Versioning / audit trail of trip changes.
- Editing itinerary items or expenses (covered by other specs).
