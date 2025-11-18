# AI Integration Brainstorm and Architecture

## Overview
AI integration provides feedback on student answers by analyzing question text, correct answer, and student response. Analysis occurs per question after full paper submission, then aggregates into overall paper feedback.

## Data Sending to AI
- **Trigger**: On paper submit (all answers saved).
- **Data Collection**: For each question in the paper:
  - Question text
  - Correct answer (option text for MCQ, text for essay)
  - Student answer (selected option or typed text)
- **Format**: JSON payload per question or batch array.
  - Example: `{"question": "What is 2+2?", "correctAnswer": "4", "studentAnswer": "4"}`
- **API Call**: POST to AI service endpoint, async if needed.
- **Response**: JSON with `feedback`, `marks`, `lessonsToReview`.
- **Storage**: Save AIAnalysis per answer, then aggregate for OverallPaperAnalysis.

## Subject-Specific AI Models
To tailor analysis (e.g., chemistry papers use chemistry-trained model), options:

### 1. General AI with Context
- Use one model (e.g., GPT-4), include subject in prompt: "Analyze this chemistry question..."
- Pros: Simple, no extra models.
- Cons: Less accurate for specialized subjects.

### 2. Fine-Tuned Models
- Fine-tune base model (e.g., GPT-3.5) per subject using domain data.
- Pros: High accuracy.
- Cons: Costly, requires training data, maintenance.

### 3. Multiple Endpoints/Models
- Deploy separate models/endpoints per subject (e.g., /ai/chemistry, /ai/math).
- Route based on PaperBundle.subjectId.
- Pros: Optimized per subject.
- Cons: Complexity, scaling.

### Industry Approaches
- **Platforms**: OpenAI (fine-tune), Anthropic Claude, Google PaLM.
- **Cloud ML**: AWS SageMaker, Azure ML, GCP Vertex AI for custom models.
- **Workflow Automation**: n8n/Zapier for orchestration (e.g., trigger AI on submit, process results).
- **Microservices**: Separate AI service (e.g., FastAPI/Python) called from Spring Boot.
- **Async Processing**: Use queues (RabbitMQ, Kafka) for AI calls to avoid blocking UI.

## Recommended Integration
- **Architecture**: Spring Boot service calls external AI API (e.g., OpenAI REST).
- **Routing**: If subject-specific, map subjectId to model/endpoint.
- **Fallback**: General model if subject model unavailable.
- **Async**: Use @Async or WebClient for non-blocking calls.
- **Error Handling**: Retry on failure, default feedback if AI down.
- **Cost**: Monitor API usage, cache common analyses.
- **Training**: Start with general, add fine-tuning later via admin-uploaded data.

## Workflow Example (n8n-like)
1. Paper submitted → Spring event triggers.
2. Collect data → Send to AI workflow.
3. AI processes → Returns results → Store in DB → Notify student.