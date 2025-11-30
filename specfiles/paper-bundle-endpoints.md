# Paper Bundle API - Complete Endpoint Reference

## Base URL
```
http://localhost:8080/api/paper-bundles
```

## Endpoints Summary

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/paper-bundles` | Get all bundles (summary) | No |
| GET | `/api/paper-bundles/filter` | Filter bundles by criteria | No |
| GET | `/api/paper-bundles/search` | Search bundles by name | No |
| GET | `/api/paper-bundles/{id}` | Get bundle details | Yes (JWT) |
| POST | `/api/paper-bundles/{id}/purchase` | Purchase a bundle | Yes (JWT) |
| POST | `/api/paper-bundles` | Create new bundle | Yes (Admin) |

---

## 1. Get All Bundles (Summary)

**Endpoint:** `GET /api/paper-bundles`

**Description:** Returns summary of all available bundles

**Authentication:** Not required

**Response:** Array of `PaperBundleSummaryDto`

**Example:**
```bash
curl http://localhost:8080/api/paper-bundles
```

---

## 2. Filter Bundles ⭐ NEW

**Endpoint:** `GET /api/paper-bundles/filter`

**Description:** Filter bundles by multiple criteria (all optional)

**Authentication:** Not required

**Query Parameters:**
- `type` (String) - MCQ, ESSAY, MIXED
- `examType` (String) - SAT, ACT, GCSE, etc.
- `subjectId` (Long) - Subject ID
- `lessonId` (Long) - Lesson ID
- `isPastPaper` (Boolean) - true/false
- `minPrice` (BigDecimal) - Minimum price
- `maxPrice` (BigDecimal) - Maximum price
- `name` (String) - Search term (case-insensitive)

**Response:** Array of `PaperBundleSummaryDto`

**Examples:**
```bash
# Filter by type
curl "http://localhost:8080/api/paper-bundles/filter?type=MCQ"

# Filter by subject and exam type
curl "http://localhost:8080/api/paper-bundles/filter?subjectId=1&examType=SAT"

# Filter by price range
curl "http://localhost:8080/api/paper-bundles/filter?minPrice=0&maxPrice=50"

# Combined filters
curl "http://localhost:8080/api/paper-bundles/filter?type=MCQ&subjectId=1&isPastPaper=true&maxPrice=100"
```

---

## 3. Search Bundles by Name ⭐ NEW

**Endpoint:** `GET /api/paper-bundles/search`

**Description:** Search bundles by name (case-insensitive, partial match)

**Authentication:** Not required

**Query Parameters:**
- `name` (String) - **Required** - Search term

**Response:** Array of `PaperBundleSummaryDto`

**Examples:**
```bash
# Search for math bundles
curl "http://localhost:8080/api/paper-bundles/search?name=math"

# Search for SAT bundles
curl "http://localhost:8080/api/paper-bundles/search?name=SAT"
```

---

## 4. Get Bundle Details

**Endpoint:** `GET /api/paper-bundles/{id}`

**Description:** Get detailed information about a specific bundle (requires purchase)

**Authentication:** Required (JWT token)

**Path Parameters:**
- `id` (Long) - Bundle ID

**Headers:**
- `Authorization: Bearer <token>`

**Response:** `PaperBundleDetailDto`

**Example:**
```bash
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
     http://localhost:8080/api/paper-bundles/1
```

**Error Responses:**
- `403 Forbidden` - Bundle not purchased
- `404 Not Found` - Bundle doesn't exist

---

## 5. Purchase Bundle

**Endpoint:** `POST /api/paper-bundles/{id}/purchase`

**Description:** Purchase a bundle for the authenticated user

**Authentication:** Required (JWT token)

**Path Parameters:**
- `id` (Long) - Bundle ID

**Headers:**
- `Authorization: Bearer <token>`

**Response:** `200 OK` (empty body)

**Example:**
```bash
curl -X POST \
     -H "Authorization: Bearer YOUR_JWT_TOKEN" \
     http://localhost:8080/api/paper-bundles/1/purchase
```

**Error Responses:**
- `400 Bad Request` - Missing/invalid token
- `404 Not Found` - Bundle doesn't exist
- `500 Internal Server Error` - Purchase failed

---

## 6. Create Bundle (Admin Only)

**Endpoint:** `POST /api/paper-bundles`

**Description:** Create a new paper bundle (admin only)

**Authentication:** Required (Admin role)

**Headers:**
- `Authorization: Bearer <admin_token>`
- `Content-Type: application/json`

**Request Body:** `PaperBundleDto`

```json
{
  "name": "SAT Mathematics Bundle",
  "description": "Comprehensive SAT math preparation",
  "price": 49.99,
  "type": "MCQ",
  "examType": "SAT",
  "subjectId": 1,
  "lessonId": null,
  "isPastPaper": false
}
```

**Response:** `201 Created` with created `PaperBundleDto`

**Example:**
```bash
curl -X POST \
     -H "Authorization: Bearer ADMIN_JWT_TOKEN" \
     -H "Content-Type: application/json" \
     -d '{
       "name": "SAT Mathematics Bundle",
       "description": "Comprehensive SAT math preparation",
       "price": 49.99,
       "type": "MCQ",
       "examType": "SAT",
       "subjectId": 1,
       "isPastPaper": false
     }' \
     http://localhost:8080/api/paper-bundles
```

---

## Response DTOs

### PaperBundleSummaryDto
```json
{
  "id": 1,
  "name": "SAT Mathematics Bundle",
  "description": "Comprehensive SAT math preparation",
  "price": 49.99,
  "type": "MCQ",
  "examType": "SAT",
  "subjectId": 1,
  "lessonId": null,
  "isPastPaper": false
}
```

### PaperBundleDetailDto
```json
{
  "id": 1,
  "name": "SAT Mathematics Bundle",
  "description": "Comprehensive SAT math preparation",
  "price": 49.99,
  "type": "MCQ",
  "examType": "SAT",
  "subjectId": 1,
  "lessonId": null,
  "isPastPaper": false,
  "papers": [
    {
      "id": 1,
      "title": "SAT Math Practice 1",
      "description": "First practice paper",
      "totalMarks": 100,
      "durationMinutes": 60
    }
  ]
}
```

---

## Common Use Cases

### 1. Browse All Available Bundles
```bash
GET /api/paper-bundles
```

### 2. Find MCQ Bundles for a Subject
```bash
GET /api/paper-bundles/filter?type=MCQ&subjectId=1
```

### 3. Find Affordable Bundles
```bash
GET /api/paper-bundles/filter?maxPrice=50
```

### 4. Search for Math Bundles
```bash
GET /api/paper-bundles/search?name=math
```

### 5. Get Bundle Details (After Purchase)
```bash
GET /api/paper-bundles/1
# Requires: Authorization header with JWT
```

### 6. Purchase a Bundle
```bash
POST /api/paper-bundles/1/purchase
# Requires: Authorization header with JWT
```

---

## Filter Combinations

The `/filter` endpoint supports combining multiple criteria:

```bash
# MCQ bundles for subject 1, under $50
GET /api/paper-bundles/filter?type=MCQ&subjectId=1&maxPrice=50

# SAT past papers
GET /api/paper-bundles/filter?examType=SAT&isPastPaper=true

# Free or low-cost bundles with "math" in the name
GET /api/paper-bundles/filter?name=math&maxPrice=25

# All filters combined
GET /api/paper-bundles/filter?type=MCQ&examType=SAT&subjectId=1&lessonId=5&isPastPaper=true&minPrice=10&maxPrice=100&name=advanced
```

---

## Error Handling

### Common Error Responses

**400 Bad Request**
```json
{
  "error": "Bad Request",
  "message": "Required parameter 'name' is missing"
}
```

**403 Forbidden**
```json
{
  "error": "Forbidden",
  "message": "Access denied: Bundle not purchased"
}
```

**404 Not Found**
```json
{
  "error": "Not Found",
  "message": "Paper bundle not found"
}
```

**500 Internal Server Error**
```json
{
  "error": "Internal Server Error",
  "message": "An unexpected error occurred"
}
```

---

## Notes

- All public endpoints (`/filter`, `/search`, base `/`) do not require authentication
- Protected endpoints require JWT token in `Authorization: Bearer <token>` header
- Admin endpoints require user to have `ADMIN` role
- All filter parameters are optional and can be combined
- Name search is case-insensitive and supports partial matching
- Empty array `[]` is returned when no bundles match the criteria
