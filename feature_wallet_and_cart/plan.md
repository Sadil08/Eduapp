# Implementation Plan: Wallet & Referral System

This document outlines the technical implementation details for the Wallet and Referral features. All code snippets reflect the *current* working implementation.

## 1. Database Schema

**File**: `backend/scripts/06_wallet_and_referrals.sql`

We extend the `users` table to support wallet balance and referral links, and create a `wallet_transactions` table for audit logs.

```sql
-- Store wallet balance and referral info directly on the user
ALTER TABLE users ADD COLUMN IF NOT EXISTS wallet_balance DECIMAL(19, 2) DEFAULT 0.00;
ALTER TABLE users ADD COLUMN IF NOT EXISTS referral_code VARCHAR(255) UNIQUE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS referred_by_id BIGINT;

-- Foreign key for lifetime attribution
ALTER TABLE users ADD CONSTRAINT fk_referred_by FOREIGN KEY (referred_by_id) REFERENCES users(id);

-- Transaction log
CREATE TABLE IF NOT EXISTS wallet_transactions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    type VARCHAR(50) NOT NULL, -- ENUM: TOP_UP, DEBIT, REFERRAL_CREDIT
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_wallet_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Global settings for dynamic configuration
CREATE TABLE IF NOT EXISTS app_settings (
    settings_key VARCHAR(255) PRIMARY KEY,
    settings_value VARCHAR(255) NOT NULL
);
```

## 2. Backend Implementation (Spring Boot)

### 2.1 User Service (Referral Binding)
**File**: `backend/src/main/java/com/eduapp/backend/service/UserService.java`

Logic to handle referral code during registration. It generates a unique code for the new user and binds them to their referrer if a valid code is provided.

```java
// Logic inside register(RegisterRequest req)
// 1. Handle Lifetime Attribution
String referralCode = req.getReferralCode();
if (referralCode != null && !referralCode.trim().isEmpty()) {
    userRepository.findByReferralCode(referralCode.trim().toUpperCase())
            .ifPresentOrElse(referrer -> {
                logger.info("Binding new user {} to referrer {}", req.getEmail(), referrer.getId());
                user.setReferredBy(referrer); // Permanent Link
            }, () -> logger.warn("Invalid referral code: {}", referralCode));
}

// 2. Generate Unique Code for New User
user.setReferralCode(generateUniqueReferralCode());

return userRepository.save(user);

// Helper method
private String generateUniqueReferralCode() {
    String code;
    do {
        code = java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    } while (userRepository.findByReferralCode(code).isPresent());
    return code;
}
```

### 2.2 Wallet Service (Transactions & Bonuses)
**File**: `backend/src/main/java/com/eduapp/backend/service/WalletService.java`

Handles balance changes. Crucially, it triggers the referral bonus *after* a debit transaction.

```java
@Transactional
public void debit(User user, BigDecimal amount, String description) {
    if (user.getWalletBalance().compareTo(amount) < 0) {
        throw new RuntimeException("Insufficient wallet balance");
    }

    // 1. Deduct Balance
    user.setWalletBalance(user.getWalletBalance().subtract(amount));
    userRepository.save(user);

    // 2. Log Transaction
    walletTransactionRepository.save(new WalletTransaction(user, amount.negate(), WalletTransactionType.DEBIT, description));

    // 3. Trigger Bonus
    processReferralBonus(user, amount);
}

private void processReferralBonus(User user, BigDecimal purchaseAmount) {
    User referrer = user.getReferredBy();
    if (referrer == null) return; // No referrer, no bonus

    // Calculate Bonus
    BigDecimal percentage = getReferralPercentage();
    BigDecimal bonus = purchaseAmount.multiply(percentage).divide(new BigDecimal("100"));

    if (bonus.compareTo(BigDecimal.ZERO) > 0) {
        // Credit Referrer
        referrer.setWalletBalance(referrer.getWalletBalance().add(bonus));
        userRepository.save(referrer);

        // Log Credit
        walletTransactionRepository.save(new WalletTransaction(
            referrer, 
            bonus, 
            WalletTransactionType.REFERRAL_CREDIT, 
            "Referral bonus from " + user.getUsername()
        ));
    }
}
```

## 3. Frontend Implementation (Next.js)

### 3.1 Registration (Input Referral)
**File**: `frontend/src/components/AuthForm.tsx`
**File**: `frontend/src/services/authService.ts`

Adds an optional field to the signup form.

```typescript
// authService.ts
export const register = async (email: string, password: string, name: string, referralCode?: string): Promise<UserResponse> => {
  // Sends referralCode in payload
  const response = await apiClient.post<UserResponse>('/api/auth/register', { email, password, name, referralCode });
  return response.data;
};

// AuthForm.tsx (inside strict mode form)
{isRegister && (
  <Form.Item name="referralCode" rules={[{ pattern: /^[A-Z0-9]{8}$/, message: 'Code must be 8 alphanumeric characters' }]}>
    <Input
      prefix={<span className="text-gray-400 font-bold text-xs mt-1">REF</span>}
      placeholder="Referral Code (Optional)"
      maxLength={8}
      onChange={(e) => form.setFieldValue('referralCode', e.target.value.toUpperCase())}
    />
  </Form.Item>
)}
```

### 3.2 Cart & Checkout
**File**: `frontend/src/app/cart/page.tsx`

Handles the purchase process using the wallet.

```typescript
const handleCheckout = async () => {
    if (items.length === 0) return;
    setProcessing(true);
    try {
        // Simple call, backend handles user identification & debit
        await apiClient.post('/api/purchase/checkout', {}); 
        message.success("Purchase successful! You can now access your bundles.");
        clearCart();
        router.push('/dashboard');
    } catch (error) {
        console.error("Checkout failed", error);
        message.error("Checkout failed. Please try again.");
    } finally {
        setProcessing(false);
    }
};
```

### 3.3 Dashboard Referral Display
**File**: `frontend/src/components/ReferralSection.tsx`

Displays the user's unique code.

```typescript
export default function ReferralSection() {
    const { user } = useAuth();
    // Graceful fallback while code is generating or if missing
    const referralCode = (user as any)?.referralCode || "Generating...";

    // ... copy logic ...
}
```
