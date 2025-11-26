# EduAPP Improvements - Paper Bundle Logic

## Overview
We have refactored the Paper Bundle and Question retrieval logic to prevent data leakage and implement a proper purchase flow.

## Changes

### 1. DTO Restructuring
We split the `PaperBundleDto` and `PaperDto` into "Summary" and "Detail" versions to control data visibility.

- **`PaperBundleSummaryDto`**: Contains only metadata (title, price, description). Returned by public endpoints.
- **`PaperBundleDetailDto`**: Contains metadata + list of papers. Returned only to users who purchased the bundle.
- **`PaperSummaryDto`**: Contains paper metadata.
- **`PaperDetailDto`**: Contains paper metadata + list of questions. Returned only when starting an attempt.

### 2. Access Control
We implemented checks in `PaperBundleService` and `PaperService` to ensure users can only access content they own.

- **`getBundleDetails(id)`**: Checks `StudentBundleAccess` table.
- **`getPaperAttempt(id)`**: Checks if the user owns the parent bundle.

### 3. New Endpoints
- **`GET /api/paper-bundles`**: Now returns `List<PaperBundleSummaryDto>` (Public).
- **`GET /api/paper-bundles/{id}`**: Returns `PaperBundleDetailDto` (Requires Purchase).
- **`POST /api/paper-bundles/{id}/purchase`**: Purchases a bundle (Creates `StudentBundleAccess`).
- **`GET /api/papers/{id}/attempt`**: Returns `PaperDetailDto` (Requires Purchase).

### 4. Spec Files
Added `specfiles/improvement-1/bundle_purchase_flow.md` defining the new behavior.

## Impact
- **Security**: Unpurchased content is no longer exposed via API.
- **Business Logic**: Enforces the "Buy to View" model.
- **Performance**: Public endpoints are lighter as they don't load nested questions.
