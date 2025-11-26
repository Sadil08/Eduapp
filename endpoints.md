API Endpoints Documentation
Authentication
Note: All protected endpoints use JWT authentication via Authorization header with Bearer token.

Paper Bundles
1. Get All Bundles (Public)
Endpoint: GET /api/paper-bundles
Description: Returns a list of all available paper bundles.
Response: List<PaperBundleSummaryDto>
[
  {
    "id": 1,
    "name": "Math Bundle 2024",
    "description": "All math papers for 2024",
    "price": 100.0
  }
]
2. Get Bundle Details (Protected)
Endpoint: GET /api/paper-bundles/{id}
Headers: Authorization: Bearer {jwt_token}
Description: Returns details of a specific bundle.
Response: PaperBundleDetailDto
{
  "id": 1,
  "name": "Math Bundle 2024",
  "description": "All math papers for 2024",
  "price": 100.0,
  "papers": [
    { "id": 101, "name": "Math Paper 1", "description": "..." }
  ]
}
3. Purchase Bundle
Endpoint: POST /api/paper-bundles/{id}/purchase
Headers: Authorization: Bearer {jwt_token}
Description: Records a purchase for the user.
Response: 200 OK
Student Dashboard
4. Get My Bundles
Endpoint: GET /api/student-bundle-accesses/my-bundles
Headers: Authorization: Bearer {jwt_token}
Description: Returns all bundles purchased by the user.
Response: List<StudentBundleAccess>
[
  {
    "id": 5,
    "student": { "id": 1, "username": "student1" },
    "bundle": { "id": 1, "name": "Math Bundle 2024", ... },
    "purchasedAt": "2024-11-25T10:00:00"
  }
]
Papers & Attempts
5. Attempt Paper
Endpoint: GET /api/papers/{id}/attempt
Headers: Authorization: Bearer {jwt_token}
Description: Returns paper questions for attempting. Hides correct answers.
Response: 
PaperAttemptDto
{
  "id": 101,
  "name": "Math Paper 1",
  "questions": [
    {
      "id": 201,
      "text": "What is 2+2?",
      "type": "MCQ",
      "marks": 5,
      "options": [
        { "id": 301, "text": "3" },
        { "id": 302, "text": "4" }
      ]
    }
  ]
}
6. Submit Paper
Endpoint: POST /api/papers/{id}/submit
Headers: Authorization: Bearer {jwt_token}
Description: Submits answers and triggers AI analysis. Returns the created attempt with basic info (AI analysis pending).
Request: 
PaperSubmissionDto
{
  "timeTakenMinutes": 45,
  "answers": [
    {
      "questionId": 201,
      "answerText": "4",
      "selectedOptionId": 302
    }
  ]
}
Response: StudentPaperAttemptDto
{
  "id": 789,
  "studentId": 42,
  "paperId": 101,
  "attemptNumber": 1,
  "status": "SUBMITTED",
  "startedAt": "2025-11-25T14:00:00",
  "completedAt": "2025-11-25T14:45:00",
  "timeTakenMinutes": 45,
  "overallFeedback": null,
  "totalMarks": null,
  "answers": [
    {
      "id": 1501,
      "attemptId": 789,
      "questionId": 201,
      "questionText": "What is 2+2?",
      "answerText": "4",
      "selectedOptionId": 302,
      "submittedAt": "2025-11-25T14:45:00",
      "marksAwarded": null,
      "marksAvailable": 5,
      "aiFeedback": null
    }
  ]
}

7. Get Attempt Results
Endpoint: GET /api/papers/attempts/{attemptId}
Headers: Authorization: Bearer {jwt_token}
Description: Retrieves paper attempt results with AI analysis. Students can only access their own attempts.
Response: StudentPaperAttemptDto
{
  "id": 789,
  "studentId": 42,
  "paperId": 101,
  "attemptNumber": 1,
  "status": "SUBMITTED",
  "startedAt": "2025-11-25T14:00:00",
  "completedAt": "2025-11-25T14:45:00",
  "timeTakenMinutes": 45,
  "overallFeedback": "Good work! You got the answer correct.",
  "totalMarks": 5,
  "answers": [
    {
      "id": 1501,
      "attemptId": 789,
      "questionId": 201,
      "questionText": "What is 2+2?",
      "answerText": "4",
      "selectedOptionId": 302,
      "submittedAt": "2025-11-25T14:45:00",
      "marksAwarded": 5,
      "marksAvailable": 5,
      "aiFeedback": "Perfect! Correct answer."
    }
  ]
}

Leaderboard
8. Opt-in to Leaderboard
Endpoint: POST /api/leaderboard/opt-in
Headers: Authorization: Bearer {jwt_token}
Description: Allows a student to show their result on the leaderboard for a specific attempt.
Request:
{
  "attemptId": 501
}
Response: 200 OK
9. Get Leaderboard
Endpoint: GET /api/leaderboard/paper/{paperId}
Description: Returns the leaderboard for a specific paper.
Response: List<Map<String, Object>>
[
  {
    "studentName": "student1",
    "marks": 95,
    "timeTaken": 40
  },
  {
    "studentName": "student2",
    "marks": 90,
    "timeTaken": 45
  }
]
