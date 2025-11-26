# Bundle Purchase Flow Specification

## User Story: Public Bundle Browsing
As a public user (or student who hasn't purchased),
I want to see a list of paper bundles with their prices and descriptions,
So that I can decide what to buy.

### Scenario: Viewing all bundles
Given I am a user
When I request the list of paper bundles
Then I should see a list of bundles
And each bundle should show:
  - Name
  - Description
  - Price
  - Type
  - Exam Type
And the response should NOT contain:
  - List of papers
  - List of questions

## User Story: Bundle Purchase
As a student,
I want to purchase a bundle,
So that I can access its papers and questions.

### Scenario: Purchasing a bundle
Given I am a logged-in student
And I have a valid payment method (mocked)
When I purchase a bundle with ID 1
Then I should receive a success confirmation
And I should have access to bundle 1

## User Story: Accessing Purchased Content
As a student who owns a bundle,
I want to see the papers in that bundle,
So that I can attempt them.

### Scenario: Viewing a purchased bundle
Given I am a logged-in student
And I have purchased bundle 1
When I request the details of bundle 1
Then I should see the bundle metadata
And I should see a list of papers in the bundle
And each paper should show:
  - Name
  - Description
  - Max free attempts

### Scenario: Viewing a non-purchased bundle
Given I am a logged-in student
And I have NOT purchased bundle 2
When I request the details of bundle 2
Then I should receive a 403 Forbidden error OR a summary view without papers

## User Story: Attempting a Paper
As a student who owns a bundle,
I want to attempt a paper,
So that I can see the questions and answer them.

### Scenario: Starting a paper attempt
Given I am a logged-in student
And I have purchased bundle 1
And bundle 1 contains paper 10
When I request to attempt paper 10
Then I should see the list of questions for paper 10
