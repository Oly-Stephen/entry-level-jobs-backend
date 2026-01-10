# Frontend Filtering & Location Search Guide

This service exposes two HTTP endpoints that power the filtering UI: the paginated job feed and the location autocomplete. This guide explains how to call them, what the responses look like, and how to present the entry-level specific messaging returned by the backend.

## `GET /api/jobs`

Retrieves a paginated list of jobs. Supports optional keyword and location filters that can be combined in a single request.

| Query Param | Type    | Default | Notes                                                                  |
| ----------- | ------- | ------- | ---------------------------------------------------------------------- |
| `page`      | integer | `0`     | Zero-based page index. Must be `>= 0`.                                 |
| `size`      | integer | `10`    | Page size clamped to `[1, 100]`.                                       |
| `keyword`   | string  | `null`  | Case-insensitive substring match on job titles. Trimmed on the server. |
| `location`  | string  | `null`  | Case-insensitive substring match on locations. Trimmed on the server.  |

### Response Shape

```json
{
  "success": true,
  "message": "Showing entry-level opportunities in Berlin matching \"product\".",
  "data": [
    {
      "id": 123,
      "title": "Junior Product Analyst",
      "company": "Acme",
      "location": "Berlin, Germany",
      "url": "https://jobs.example.com/123",
      "description": "…",
      "source": "Remotive",
      "postedAt": "2025-12-30T14:02:11",
      "createdAt": "2025-12-30T16:55:27"
    }
  ],
  "pagination": {
    "page": 0,
    "size": 10,
    "totalPages": 3,
    "totalElements": 21,
    "hasNext": true,
    "hasPrevious": false
  }
}
```

#### Sample payloads

```http
GET /api/jobs?page=1&size=5&keyword=product&location=berlin
```

```json
{
  "success": true,
  "message": "Showing entry-level opportunities in Berlin matching \"product\".",
  "data": [
    {
      "id": 412,
      "title": "Junior Product Analyst",
      "company": "Acme",
      "location": "Berlin, Germany",
      "url": "https://jobs.example.com/412",
      "description": "You will support senior PMs with backlog grooming…",
      "source": "Remotive",
      "postedAt": "2025-12-30T14:02:11",
      "createdAt": "2025-12-30T16:55:27"
    }
  ],
  "pagination": {
    "page": 1,
    "size": 5,
    "totalPages": 4,
    "totalElements": 18,
    "hasNext": true,
    "hasPrevious": true
  }
}
```

```http
GET /api/jobs?location=accra
```

```json
{
  "success": true,
  "message": "We couldn't find entry-level opportunities in Accra yet. Try remote-friendly or nearby locations.",
  "data": [],
  "pagination": {
    "page": 0,
    "size": 10,
    "totalPages": 0,
    "totalElements": 0,
    "hasNext": false,
    "hasPrevious": false
  }
}
```

#### `message` behavior

- When `location` is provided and results exist, the backend emits a positive confirmation message (e.g., `Showing entry-level opportunities in Lagos.`).
- When `location` is provided and zero matches exist across all pages, the message becomes a gentle nudge toward nearby or remote options (e.g., `We couldn't find entry-level opportunities in Accra yet. Try remote-friendly or nearby locations.`).
- If no location filter is sent, `message` will be `null` unless another part of the request triggers an error.

Display this message near the results list so users immediately understand why a grid is empty. The copy is already "entry-level" aware, so plain rendering is fine.

### Combined filters

You can send `keyword` and `location` together, and the backend will apply both in one query. There is no need for the frontend to orchestrate multiple requests.

Example request:

```
GET /api/jobs?keyword=designer&location=remote&page=0&size=20
```

## `GET /api/jobs/locations`

Drives the location search/autocomplete UI. The backend returns both the modern `options` array (recommended) and a legacy `locations` string list for backward compatibility.

| Query Param | Type    | Default | Notes                                                                                  |
| ----------- | ------- | ------- | -------------------------------------------------------------------------------------- |
| `query`     | string  | `null`  | Optional substring filter. If omitted, the backend returns the most popular locations. |
| `limit`     | integer | `10`    | Clamped to `[1, 50]`. Controls how many suggestions come back.                         |

### Response Shape

```json
{
  "success": true,
  "query": "lag",
  "options": [
    { "value": "Lagos, Nigeria", "label": "Lagos, Nigeria", "job_count": 12 },
    { "value": "Lagos, Portugal", "label": "Lagos, Portugal", "job_count": 3 }
  ],
  "locations": ["Lagos, Nigeria", "Lagos, Portugal"],
  "returned": 2,
  "total_matches": 2,
  "message": "Select a location to narrow entry-level opportunities."
}
```

#### Sample payloads

```http
GET /api/jobs/locations?query=lag&limit=5
```

```json
{
  "success": true,
  "query": "lag",
  "options": [
    { "value": "Lagos, Nigeria", "label": "Lagos, Nigeria", "job_count": 12 },
    { "value": "Lagos, Portugal", "label": "Lagos, Portugal", "job_count": 3 }
  ],
  "locations": ["Lagos, Nigeria", "Lagos, Portugal"],
  "returned": 2,
  "total_matches": 2,
  "message": "Select a location to narrow entry-level opportunities."
}
```

```http
GET /api/jobs/locations?query=accra
```

```json
{
  "success": true,
  "query": "accra",
  "options": [],
  "locations": [],
  "returned": 0,
  "total_matches": 0,
  "message": "We haven't indexed entry-level roles for that location yet. Try a nearby city or remote filter."
}
```

When no locations match the query the backend still returns `success: true` but all arrays are empty and the message switches to `"We haven't indexed entry-level roles for that location yet. Try a nearby city or remote filter."`. Use that copy directly in the UI for consistency.

### Implementation tips

- Bind suggestions to `options`. Each option already includes a display label and a count you can surface in the dropdown (e.g., `Berlin · 18 roles`).
- If you need to keep legacy flows working, continue reading from `locations`. It mirrors the `options.value` values.
- The backend trims whitespace before searching, so you can send the user's raw input.

## Suggested UX flow

1. On page load, fetch `GET /api/jobs` without filters to show the latest entry-level roles.
2. As the user types into the location field (after a short debounce), call `GET /api/jobs/locations?query=typedText` and populate your dropdown with `options`.
3. When the user selects a location (and optionally a keyword), request `GET /api/jobs` with both filters. Render the returned `message` inline with the results.
4. If the response has `success=false`, display `error` from the payload and provide a retry option.

Following this contract keeps the frontend and backend aligned while providing consistent, entry-level specific guidance to job seekers.
