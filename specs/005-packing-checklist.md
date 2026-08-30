# 005 — Packing Checklist

> **Status**: Proposed

## 1. Overview & motivation

Travelers need a lightweight way to plan what to pack for each trip. This spec
adds a per-trip packing checklist with items that can be added, toggled and
removed, plus a completion progress indicator.

## 2. Functional requirements

- **FR-01** A user can add a packing item to a trip (text label, optional
  quantity).
- **FR-02** A user can mark a packing item as packed/unpacked (toggle).
- **FR-03** A user can remove a packing item.
- **FR-04** The UI shows progress (packed / total) for each trip.
- **FR-05** Packing items are scoped to the trip owner and cascade-deleted with
  the trip.

## 3. API endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `POST` | `/api/trips/{tripId}/packing` | Bearer | Add item (body: `PackingItem`) |
| `PUT` | `/api/trips/{tripId}/packing/{itemId}` | Bearer | Toggle/update item |
| `DELETE` | `/api/trips/{tripId}/packing/{itemId}` | Bearer | Remove item |

Responses return the updated `Trip` (matching the existing itinerary/expense
pattern) so the frontend can refresh state in one call.

## 4. Data model / DB changes

- New entity `PackingItem` (`packing_items` table):
  - `id` (String PK)
  - `label` (String)
  - `quantity` (int, default 1)
  - `packed` (boolean, default false)
  - `trip_id` (FK, `@ManyToOne` + `@JsonIgnore`, mirroring `Expense`)
- `Trip` gains a `@OneToMany` `packingItems` list with cascade + orphan removal.

## 5. UI requirements

- New "Packing" tab (or section) on the trip detail view.
- Add-item input, checkbox list, and a progress bar showing packed/total.
- Reuse the existing list styling (`list-item`) and toast messages.

## 6. Non-functional requirements

- Owner-only access enforced via `findByIdAndUser`.
- i18n keys for labels and toasts added to `en.json`, `pl.json`, `es.json`.

## 7. Out of scope

- Shared/preset packing templates.
- Per-trip-day packing groups.
