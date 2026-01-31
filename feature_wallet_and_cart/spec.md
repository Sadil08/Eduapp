# Specification: Wallet, Cart, and Referral System

## 1. Overview
This feature implements a digital wallet for students to purchase educational bundles, integrated with a shopping cart and a referral system. The goal is to facilitate seamless transactions and incentivize user growth through a "Refer & Earn" mechanism.

## 2. Why?
- **Ease of Purchase**: A wallet system reduces the friction of repeated small transactions.
- **Growth Loop**: The referral system encourages existing users to invite friends by rewarding them with a percentage of their friends' purchases.
- **Lifetime Attribution**: By linking a user permanently to a referrer, we create long-term incentives for promoters.

## 3. Core Features

### 3.1 Digital Wallet
- **Balance Management**: Users hold a balance in their wallet.
- **Top-Up**: Users can add funds to their wallet (simulated integration).
- **Transaction History**: Users can view a log of all credits (top-ups, bonuses) and debits (purchases).

### 3.2 Shopping Cart & Checkout
- **Cart Management**: Add/Remove bundles.
- **Checkout**: Purchase bundles using wallet balance.
- **Validation**: Ensure sufficient funds before processing.

### 3.3 Referral System (Lifetime Attribution)
- **Code Generation**: Every user gets a unique 8-character referral code upon signup.
- **Redemption**: New users enter a referral code *during registration*.
- **Binding**: The new user is permanently linked to the referrer.
- **Rewards**: The referrer earns a percentage (default 0.5%) of *every* purchase made by the referred user, forever.

## 4. User Stories

### Student
1. **View Balance**: "As a student, I want to see my current wallet balance on the dashboard and cart so I know how much I can spend."
2. **Top Up**: "As a student, I want to add funds to my wallet so I can buy bundles."
3. **View History**: "As a student, I want to see a list of my past transactions verify my spending and earnings."
4. **Refer a Friend**: "As a student, I want to copy my unique referral code to share with friends."
5. **Redeem Referral**: "As a new user, I want to enter a friend's referral code when I sign up so I can support them."
6. **Checkout**: "As a student, I want to buy all items in my cart using my wallet balance in one click."

### Admin
1. **Set Commission**: "As an admin, I want to configure the global referral percentage so I can adjust marketing incentives."
