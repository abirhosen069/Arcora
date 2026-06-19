]`1 ArcOra Project Progress Tracking

## Overall Status: 🟢 Phase 1 MVP Scaffold Validated + API-Backed Runtime Wiring and Compliance Pre-Checks Active

### Phase 1: The Foundation (MVP)
- [x] Project Scaffolding & Architecture Setup
- [x] Authentication & Smart Wallet Creation UI/mock flow (Email)
- [x] Unified Balance UI/mock Arc Testnet data
- [x] P2P Payments UI/mock @username support
- [x] One-Tap Bridge UI/mock route execution
- [x] Home Dashboard UI
- [x] Receive QR placeholder UI
- [x] Transaction Inbox / Activity UI

### Phase 2: Intelligent Finance
- [ ] AI Assistant Integration
- [ ] Merchant Suite (QR Payments)
- [ ] Subscription Management
- [ ] Real-time Notifications (WebSockets)

### Phase 3: Agentic Economy
- [ ] AI Agent Wallets
- [ ] Agent Marketplace
- [ ] Reputation System
- [x] Compliance Layer Integration

---

## Recent Activity
- [x] Initialized project structure
- [x] Added Android Gradle project in `arcora/`
- [x] Added Hilt application and MainActivity Compose entry point
- [x] Added Clean Architecture domain models, repositories, use cases
- [x] Added mock repositories representing Arc Testnet App Kit flows: Send, Bridge, Unified Balance
- [x] Added Compose design system, navigation, onboarding, dashboard, send, receive, bridge, and activity screens
- [x] Attempted Gradle validation; blocked because no `gradlew.bat` exists and no system `gradle` is installed in PATH
- [x] Added NestJS backend scaffold with Prisma schema for users, transactions, payment requests, agent wallets, activity, and wallet APIs
- [x] Added Android secure session store abstraction backed by encrypted shared preferences
- [x] Added transaction authorization boundary for biometric/passkey confirmation before send/bridge execution
- [x] Added Retrofit API contracts for the backend scaffold
- [x] Added ArcOra QR payment payload generator contract and implementation
- [x] Added Android AI Assistant intent parsing scaffold and screen
- [x] Added backend AI parse-intent endpoint scaffold
- [x] Added backend merchant, subscription, and agent marketplace scaffolds
- [x] Added Android Merchant Suite, Subscriptions, and Agent Marketplace route placeholders
- [x] Added merchant, subscription, agent, and compliance domain placeholder models
- [x] Added Gradle wrapper/build helper and validated Android debug APK build
- [x] Fixed Retrofit path annotations, Compose card scope typing, AndroidX compatibility, and AGP/JDK image build issue

- [x] Added backend merchant account, dashboard, and checkout-link endpoints
- [x] Added backend subscription create/list/status/pause/renew/cancel endpoints
- [x] Added Android merchant API contracts, ViewModel, dashboard metrics, and checkout-link UI wiring
- [x] Added Android subscription API contracts, ViewModel, create/list/pause/renew/cancel UI wiring
- [x] Revalidated backend and Android builds after merchant/subscription integration
- [x] Added API-backed AI assistant repository and runtime binding
- [x] Expanded backend AI parse-intent responses with confidence, source-chain detection, and confirmation titles
- [x] Added AI confirmation execution routing for send, bridge, request, and spending intents
- [x] Updated Assistant UI with prepare/confirm action controls and result cards
- [x] Revalidated backend and Android builds after AI routing work
- [x] Added real QR rendering dependency and implemented generated static/payment-request receive QR UI
- [x] Added Receive ViewModel for current-user QR payload generation and amount-locked request QR payloads
- [x] URL-encoded QR payload query parameters
- [x] Added Android API/network error normalization for user-safe repository failures
- [x] Added API-backed Android auth repository, replacing runtime mock auth binding
- [x] Backend signup now returns user profile plus signed testnet session payload
- [x] Android onboarding now persists backend-issued session data through encrypted storage
- [x] Added API-backed Android wallet repository, replacing runtime mock wallet binding
- [x] Wallet repository now resolves recipients through backend profile lookup, refreshes live Arc Testnet balance, maps backend activity, and prepares payment quotes
- [x] Added backend migration deployment and Prisma Studio scripts
- [x] Updated backend migration/deployment documentation
- [x] Revalidated backend build and Android debug APK build after API-backed repository wiring
- [x] Added backend compliance screening endpoint for local counterparty policy pre-checks
- [x] Added Android API-backed compliance policy and Hilt runtime binding
- [x] Added compliance pre-checks before send and bridge authorization, including approval risk-score copy
- [x] Revalidated backend build and Android debug APK build after compliance integration
- [x] Replaced mock transaction auto-approval with Android BiometricPrompt-based transaction confirmation
- [x] Regitered the active FragmentActivity for biometric prompt presentation and rebound TransactionAuthorizer in Hilt
[x] Revalidated Android debug APK build after biometric authorizer integration
- [x] Added release/security checklist covering Android signing, testnet disclosure, privacy/terms, backend runtime safety, migration controls, audit tracking, and observability planning
- [x] Validated local backend runtime with PostgreSQL, Prisma schema sync, and `/health` checks
- [x] Fixed physical Android device backend connectivity with debug localhost URL, cleartext allowance, and `adb reverse`
- [x] Added retryable network status cards across onboarding, dashboard, send, bridge, merchant, and subscription flows

## Deployment (Online — No Local Dev)

- [x] Added Dockerfile and Render blueprint (`render.yaml`) for one-click cloud deploy
- [x] Added initial Prisma migration for production `migrate deploy`
- [x] Added `start:prod` script (migrate + start) for container/PaaS
- [x] Added JWT auth guard on protected API routes
- [x] Added `GET /auth/me` for Android session restore
- [x] Implemented Arc Testnet USDC send via server relay (`viem` + `RELAYER_PRIVATE_KEY`)
- [x] Android app points at production HTTPS API (`gradle.properties` → `ARCORA_API_BASE_URL`)
- [x] Android auth interceptor attaches session token to all API calls
- [x] Android calls `POST /payments/send` after biometric approval
- [x] Release builds use HTTPS-only network security config
- [x] Added `docs/DEPLOY.md` and GitHub Actions CI

## Next Engineering Tasks
- [x] Add Gradle wrapper or use Android Studio sync to run validation, then fix compile issues
- [x] Add local secure session persistence using Android Keystore / encrypted storage
- [x] Replace mock repositories with backend/API-backed runtime implementations for auth, balance, recipient lookup, activity, and payment quote preparation
- [x] Replace payment quote-only flow with real Arc/Circle/App Kit signing and broadcast when credentials/policy are available
- [ ] Add Google login and passkey integration
- [x] Replace mock transaction authorizer with real Android BiometricPrompt implementation
- [x] Add compliance pre-checks before payment and bridge approvals
- [x] Add release signing, testnet disclaimer, privacy/terms, and observability documentation
- [x] Run Prisma schema sync against a real local PostgreSQL database for runtime validation
- [x] Route AI parsed intents into concrete confirmation actions
- [x] Implement merchant checkout QR/payment links and subscription lifecycle APIs
- [ ] Replace placeholder Phase 2/3 screens with real stateful ViewModels and backend integration

## Validation
- [x] Android debug build passes using `arcora\build-local.bat`
- [x] Debug APK generated at `arcora/app/build/outputs/apk/debug/app-debug.apk`
- [x] Backend TypeScript/Nest build passes using `npm --prefix arcora\backend run build`
- [x] Backend database runtime validation completed against local PostgreSQL with Prisma schema sync