# Constitution: Wallet & Cart Feature

## 1. Architectural Principles

### Backend (Spring Boot)
- **Layered Architecture**: Strictly follow `Controller -> Service -> Repository` pattern.
  - **Controllers**: Handle HTTP requests, validation, and response formatting. No business logic.
  - **Services**: Contain all business logic, transaction management (`@Transactional`), and cross-service orchestration.
  - **Repositories**: Handle data access using Spring Data JPA.
- **DTOs (Data Transfer Objects)**: Use DTOs for data exchange between frontend and backend to decouple internal models from API contracts.
- **Dependency Injection**: Use Constructor Injection for all dependencies to ensure immutability and testability.

### Frontend (Next.js + React)
- **Component Composition**: Build pages using small, reusable components (e.g., `WalletBalanceCard`, `TransactionHistory`).
- **Service Layer**: Centralize API calls in `src/services/` (e.g., `walletService.ts`) to keep components clean of infrastructure concerns.
- **Context API**: Use React Context (e.g., `AuthContext`, `CartContext`) for global state management.
- **Type Safety**: Strictly use TypeScript interfaces for all props and API responses.

## 2. Security Standards
- **Authentication**: All protected endpoints must verify the JWT token via `JwtAuthenticationFilter` and `JwtUtil`.
- **Authorization**: Use `@PreAuthorize` to restrict access based on roles (`ADMIN`, `STUDENT`).
- **Input Validation**: Validate all incoming DTOs (not null, correct formats).
- **Data Integrity**: Use `@Transactional` on all service methods that modify data (e.g., top-ups, purchases) to ensure atomicity.

## 3. Coding Guidelines
- **Naming Conventions**:
  - Classes: PascalCase (e.g., `WalletService`)
  - Methods/Variables: camelCase (e.g., `processReferralBonus`)
  - Constants: UPPER_SNAKE_CASE (e.g., `DEFAULT_REFERRAL_PERCENTAGE`)
- **Error Handling**: Use distinct exceptions and meaningful error messages. Log warnings/errors appropriately.
- **Comments**: Document complex business logic (e.g., referral attribution rules) directly in the code.

## 4. Feature Specific Rules
- **Referral Integrity**: 
  - A user can only be referred ONCE (Lifetime Attribution).
  - Self-referral is strictly prohibited.
- **Wallet Consistency**: 
  - Wallet balance cannot be negative.
  - All balance changes must be recorded in `wallet_transactions`.
