# 006 — Collaborative Trip Sharing

> **Status**: Proposed

## 1. Overview & motivation

Trips are currently private to a single user. Friends or family planning a
joint trip cannot view or edit the same itinerary. This spec adds the ability
to share a trip with other registered users under "viewer" or "editor" roles.

## 2. Functional requirements

- **FR-01** A trip owner can invite another registered user (by username or
  email) to a trip.
- **FR-02** Two roles are supported: `VIEWER` (read-only) and `EDITOR`
  (can modify itinerary, expenses, packing items and notes).
- **FR-03** Only the owner can delete the trip, manage members, or transfer
  ownership.
- **FR-04** Shared trips appear in the invited user's dashboard, marked as
  shared.
- **FR-05** A member can leave a shared trip; an owner can remove a member.

## 3. API endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `POST` | `/api/trips/{tripId}/members` | Bearer (owner) | Invite (body: `username`/`email`, `role`) |
| `DELETE` | `/api/trips/{tripId}/members/{userId}` | Bearer (owner) | Remove member |
| `GET` | `/api/trips/{tripId}/members` | Bearer | List members |

Existing `GET /api/trips` returns both owned and shared trips. Mutation
endpoints must verify the caller's role before allowing writes.

## 4. Data model / DB changes

- New table `trip_members`:
  - `trip_id` (FK), `user_id` (FK), `role` (String: `VIEWER`/`EDITOR`)
  - Composite or surrogate PK; unique constraint on `(trip_id, user_id)`.
- `TripRepository` gains a query to find trips where the user is owner or member.

## 5. UI requirements

- "Share" action on the trip detail view; invite form with role selector.
- Members list with role badges and remove/leave controls.
- Shared trips show a "shared" badge on the dashboard card.

## 6. Non-functional requirements

- Authorization must be enforced server-side in `TripService` for every
  mutation (owner or editor), never client-side only.
- Deleting a trip or account cascades `trip_members` rows.
- i18n keys for sharing labels added to all three locale files.

## 7. Out of scope

- Public/shared links for unauthenticated users.
- Real-time collaboration (WebSockets).
- Transfer of ownership between users.
