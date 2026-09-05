# 011 — Settings Layout & Preferences

> **Status**: In progress

## 1. Overview & motivation

The account settings page (`account.html`) is currently a single-column stack of
auth-style cards with inline styling. It feels dated and doesn't match the
dashboard. This spec redesigns it into a modern two-pane settings screen with a
left navigation menu, and adds a few preferences that make sense for a travel
planner: an appearance (theme) picker and a default currency for new trips.

## 2. Functional requirements

- **FR-01** Settings are presented as a left-side menu with content panes on the
  right: Profile, Appearance, Travel, Security.
- **FR-02** The page reuses the dashboard navbar (brand, theme toggle, language
  switch, username badge, logout) instead of the auth layout.
- **FR-03** The Profile pane shows the username (read-only) and lets the user
  edit email; language and currency move to their respective panes.
- **FR-04** The Appearance pane offers a Light / Dark / System segmented control
  (persisted to `localStorage.theme`, device-specific).
- **FR-05** The Travel pane offers a default currency selector (same list as the
  trip form, from `/api/currencies`) used as the initial currency for new trips.
- **FR-06** The Security pane keeps the change-password form and the Danger Zone
  (account deletion).
- **FR-07** The selected pane is reflected in the URL hash (`#profile`,
  `#appearance`, `#travel`, `#security`) so refreshes preserve the current pane.
- **FR-08** On small screens the left menu collapses to a horizontal pill strip.

## 3. API endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `GET` | `/api/user/profile` | Bearer | Add `currency` to the response |
| `PUT` | `/api/user/profile` | Bearer | Accept optional `currency` (3-letter code) |

## 4. Data model / DB changes

- `users` table gains `currency` (string, default `"PLN"`).

## 5. UI requirements

- Rewrite `account.html` to use the dashboard navbar and a `.settings-layout`
  grid: `.settings-nav` (left) + `.settings-panel` (right).
- New i18n keys in `en.json`, `pl.json`, `es.json` for the nav labels, theme
  options, currency label, and section descriptions.
- Bump cache-bust query params (`?v=5`) for the changed CSS/JS.

## 6. Non-functional requirements

- Theme selection validates against `light|dark|system`.
- Currency is normalized to an uppercase 3-letter code; invalid values fall back
  to `"PLN"`.
- Language remains `pl|en|es` only.
- Existing profile/currency behaviour must not break the dashboard's trip form.

## 7. Out of scope

- Editable username.
- Date format preferences.
- Email reminders (spec 007).
- Storing theme server-side (remains device-local).
