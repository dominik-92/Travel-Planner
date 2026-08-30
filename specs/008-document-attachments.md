# 008 — Document Attachments

> **Status**: Proposed

## 1. Overview & motivation

Travelers need to keep tickets, reservations and other documents associated
with a trip. This spec adds file upload/download/delete for trip attachments
with size and type validation.

## 2. Functional requirements

- **FR-01** A user can upload a file (e.g. PDF, image, ticket) to a trip, with
  an optional label.
- **FR-02** A user can list attachments for a trip.
- **FR-03** A user can download an attachment.
- **FR-04** A user can delete an attachment.
- **FR-05** Attachments are scoped to the trip owner and cascade-deleted with
  the trip.

## 3. API endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `POST` | `/api/trips/{tripId}/attachments` | Bearer | Upload (multipart, field `file`, optional `label`) |
| `GET` | `/api/trips/{tripId}/attachments` | Bearer | List metadata |
| `GET` | `/api/trips/{tripId}/attachments/{attachmentId}` | Bearer | Download file |
| `DELETE` | `/api/trips/{tripId}/attachments/{attachmentId}` | Bearer | Delete |

## 4. Data model / DB changes

- New entity `Attachment` (`attachments` table):
  - `id` (String PK)
  - `fileName` (String), `contentType` (String), `size` (long)
  - `storageKey` (String) — filesystem path or object-store key
  - `uploadedAt` (String)
  - `trip_id` (FK, `@ManyToOne` + `@JsonIgnore`)
- `Trip` gains a `@OneToMany` `attachments` list (cascade + orphan removal).

## 5. UI requirements

- "Documents" section on the trip detail view: upload button, list with
  download/delete actions.

## 6. Non-functional requirements

- Files are stored on local filesystem (configurable directory) initially;
  storage location is abstracted behind a `StorageService` for future S3.
- Enforce a max file size (configurable, default 10 MB) and an allow-list of
  content types (PDF, common image formats).
- Generated storage keys are random and never derived from user input.
- Uploads limited to the trip owner.

## 7. Out of scope

- Virus scanning.
- Image thumbnails/previews.
- Versioning of attachments.
