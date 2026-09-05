# 001 — Account & Password Management

> **Status**: In progress

## 1. Overview & motivation

The application currently supports registration, login and language switching
(`AuthController`, `AuthService`). Users have no way to change their password,
recover a lost password, update their profile, or delete their account. This
spec adds self-service account management so users are not locked out when they
forget a password and can exercise control over their own data.

## 2. Functional requirements

- **FR-01** A logged-in user can change their password by providing the current
  password and a new password.
- **FR-02** A user who forgot their password can request a reset link. The link
  is single-use, expires after a configurable period, and is tied to the
  account's email address.
- **FR-03** A user can update their profile email address (with uniqueness
  validation) and preferred language.
- **FR-04** A user can delete their own account. Deleting an account cascades to
  all trips, itinerary items, expenses and packing items owned by that user.
- **FR-05** All password changes invalidate previously issued JWTs (token
  versioning) so a compromised session cannot be reused.

## 3. API endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `PUT` | `/api/user/password` | Bearer | Change password (body: `currentPassword`, `newPassword`) |
| `POST` | `/api/auth/forgot-password` | Public | Request reset (body: `email`) |
| `POST` | `/api/auth/reset-password` | Public | Reset using token (body: `token`, `newPassword`) |
| `PUT` | `/api/user/profile` | Bearer | Update email / language (body: `email`, `language`) |
| `DELETE` | `/api/user` | Bearer | Delete own account |

Success responses follow the existing `Map<String, String>` pattern
(e.g. `{ "message": "..." }`); errors reuse `GlobalExceptionHandler`.

## 4. Data model / DB changes

- `users` table gains:
  - `password_version` (int, default `0`) — increment on every password change.
  - `reset_token` (nullable string), `reset_token_expires_at` (nullable string)
    for the forgot-password flow.
- New table `password_reset_tokens` (alternative to columns) if a separate
  token store is preferred; recommended to keep tokens out of the main entity.

## 5. UI requirements

- Account settings view (new page or modal) reachable from the navbar.
- Change-password form with current/new/confirm fields and i18n labels.
- Forgot-password and reset-password pages (`forgot-password.html`,
  `reset-password.html`) mirroring the existing `login.html` styling.
- Confirmation modal before account deletion using the existing
  `showConfirm` pattern; deletion redirects to `login.html`.

## 6. Non-functional requirements

- Passwords are BCrypt-hashed via the existing `PasswordEncoder` bean.
- Reset tokens are cryptographically random (`SecureRandom`), single-use, and
  expire (default 15 minutes, configurable via `application.properties`).
- All new endpoints validate input and return `400` via `IllegalArgumentException`.
- Language selection for the reset email respects the user's stored language.
- New i18n keys must be added to `en.json`, `pl.json` and `es.json`.

## 7. Out of scope

- Email verification at registration time.
- OAuth2 / third-party identity providers.
- Administrative user management.
