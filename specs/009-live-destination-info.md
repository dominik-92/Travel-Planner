# 009 — Live Destination Information (Weather & Currency APIs)

> **Status**: Proposed

## 1. Overview & motivation

Destination info (`weather`, `currency`, `tips`) is hardcoded in
`TripService.buildDestinationInfo` for a handful of destinations and falls back
to a generic placeholder for everything else. This spec replaces the hardcoded
switch with live data from external APIs, keeping the cached database data as a
fallback.

## 2. Functional requirements

- **FR-01** Loading destination info for a trip fetches live weather and
  currency info for the destination/country.
- **FR-02** Currency info continues to use the existing NBP integration
  (`CurrencyService`) where applicable, extended with a broader provider if
  needed.
- **FR-03** Weather is fetched from an external weather API using the
  destination (and optional date range when the trip is within the forecast
  window).
- **FR-04** Results are cached with a TTL to limit external API usage.
- **FR-05** On external API failure, the system falls back gracefully to the
  current default/placeholder info rather than erroring.

## 3. API endpoints

- No change to the existing `POST /api/trips/{tripId}/destination-info`
  contract; it now returns richer, live-backed `DestinationInfo`.
- Optional new endpoint `GET /api/destinations/{query}/info` for on-demand
  lookup in the UI.

## 4. Data model / DB changes

- `DestinationInfo` embeddable may be extended with new fields (e.g.
  `weatherTempC`, `weatherIcon`, `weatherUpdatedAt`, `currencyRate`,
  `currencyUpdatedAt`) — kept nullable for backward compatibility.
- API keys are provided via `application.properties` / environment variables,
  never hardcoded.

## 5. UI requirements

- Destination tab shows the weather value with temperature and updated-at
  timestamp, plus a "refresh" action.
- Preserve existing fields so the current rendering keeps working.

## 6. Non-functional requirements

- External calls use the existing `RestTemplate` with timeouts.
- Implement a caching layer (e.g. Spring Cache) with configurable TTL.
- API keys stored securely (env vars), not committed.
- Graceful degradation on rate limits or outages.

## 7. Out of scope

- Full multi-day forecast display.
- Geocoding of arbitrary destinations (may reuse the countries list).
- Hotel/attraction recommendations.
