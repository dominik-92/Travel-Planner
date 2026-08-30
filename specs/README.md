# Specs

This directory holds feature specifications, written before implementation as
part of a spec-driven development workflow: define the behaviour and API first,
review it, then implement against the spec.

## Workflow

1. **Propose** — copy `000-template.md` to `NNN-slug.md` with the next
   sequential number and fill it in.
2. **Review** — get agreement on requirements, endpoints, and data model before
   writing code.
3. **Implement** — build the feature to match the spec.
4. **Close** — update the status to `Done` and reference the PR/commit.

## Statuses

- `Proposed` — drafted, not yet approved or implemented.
- `Approved` — reviewed and ready to implement.
- `In progress` — implementation underway.
- `Done` — implemented and shipped.

## Numbering & naming

Specs are numbered sequentially with zero padding and a short slug:

```
001-account-management.md
002-trip-editing.md
```

## Structure

Every spec follows the template in `000-template.md`:

1. Overview & motivation
2. Functional requirements (`FR-*`)
3. API endpoints
4. Data model / DB changes
5. UI requirements
6. Non-functional requirements
7. Out of scope

## Index

| # | Spec |
|---|------|
| 001 | [Account & password management](001-account-management.md) |
| 002 | [Trip editing (full CRUD)](002-trip-editing.md) |
| 003 | [Expense analytics & category breakdown](003-expense-analytics.md) |
| 004 | [Dashboard search, sort & filter](004-dashboard-search-filter.md) |
| 005 | [Packing checklist](005-packing-checklist.md) |
| 006 | [Collaborative trip sharing](006-trip-sharing.md) |
| 007 | [Email reminders for upcoming trips](007-email-reminders.md) |
| 008 | [Document attachments](008-document-attachments.md) |
| 009 | [Live destination information](009-live-destination-info.md) |
| 010 | [API documentation + import/export](010-api-docs-import-export.md) |
