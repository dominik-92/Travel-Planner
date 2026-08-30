# 007 — Email Reminders for Upcoming Trips

> **Status**: Proposed

## 1. Overview & motivation

Users forget about upcoming departures. This spec adds opt-in email reminders
so the system notifies a user a configurable number of days before a trip
starts. Email delivery requires SMTP configuration and a scheduled job.

## 2. Functional requirements

- **FR-01** A user can enable or disable reminders per trip, and choose the
  number of days before the start date to be reminded.
- **FR-02** A scheduled job runs daily and sends an email to users whose
  upcoming trips match the reminder window and have not already been notified
  for that trip.
- **FR-03** Each reminder is sent at most once per trip.
- **FR-04** Email content respects the user's preferred language (PL/EN/ES).
- **FR-05** Failures are logged and do not block the scheduled run.

## 3. API endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `PUT` | `/api/trips/{tripId}/reminder` | Bearer | Update reminder settings (body: `enabled`, `daysBefore`) |

No public endpoint is needed to trigger sending; it is driven by the scheduler.

## 4. Data model / DB changes

- `trips` table gains:
  - `reminder_enabled` (boolean, default false)
  - `reminder_days_before` (int, default 7)
  - `reminder_sent` (boolean, default false)
- New `application.properties` keys for SMTP (`spring.mail.*`) and a
  `app.reminder.cron` schedule.

## 5. UI requirements

- Reminder toggle and "days before" selector on the trip detail view.
- No dedicated email UI is required beyond configuration.

## 6. Non-functional requirements

- Use Spring's `JavaMailSender` and `@Scheduled`/`@EnableScheduling`.
- Reminder sending must be idempotent (track `reminder_sent`).
- Emails are sent asynchronously or in a bounded batch to avoid blocking.
- SMTP settings are environment-configurable (never hardcoded credentials).
- i18n email templates for `en`, `pl`, `es`.

## 7. Out of scope

- SMS / push notifications.
- In-app notification center.
- Reminder rescheduling based on edited dates (may be a follow-up).
