# Bundle Filtering API Documentation

## Overview

This document describes the new bundle filtering and search endpoints added to the EduApp backend API. These endpoints allow users to filter and search for paper bundles based on various criteria.

## Base URL

```
http://localhost:8080/api/paper-bundles
```

## Endpoints

### 1. Filter Bundles

Filter bundles by multiple criteria. All parameters are optional and can be combined.

**Endpoint:** `GET /api/paper-bundles/filter`

**Authentication:** Not required (public endpoint)

**Query Parameters:**

| Parameter | Type | Required | Description | Example Values |
|-----------|------|----------|-------------|----------------|
| `type` | String (Enum) | No | Paper type | `MCQ`, `ESSAY`, `MIXED` |
| `examType` | String | No | Exam type | `SAT`, `ACT`, `GCSE`, `AP` |
| `subjectId` | Long | No | Subject ID | `1`, `2`, `3` |
| `lessonId` | Long | No | Lesson ID | `1`, `2`, `3` |
| `isPastPaper` | Boolean | No | Past paper status | `true`, `false` |
| `minPrice` | BigDecimal | No | Minimum price | `0`, `10.00`, `25.50` |
| `maxPrice` | BigDecimal | No | Maximum price | `50.00`, `100.00` |
| `name` | String | No | Name search term (case-insensitive) | `math`, `SAT`, `physics` |

**Response:** `200 OK`

Returns an array of `PaperBundleSummaryDto` objects.

**Response Schema:**

```json
[
  {
    "id": "number",
    "name": "string",
    "description": "string",
    "price": "number",
    "type": "string (MCQ|ESSAY|MIXED)",
    "examType": "string",
    "subjectId": "number",
    "lessonId": "number",
    "isPastPaper": "boolean"
  }
]
```

**Example Requests:**

```bash
# Filter by paper type
curl "http://localhost:8080/api/paper-bundles/filter?type=MCQ"

# Filter by subject and exam type
curl "http://localhost:8080/api/paper-bundles/filter?subjectId=1&examType=SAT"

# Filter by price range
curl "http://localhost:8080/api/paper-bundles/filter?minPrice=0&maxPrice=50"

# Filter past papers only
curl "http://localhost:8080/api/paper-bundles/filter?isPastPaper=true"

# Combined filters
curl "http://localhost:8080/api/paper-bundles/filter?type=MCQ&subjectId=1&isPastPaper=true&maxPrice=100"

# Filter with name search
curl "http://localhost:8080/api/paper-bundles/filter?name=mathematics&type=MCQ"

# Get all bundles (no filters)
curl "http://localhost:8080/api/paper-bundles/filter"
```

**Example Response:**

```json
[
  {
    "id": 1,
    "name": "SAT Mathematics Bundle",
    "description": "Comprehensive SAT math preparation with 50+ practice questions",
    "price": 49.99,
    "type": "MCQ",
    "examType": "SAT",
    "subjectId": 1,
    "lessonId": null,
    "isPastPaper": false
  },
  {
    "id": 2,
    "name": "SAT Math Advanced",
    "description": "Advanced SAT mathematics topics",
    "price": 39.99,
    "type": "MIXED",
    "examType": "SAT",
    "subjectId": 1,
    "lessonId": 5,
    "isPastPaper": false
  }
]
```

---

### 2. Search Bundles by Name

Search for bundles by name using case-insensitive partial matching.

**Endpoint:** `GET /api/paper-bundles/search`

**Authentication:** Not required (public endpoint)

**Query Parameters:**

| Parameter | Type | Required | Description | Example Values |
|-----------|------|----------|-------------|----------------|
| `name` | String | **Yes** | Search term | `math`, `SAT`, `physics` |

**Response:** `200 OK`

Returns an array of `PaperBundleSummaryDto` objects matching the search term.

**Example Requests:**

```bash
# Search for bundles containing "math"
curl "http://localhost:8080/api/paper-bundles/search?name=math"

# Search for bundles containing "SAT"
curl "http://localhost:8080/api/paper-bundles/search?name=SAT"

# Search for bundles containing "physics"
curl "http://localhost:8080/api/paper-bundles/search?name=physics"
```

**Example Response:**

```json
[
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
  },
  {
    "id": 3,
    "name": "Advanced Mathematics",
    "description": "Advanced math topics for college prep",
    "price": 79.99,
    "type": "MIXED",
    "examType": "AP",
    "subjectId": 1,
    "lessonId": 5,
    "isPastPaper": false
  }
]
```

---

## Filter Behavior

### Null Parameters
- All filter parameters are optional
- If a parameter is `null` or not provided, it is ignored in the filter
- If no parameters are provided to `/filter`, all bundles are returned

### Name Search
- Case-insensitive matching
- Partial matching (searches for the term anywhere in the bundle name)
- Example: `name=math` will match "Mathematics", "SAT Math", "Advanced Math Topics"

### Price Range
- `minPrice` filters bundles with price >= specified value
- `maxPrice` filters bundles with price <= specified value
- Both can be used together to create a price range
- If only one is specified, it acts as a lower or upper bound

### Combined Filters
- All filters use AND logic (all conditions must be met)
- Example: `type=MCQ&subjectId=1` returns bundles that are BOTH MCQ type AND belong to subject 1

---

## Error Responses

### 400 Bad Request
Returned when required parameters are missing or invalid.

**Example:**
```json
{
  "error": "Bad Request",
  "message": "Required parameter 'name' is missing"
}
```

### 500 Internal Server Error
Returned when an unexpected error occurs on the server.

**Example:**
```json
{
  "error": "Internal Server Error",
  "message": "An unexpected error occurred"
}
```

---

## Use Cases

### 1. Browse All Bundles
```bash
curl "http://localhost:8080/api/paper-bundles/filter"
```

### 2. Find MCQ Bundles for a Specific Subject
```bash
curl "http://localhost:8080/api/paper-bundles/filter?type=MCQ&subjectId=1"
```

### 3. Find Affordable Bundles (Under $50)
```bash
curl "http://localhost:8080/api/paper-bundles/filter?maxPrice=50"
```

### 4. Find Past Papers for SAT
```bash
curl "http://localhost:8080/api/paper-bundles/filter?examType=SAT&isPastPaper=true"
```

### 5. Search for Math-Related Bundles
```bash
curl "http://localhost:8080/api/paper-bundles/search?name=math"
```

### 6. Find Free or Low-Cost MCQ Bundles
```bash
curl "http://localhost:8080/api/paper-bundles/filter?type=MCQ&minPrice=0&maxPrice=25"
```

### 7. Find Bundles for a Specific Lesson
```bash
curl "http://localhost:8080/api/paper-bundles/filter?lessonId=5"
```

---

## Frontend Integration Examples

### JavaScript/TypeScript

```typescript
// Filter bundles by type and subject
async function filterBundles(type?: string, subjectId?: number) {
  const params = new URLSearchParams();
  if (type) params.append('type', type);
  if (subjectId) params.append('subjectId', subjectId.toString());
  
  const response = await fetch(`/api/paper-bundles/filter?${params}`);
  return await response.json();
}

// Search bundles by name
async function searchBundles(name: string) {
  const response = await fetch(`/api/paper-bundles/search?name=${encodeURIComponent(name)}`);
  return await response.json();
}

// Example usage
const mcqBundles = await filterBundles('MCQ', 1);
const mathBundles = await searchBundles('mathematics');
```

### React Example

```tsx
import { useState, useEffect } from 'react';

interface BundleFilters {
  type?: 'MCQ' | 'ESSAY' | 'MIXED';
  examType?: string;
  subjectId?: number;
  lessonId?: number;
  isPastPaper?: boolean;
  minPrice?: number;
  maxPrice?: number;
  name?: string;
}

function BundleList() {
  const [bundles, setBundles] = useState([]);
  const [filters, setFilters] = useState<BundleFilters>({});

  useEffect(() => {
    const fetchBundles = async () => {
      const params = new URLSearchParams();
      Object.entries(filters).forEach(([key, value]) => {
        if (value !== undefined && value !== null) {
          params.append(key, value.toString());
        }
      });

      const response = await fetch(`/api/paper-bundles/filter?${params}`);
      const data = await response.json();
      setBundles(data);
    };

    fetchBundles();
  }, [filters]);

  return (
    <div>
      {/* Filter UI */}
      <select onChange={(e) => setFilters({...filters, type: e.target.value})}>
        <option value="">All Types</option>
        <option value="MCQ">MCQ</option>
        <option value="ESSAY">Essay</option>
        <option value="MIXED">Mixed</option>
      </select>

      {/* Bundle list */}
      {bundles.map(bundle => (
        <div key={bundle.id}>{bundle.name}</div>
      ))}
    </div>
  );
}
```

---

## Notes

- Both endpoints return the same `PaperBundleSummaryDto` format
- The `/filter` endpoint is more flexible and can replace `/search` if needed
- For simple name searches, `/search` provides a cleaner API
- All endpoints are public and do not require authentication
- Results are not paginated (returns all matching bundles)
- Consider adding pagination for large datasets in the future
