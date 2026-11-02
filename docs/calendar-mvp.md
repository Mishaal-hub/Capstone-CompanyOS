# Calendar MVP — local implementation, 2026-10-21

## Scope

Month/week/day date grids, previous/next/today navigation, date selection, event creation/editing/deletion, categories, status, location, loading/retry/error states, MySQL-compatible JPA persistence, and existing audit logging. Week/day are date-based event lists rather than draggable hourly time grids.

No recurrence, attendees, notifications, external calendar synchronization, or drag-and-drop. No new library dependencies. Existing authentication/security/OTP source code is unchanged.

## Architecture

`Calendar.jsx` → shared `api.js` helper → `CalendarEventController` → `CalendarEventService` / `CalendarEventServiceImpl` → `CalendarEventRepository` → `calendar_events`.

`App.jsx` already contained the local Calendar route before this work; it was left untouched.

## Security and visibility

- Events are shared within the authenticated organization; any active workspace member can create, edit, or delete workspace events in this MVP. Creator-only editing is not implemented.
- Every service operation requires `TenantContext`, an authenticated principal, and an active user whose organization matches the tenant context.
- Every single-event lookup is `findByIdAndOrganizationId`; foreign IDs return 404 for get/update/delete.
- Range queries filter the organization in SQL. Events spanning boundaries are included using `start < to AND end > from` (half-open interval).
- Request DTOs do not expose organization, creator, username, ID, or audit timestamp setters. Spoofed identity fields never override server-derived values.
- Old rows with a NULL organization remain inaccessible. They are not silently assigned to the current user.
- Existing create/update/delete audit logging is retained in the same transaction as the event operation.

## API

Primary compatibility URL: `/api/calendar/events`. Alias: `/api/calendar-events`.

| Method | Path | Result |
| --- | --- | --- |
| GET | `?from=<ISO instant>&to=<ISO instant>` | 200, events overlapping the range |
| GET | `/{id}` | 200, one tenant-owned event; otherwise 404 |
| POST | collection | 201, persisted event |
| PUT | `/{id}` | 200, full editable-field replacement |
| DELETE | `/{id}` | 204; foreign/missing IDs return 404 |

`from` and `to` are required, `to > from`, maximum range 366 days. ISO timestamps must include an offset or Z.

Example body:

```json
{
  "title": "Sprint planning",
  "description": "Calendar MVP review",
  "startTime": "2026-10-21T03:30:00Z",
  "endTime": "2026-10-21T04:30:00Z",
  "timeZone": "Asia/Kolkata",
  "eventType": "MEETING",
  "status": "SCHEDULED",
  "location": "Meeting room"
}
```

Title is required and limited to 150 characters (the existing database width is retained at 255 to avoid shrinking legacy data). Description maximum 5000, location maximum 255. End must be strictly after start. Category: MEETING, DEADLINE, FOCUS, OTHER. Status: SCHEDULED, COMPLETED, CANCELLED. Time zone must be recognized by Java's IANA zone database.

Responses include server-owned `id`, `organizationId`, `createdBy`, `createdAt`, and `updatedAt`; no account/password data is returned.

## Date/time behavior

Event times and audit timestamps use Java Instant, stored/serialized as UTC instants. The UI displays and edits times in the browser's time zone, shown above the form; saving records that browser zone as metadata. Editing from another zone preserves instants unless the user changes the displayed date/time, and updates zone metadata to that editor's zone. Nonexistent daylight-saving local times are rejected; ambiguous fall-back times use the browser's first matching offset. Selecting a particular offset for an ambiguous local time is not part of this MVP.

No all-day or recurring event semantics are implied. Multi-day events are displayed on every overlapping date.

## Important: existing MySQL data — review before normal application startup

This implementation was validated against isolated H2 databases in MySQL mode, not the existing MySQL database. No live MySQL reads, migrations, backfills, or deletes were performed.

The repository currently uses Hibernate `ddl-auto=update`. Normal application startup may apply schema changes automatically. Back up and inspect the actual schema/data before starting the revised backend against an existing database. Prefer reviewing/applying schema changes in a database copy and using `ddl-auto=validate` for a controlled deployment.

New fields include nullable organization_id, created_by, time_zone, status, created_at, and updated_at. Nullable mappings intentionally accommodate legacy rows; the service requires valid tenant/creator/time data for all new records. Existing start_time/end_time columns previously represented LocalDateTime values without a time zone. Do not simply label those old values as UTC.

Migration procedure (not executed):

1. Take a database backup and clone it into an isolated MySQL test database.
2. Inspect `SHOW CREATE TABLE calendar_events` and inventory legacy rows. Review the schema diff, including description expansion to TEXT and the tenant/start-time index.
3. Establish each legacy row's real organization and creator from trusted evidence. The old username field was client-supplied and must NOT be treated as sufficient proof of ownership.
4. Confirm the original local time zone for each old timestamp and convert it once to UTC. Do not invent or mass-default an uncertain zone.
5. Validate end > start, map old category values to the new allowed values, choose status, and populate audit metadata using documented evidence. Leave uncertain rows quarantined, not discarded.
6. Apply a reviewed backfill in the clone, verify time round trips, and test with accounts from two organizations. Only then consider production migration and stricter NOT NULL/database constraints.
7. Review and approve migration separately. No application commit/push or migration is authorized by these notes.

## Reproducible checks

Backend (PowerShell, from backend):

```powershell
mvn.cmd test '-Dspring.mail.host=localhost' '-Dspring.mail.username=test' '-Dspring.mail.password=test' '-Djwt.secret=calendar-test-secret-key-that-is-at-least-32-bytes-long'
```

Tests use H2 URLs declared in the test classes, never the configured MySQL URL. Dummy mail settings are for context startup; no email is sent by the Calendar tests.

Frontend (from frontend):

```powershell
npm.cmd run test:calendar
$env:TZ='America/New_York'; npm.cmd run test:calendar; Remove-Item Env:TZ
npm.cmd run build
npx.cmd eslint src/pages/Calendar.jsx src/api.js
```

`VITE_API_URL` can point a development build at an isolated API, e.g. `http://localhost:18080/api`; default remains `http://localhost:8080/api`. Google OAuth's existing Login.jsx redirect is unchanged and is not part of this configurable Calendar preview.

Tests cover actual HTTP/JWT authorization, spoofed identity, workspace sharing, cross-tenant access/mutation denial, CRUD/audit persistence, boundary overlap, invalid payloads/ranges/zones, legacy quarantine, UTC/offset round trips, and existing password login. Date tests cover month/year/leap boundaries and DST behavior. A real-browser smoke test used a temporary H2-backed server and a synthetic verified account to exercise normal password login, event creation/editing/deletion, and month/week/day rendering. This is not a live MySQL or comprehensive browser-automation suite.
