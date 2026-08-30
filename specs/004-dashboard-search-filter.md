# 004 — Dashboard Search, Sort & Filter

> **Status**: Proposed

## 1. Overview & motivation

The dashboard currently filters trips only by "all / upcoming / past" via
client-side stat cards. As the number of trips grows, users need to search by
name or destination, sort by date or budget, and filter by destination country
or currency. This spec moves searching, sorting and filtering to the backend
for scalability.

## 2. Functional requirements

- **FR-01** Filter trips by free-text query matching `name`, `destination` or
  `country` (case-insensitive, partial match).
- **FR-02** Filter by trip status: `all`, `upcoming`, `past`.
- **FR-03** Filter by currency code (e.g. `PLN`, `EUR`).
- **FR-04** Sort by `startDate`, `endDate`, `createdAt`, `budget` or `name`,
  ascending or descending (default: `startDate` ascending).
- **FR-05** Results are always scoped to the authenticated user.

## 3. API endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `GET` | `/api/trips?search=&status=&currency=&sort=&order=` | Bearer | Query trips |

Query parameters are optional and combinable. `TripRepository` gains a
query method (e.g. `@Query` with `lower(...) like ...`) plus sorting via the
`sort`/`order` parameters.

## 4. Data model / DB changes

- No schema changes required.

## 5. UI requirements

- Add a search input and a sort dropdown to the dashboard header area.
- Wire the stat cards to the `status` parameter.
- Debounce search input (e.g. 300ms) before re-fetching trips.
- Show a "no matches" message when results are empty (reuse `trip.noMatch`).

## 6. Non-functional requirements

- Filtering and sorting execute in the database (no full-table load in memory).
- Query values must be validated and parameterized to avoid injection.
- i18n keys for sort/filter labels added to all three locale files.

## 7. Out of scope

- Pagination (covered in a future spec).
- Saved/favorite filters.
- Fuzzy or phonetic search.
