# 010 — API Documentation (OpenAPI) + Data Import/Export

> **Status**: Proposed

## 1. Overview & motivation

The REST API is undocumented and data can only be exported as JSON from the
client (no matching import). This spec adds interactive OpenAPI/Swagger
documentation, a server-side JSON import endpoint, and additional export
formats (CSV, PDF) to improve developer experience and data portability.

## 2. Functional requirements

- **FR-01** Expose OpenAPI 3 documentation and a Swagger UI page for the API.
- **FR-02** Document existing endpoints (trips, auth, countries, currencies)
  with schemas, parameters and example responses.
- **FR-03** Provide a JSON import endpoint that accepts a previously exported
  trips file and creates trips for the authenticated user.
- **FR-04** Import validates structure and rejects malformed or oversized
  payloads; it does not overwrite existing trips (new IDs assigned).
- **FR-05** Provide CSV export of a single trip's expenses and a PDF export of a
  trip summary (optional format flag).

## 3. API endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `POST` | `/api/trips/import` | Bearer | Import trips (body: JSON array of trips) |
| `GET` | `/api/trips/{tripId}/export?format=csv` | Bearer | Export trip expenses as CSV |
| `GET` | `/api/trips/{tripId}/export?format=pdf` | Bearer | Export trip summary as PDF |
| `GET` | `/swagger-ui/**`, `/v3/api-docs/**` | Public | Swagger UI / OpenAPI JSON |

## 4. Data model / DB changes

- No schema changes required. Import reuses `TripService.createTrip` logic with
  fresh IDs and ownership set to the current user.

## 5. UI requirements

- Add an "Import" button next to "Export Trips" that opens a file picker and
  posts the selected JSON.
- Add per-trip "Export CSV/PDF" actions on the trip detail view.

## 6. Non-functional requirements

- Add `springdoc-openapi-starter-webmvc-ui` dependency.
- Swagger UI and `/v3/api-docs/**` are whitelisted in `SecurityConfig` for
  anonymous read access (or protected, per deployment choice).
- Import caps payload size (e.g. 5 MB) and count (e.g. 500 trips).
- Exports stream responses with correct `Content-Type` and `Content-Disposition`.

## 7. Out of scope

- Bi-directional sync with external systems.
- API versioning headers.
- Export of attachments/documents.
