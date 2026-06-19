# ArcOra Production App: Comprehensive Implementation Plan

## 1. Executive Summary
**ArcOra** is a high-end fintech application designed to bring the power of the Arc Network to a mass-market audience. By leveraging Account Abstraction (AA) and the Arc App Kit, it removes all traditional blockchain friction (gas, seed phrases, chain switching), presenting a seamless "Stablecoin-native" financial experience.

---

## 2. Technical Architecture

### 2.1 High-Level Stack
*   **Frontend:** Android (Kotlin, Jetpack Compose)
*   **Architecture:** Clean Architecture + MVVM
*   **DI:** Hilt
*   **Async/State:** Kotlin Coroutines & StateFlow
*   **Backend:** Node.js / NestJS (for off-chain metadata, usernames, and notifications)
*   **Database:** PostgreSQL (User profiles, username mapping, transaction metadata)
*   **Cache/Real-time:** Redis & WebSockets (for real-time payment notifications)
*   **Blockchain Layer:** 
    *   **Arc Network** (Main Execution Layer)
    *   **Arc App Kit** (Core financial primitives)
    *   **Account Abstraction Provider** (Smart account management & Social Login)
    *   **CCTP (Cross-Chain Transfer Protocol)** (Powering the "One-Tap Bridge")

### 2.2 Component Diagram
`User Interface (Compose)` $\rightarrow$ `ViewModel` $\rightarrow$ `UseCases` $\rightarrow$ `Repository` $\rightarrow$ `[Arc App Kit SDK / Backend API]` $\rightarrow$ `Arc Network`

---

## 3. Database Schema (Backend)

### 3.1 `Users` Table
| Column | Type | Description |
| :--- | :--- | :--- |
| `user_id` | UUID (PK) | Internal unique identifier |
| `email` | String (Unique) | User's login email |
| `username` | String (Unique) | The `@username` handle |
| `smart_account_address` | String | The EVM address of the smart account |
| `profile_image_url` | String | Link to user avatar |
| `reputation_score` | Integer | 0-100 score |
| `is_verified` | Boolean | KYC/Verification status |
| `created_at` | Timestamp | Account creation date |

### 3.2 `Transactions` Table (Metadata Layer)
| Column | Type | Description |
| :--- | :--- | :--- |
| `tx_id` | UUID (PK) | Internal TX ID |
| `blockchain_hash` | String | The on-chain transaction hash |
| `sender_id` | UUID (FK) | Reference to Users table |
| `receiver_id` | UUID (FK) | Reference to Users table (if internal) |
| `amount` | Decimal | Amount in USDC |
| `status` | Enum | `PENDING`, `COMPLETED`, `FAILED`, `REJECTED` |
| `type` | Enum | `PAYMENT`, `REQUEST`, `BRIDGE`, `SWAP` |
| `created_at` | Timestamp | Transaction timestamp |

### 3.3 `AI_Agent_Wallets` Table
| Column | Type | Description |
| :--- | :--- | :--- |
| `agent_id` | UUID (PK) | Unique agent ID |
| `owner_id` | UUID (FK) | User who owns the agent |
| `agent_address` | String | The agent's smart account address |
| `monthly_budget` | Decimal | Spending limit for the agent |
| `permissions` | JSON | Allowed transaction types/contracts |

---

## 4. API Design (REST/WebSocket)

### 4.1 User & Profile API
*   `POST /auth/signup` $\rightarrow$ Create account & trigger smart account deployment.
*   `PATCH /user/username` $\rightarrow$ Claim/change `@username`.
*   `GET /user/profile/{username}` $\rightarrow$ Fetch public profile & reputation.

### 4.2 Payment Orchestration
*   `POST /payments/request` $\rightarrow$ Create a payment request for another user.
*   `GET /payments/inbox` $\rightarrow$ Fetch all pending requests and receipts.
*   `POST /payments/resolve` $\rightarrow$ Approve/Reject a payment request.

### 4.3 AI Assistant Integration
*   `POST /ai/parse-intent` $\rightarrow$ Input: "Send 50 USDC to @alex" $\rightarrow$ Output: `{ action: "SEND", amount: 50, recipient: "0x...", token: "USDC" }`.

---

## 5. Implementation Roadmap

### Phase 1: The Foundation (MVP)
*   **Onboarding:** Implement Email/Google Login $\rightarrow$ Automatic Smart Account creation (via AA provider).
*   **Core Wallet:** Integrated `Unified Balance` to show total USDC across chains.
*   **P2P Payments:** Implement `kit.send` using usernames (mapping username $\rightarrow$ address via API).
*   **Bridge:** Implement "One-Tap Bridge" via `kit.bridge` to bring assets from Base/Ethereum to Arc.
*   **Basic UI:** Modern Fintech Dashboard (Total Balance, Recent Activity).

### Phase 2: Intelligent Finance
*   **AI Assistant:** Integrate LLM for intent parsing $\rightarrow$ Generate confirmation screens $\rightarrow$ Execute via App Kit.
*   **Merchant Suite:** Static and Dynamic QR generation for businesses + Merchant Dashboard.
*   **Subscriptions:** Smart contract-based recurring payments with "Pause/Cancel" controls.
*   **Notifications:** WebSocket integration for real-time "Payment Received" alerts.

### Phase 3: Agentic Economy
*   **Agent Wallets:** Ability to spawn sub-wallets with strict spending limits.
*   **Marketplace:** UI to discover and subscribe to specialized AI Agents.
*   **Reputation System:** Algorithm to calculate trust scores based on transaction history.
*   **Compliance:** Integrate risk-scoring services for transaction monitoring.

---

## 6. Critical Technical Workflows

### 6.1 The "Zero-Knowledge" User Experience
To ensure users never see "blockchain" details:
1.  **Sending:** User enters `@username` $\rightarrow$ App fetches address from API $\rightarrow$ App calls `kit.send` $\rightarrow$ Biometric prompt $\rightarrow$ Transaction sent.
2.  **Bridging:** User selects "Add Funds" $\rightarrow$ Selects "Base" $\rightarrow$ App calls `kit.bridge` $\rightarrow$ Background process handles CCTP mint/burn.

### 6.2 Security Model
*   **Local Security:** Use `EncryptedSharedPreferences` and Android Keystore for session tokens.
*   **Authentication:** Passkeys for passwordless, high-security login.
*   **Approval:** All transactions require a biometric signature (Fingerprint/Face) before the smart account executes.

---

## 7. State Management & UI Plan
*   **State:** Use `StateFlow` in ViewModels to represent `WalletState` (Loading, Loaded, Error).
*   **UI:** Jetpack Compose with a design system focusing on:
    *   **Contrast:** High-contrast typography for financial figures.
    *   **Feedback:** Haptic feedback on successful payments.
    *   **Simplicity:** No "Gas" or "Network" selectors; the app chooses the most efficient route.

---

## 8. Current Repository Implementation Status

### 8.1 Android App Scaffold (`arcora/app`)
The Android client now follows Clean Architecture + MVVM and is organized around these boundaries:

*   **Domain layer**
    *   `domain/model`: money, portfolio, user profile, wallet, bridge, and transaction records.
    *   `domain/repository`: wallet/auth contracts.
    *   `domain/usecase`: create smart wallet, observe dashboard, send payment, bridge to Arc.
    *   `domain/security`: secure session and transaction approval abstractions.
    *   `domain/qr`: ArcOra payment QR payload contracts.
    *   `domain/ai`, `domain/merchant`, `domain/subscription`, `domain/agent`, `domain/compliance`: Phase 2/3 domain placeholders.
*   **Data layer**
    *   Mock Arc Testnet repositories for wallet creation, unified balance, send, bridge, and activity.
    *   Retrofit API contract targeting the NestJS backend.
    *   Encrypted session storage backed by AndroidX Security Crypto.
    *   Mock transaction authorizer representing the future biometric/passkey boundary.
    *   QR payload generator using the `arcora://pay` deep-link contract.
*   **Presentation layer**
    *   Compose screens for onboarding, dashboard, send, receive, bridge, activity inbox, AI assistant, merchant dashboard, subscriptions, and agent marketplace.
    *   Shared design system components and theme.
    *   Navigation routes for the full roadmap surface.

### 8.2 Backend Scaffold (`arcora/backend`)
The backend is scaffolded as NestJS + Prisma:

*   `auth`: signup endpoint to register email, username, and smart account address.
*   `users`: username profile lookup.
*   `payments`: payment requests, inbox, and resolution.
*   `wallet`: unified-balance adapter placeholder for Arc App Kit.
*   `activity`: transaction metadata feed.
*   `ai`: parse-intent endpoint placeholder.
*   `merchants`: merchant dashboard placeholder.
*   `subscriptions`: recurring payment placeholder.
*   `agents`: agent marketplace placeholder.
*   `prisma/schema.prisma`: users, transactions, payment requests, agent wallets, merchant accounts, and subscriptions.

### 8.3 Validation Status
Gradle validation was attempted with:

```bash
cd arcora && gradlew.bat :app:assembleDebug
```

Validation is currently blocked because:

*   no Gradle wrapper exists in `arcora/`, and
*   no system `gradle` executable is available in PATH.

Next validation options:

1. Use Android Studio Gradle Sync to generate/use the wrapper.
2. Add a Gradle wrapper to the project, then run `gradlew.bat :app:assembleDebug`.
3. Install Gradle locally and run `gradle :app:assembleDebug`.

---

## 9. Production Integration Boundaries

### 9.1 Arc App Kit Adapter
Create an `ArcWalletRepository` implementation to replace `MockWalletRepository` once Arc App Kit credentials/endpoints are available. It must implement:

*   smart account creation / lookup,
*   unified USDC balance,
*   username-recipient resolution through backend,
*   `send` execution,
*   `bridge` quote and execution,
*   transaction history mapping.

### 9.2 Authentication Adapter
Replace mock onboarding with:

*   Google login,
*   passkeys,
*   backend session issuance,
*   encrypted token persistence,
*   session refresh/logout flows.

### 9.3 Transaction Approval Adapter
Replace `MockTransactionAuthorizer` with an Activity-bound Android `BiometricPrompt` implementation. The domain boundary is already present, so send/bridge use cases will continue to require explicit approval before execution.

### 9.4 Backend Production Services
The backend must evolve from metadata scaffold to production orchestration:

*   JWT/session guards.
*   Rate limiting and audit logs.
*   WebSocket notification gateway.
*   Idempotency keys for payment and bridge actions.
*   Risk/compliance checks before high-risk activity.
*   Background workers for CCTP/bridge status updates.

---

## 10. Next Milestones

### Milestone A: Compile & Stabilize
1. Add Gradle wrapper or run Android Studio sync.
2. Build `:app:assembleDebug`.
3. Fix Kotlin/Hilt/Compose compile errors.
4. Add minimal unit tests for use cases and QR payload generation.

### Milestone B: Real Backend Loop
1. Install backend dependencies.
2. Configure PostgreSQL.
3. Run Prisma migration.
4. Add auth/session middleware.
5. Connect Android Retrofit repositories to local backend.

### Milestone C: Arc Testnet Integration
1. Replace mock smart wallet creation with Arc App Kit account creation.
2. Replace mock balance/send/bridge with real Arc Testnet calls.
3. Add CCTP bridge status tracking.
4. Add transaction hash links and reconciliation.

### Milestone D: Production UX
1. Real QR image generation and dynamic payment requests.
2. AI intent confirmation routing into send/request/bridge flows.
3. Merchant checkout links and analytics.
4. Subscription pause/cancel/limit controls.
5. Agent wallet permission UI and marketplace onboarding.