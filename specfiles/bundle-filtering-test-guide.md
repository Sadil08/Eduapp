# Bundle Filtering - Quick Test Guide

This guide provides quick curl commands to test the new bundle filtering endpoints.

## Prerequisites

Make sure the backend server is running:
```bash
cd /home/sadil/Desktop/EduAPP/backend
./mvnw spring-boot:run
```

## Test Commands

### 1. Get All Bundles (No Filters)
```bash
curl -X GET "http://localhost:8080/api/paper-bundles/filter"
```

### 2. Filter by Paper Type

**Get MCQ bundles:**
```bash
curl -X GET "http://localhost:8080/api/paper-bundles/filter?type=MCQ"
```

**Get Essay bundles:**
```bash
curl -X GET "http://localhost:8080/api/paper-bundles/filter?type=ESSAY"
```

**Get Mixed bundles:**
```bash
curl -X GET "http://localhost:8080/api/paper-bundles/filter?type=MIXED"
```

### 3. Filter by Exam Type

**Get SAT bundles:**
```bash
curl -X GET "http://localhost:8080/api/paper-bundles/filter?examType=SAT"
```

**Get ACT bundles:**
```bash
curl -X GET "http://localhost:8080/api/paper-bundles/filter?examType=ACT"
```

### 4. Filter by Subject

**Get bundles for subject ID 1:**
```bash
curl -X GET "http://localhost:8080/api/paper-bundles/filter?subjectId=1"
```

### 5. Filter by Lesson

**Get bundles for lesson ID 1:**
```bash
curl -X GET "http://localhost:8080/api/paper-bundles/filter?lessonId=1"
```

### 6. Filter by Past Paper Status

**Get only past papers:**
```bash
curl -X GET "http://localhost:8080/api/paper-bundles/filter?isPastPaper=true"
```

**Get only non-past papers:**
```bash
curl -X GET "http://localhost:8080/api/paper-bundles/filter?isPastPaper=false"
```

### 7. Filter by Price Range

**Get bundles under $50:**
```bash
curl -X GET "http://localhost:8080/api/paper-bundles/filter?maxPrice=50"
```

**Get bundles over $25:**
```bash
curl -X GET "http://localhost:8080/api/paper-bundles/filter?minPrice=25"
```

**Get bundles between $25 and $75:**
```bash
curl -X GET "http://localhost:8080/api/paper-bundles/filter?minPrice=25&maxPrice=75"
```

**Get free bundles:**
```bash
curl -X GET "http://localhost:8080/api/paper-bundles/filter?minPrice=0&maxPrice=0"
```

### 8. Search by Name

**Search for "math":**
```bash
curl -X GET "http://localhost:8080/api/paper-bundles/search?name=math"
```

**Search for "SAT":**
```bash
curl -X GET "http://localhost:8080/api/paper-bundles/search?name=SAT"
```

**Search for "physics":**
```bash
curl -X GET "http://localhost:8080/api/paper-bundles/search?name=physics"
```

### 9. Combined Filters

**MCQ bundles for subject 1:**
```bash
curl -X GET "http://localhost:8080/api/paper-bundles/filter?type=MCQ&subjectId=1"
```

**SAT past papers:**
```bash
curl -X GET "http://localhost:8080/api/paper-bundles/filter?examType=SAT&isPastPaper=true"
```

**Affordable MCQ bundles (under $50):**
```bash
curl -X GET "http://localhost:8080/api/paper-bundles/filter?type=MCQ&maxPrice=50"
```

**SAT Math bundles (combining exam type and name search):**
```bash
curl -X GET "http://localhost:8080/api/paper-bundles/filter?examType=SAT&name=math"
```

**Complex filter (MCQ, Subject 1, Past Papers, Under $100):**
```bash
curl -X GET "http://localhost:8080/api/paper-bundles/filter?type=MCQ&subjectId=1&isPastPaper=true&maxPrice=100"
```

### 10. Pretty Print JSON (with jq)

If you have `jq` installed, you can format the output:

```bash
curl -X GET "http://localhost:8080/api/paper-bundles/filter?type=MCQ" | jq '.'
```

## Testing with Postman

Import these as Postman requests:

1. **Filter Endpoint:**
   - Method: GET
   - URL: `http://localhost:8080/api/paper-bundles/filter`
   - Params: Add any combination of: type, examType, subjectId, lessonId, isPastPaper, minPrice, maxPrice, name

2. **Search Endpoint:**
   - Method: GET
   - URL: `http://localhost:8080/api/paper-bundles/search`
   - Params: name (required)

## Expected Response Format

All endpoints return JSON array:

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
  }
]
```

## Troubleshooting

### Empty Array Response `[]`
- This is normal if no bundles match the filter criteria
- Try removing some filters to get results

### 400 Bad Request
- Check that parameter names are spelled correctly
- Ensure `type` values are: MCQ, ESSAY, or MIXED (case-sensitive)
- Ensure boolean values are: true or false (lowercase)

### 500 Internal Server Error
- Check server logs for detailed error messages
- Ensure database is running and accessible
- Verify all required services are started

## Next Steps

After testing with curl, integrate these endpoints into your frontend application using the examples in [bundle-filtering-api.md](file:///home/sadil/Desktop/EduAPP/backend/specfiles/bundle-filtering-api.md).
