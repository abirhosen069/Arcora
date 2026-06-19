# ArcOra Live App Worklog

This file records the step-by-step work required to turn ArcOra from a validated scaffold into a live Arc Testnet MVP.

## Working Rule
- Continue implementation step by step.
- Stop only when manual input is required, such as private credentials, deployed service URLs, API keys, or account-console setup.
- Keep this file and `docs/PROGRESS.md` updated after meaningful work.

## Current Live-App Goal
Ship a live Arc Testnet MVP with:
- Production-ready backend environment configuration.
- Android app connected to configurable backend URL.
- Real Arc Testnet wallet/balance/payment/bridge integrations once credentials are available.
- Secure auth/session and biometric/passkey transaction confirmation.
- Testnet-validated signup, balance, send, receive, activity, and optional bridge flows.

## Execution Checklist

### 1. Backend Readiness
- [x] Confirm backend dependencies install successfully.
- [x] Generate Prisma client.
- [x] Add health endpoint for deployment/runtime checks.
- [x] Add strict environment validation.
- [x] Configure database migration flow.
- [x] Add deployment documentation.

### 2. Android Live Configuration
- [x] Move backend base URL from hardcoded emulator URL to Gradle `BuildConfig`.
- [x] Add debug/release backend URL placeholders.
- [x] Keep emulator localhost working for local development.
- [x] Document how to set production API URL.

### 3. API-Backed App State
- [x] Replace mock auth repository with API-backed implementation.
- [x] Replace mock wallet repository with API-backed implementation.
- [x] Add network error/loading/retry handling.
- [x] Persist authenticated user/session securely.

### 4. Arc Testnet Integration
- [x] Add Arc Testnet RPC/App Kit configuration to backend env template.
- [x] Implement balance lookup using Arc Testnet provider/API.
- [ ] Implement transaction creation/execution path.
- [ ] Implement transaction status polling/indexing.
- [ ] Implement bridge quote/execution path if Arc route credentials are available.

### 5. Security and Release
- [x] Replace mock transaction authorizer with Android biometric confirmation.
- [x] Add backend/API-backed compliance pre-check before payment and bridge approval.
- [x] Add release signing config documentation.
- [x] Add privacy/terms/testnet disclaimer checklist.
- [x] Add crash/logging/observability plan.

## Work Entries

### 2026-06-16
- Started live-app implementation track after Android scaffold validation.
- Audited backend scaffold under `arcora/backend`.
- Audited Android network integration in `arcora/app/src/main/java/com/arcora/data/api/NetworkModule.kt`.
- Found current backend URL is hardcoded as `http://10.0.2.2:8080/` and should be moved to build configuration.
- Expanded `arcora/backend/.env.example` with production, Arc Testnet, wallet provider, auth, CORS, and observability placeholders.
- Added `arcora/backend/src/env.validation.ts` and wired it into `ConfigModule` for production-required environment checks.
- Updated backend CORS handling in `arcora/backend/src/main.ts` to use `CORS_ORIGINS`.
- Added `arcora/backend/src/health.controller.ts` and registered it in `AppModule` for `/health` runtime checks.
- Added backend startup/deployment notes in `arcora/backend/README.md`.
- Installed backend dependencies with `npm --prefix D:\\Arcora\\arcora\\backend install`; install completed with npm audit warnings.
- Validated backend TypeScript/Nest build with `npm --prefix D:\\Arcora\\arcora\\backend run build`; build passed.
- Generated Prisma client with `npm --prefix D:\\Arcora\\arcora\\backend run prisma:generate`; generation passed.
- Moved Android Retrofit base URL to `BuildConfig.API_BASE_URL` in `NetworkModule.kt`.
- Enabled Android `buildConfig` and added debug/release `API_BASE_URL` fields in `app/build.gradle.kts`.
- Android Gradle validation is blocked in the current shell because `JAVA_HOME` is not set and `java` is unavailable on PATH.
- Git status could not be checked because `D:/Arcora/arcora` is not a git repository.
- Added root `.gitignore` to exclude Android build output, local properties, backend `node_modules`, backend `dist`, and secret env files while preserving `.env.example`.
- Wired provided Arc Testnet public network values into `backend/.env.example`: RPC `https://rpc.testnet.arc.network`, chain ID `5042002`, explorer `https://testnet.arcscan.app`, gas tracker, and Circle faucet.
- Set `ARC_USDC_ADDRESS=native` because provided network details specify `USDC` as the native currency symbol rather than a token contract address.
- Relaxed production env validation so `ARC_USDC_ADDRESS` is optional/native-aware; `DATABASE_URL`, `JWT_SECRET`, `ARC_CHAIN_ID`, and `ARC_RPC_URL` remain required.
- Added `ArcNetworkService` and `GET /wallet/network/arc-testnet` to expose Arc Testnet metadata to clients.
- Updated wallet unified-balance placeholder to include Arc Testnet chain ID and native-USDC mode.
- Revalidated backend build after Arc network changes with `npm --prefix D:\\Arcora\\arcora\\backend run build`; build passed.
- Ran `npm audit --omit=dev`; production dependencies still report moderate/high Nest/Express transitive vulnerabilities that require breaking updates.
- Attempted safe `npm audit fix`; no non-breaking fixes were available.
- Updated backend Nest package ranges to latest available Nest 10 patch line and reinstalled; installed versions are `@nestjs/common/core/platform-express@10.4.22` and `@nestjs/config@3.3.0`.
- Revalidated backend build after dependency range update; build passed.
- Remaining npm audit findings require a planned Nest 11/major dependency upgrade before production launch.
- Received Circle/App Kit configuration values from user; did not write pasted key material into committed files.
- Expanded `backend/.env.example` with `CIRCLE_PROJECT_ID`, `CIRCLE_WALLET_SET_ID`, and `TRANSACTION_EXECUTION_MODE` placeholders.
- Added `CircleAppKitService` to report wallet provider readiness using booleans only, without exposing secrets.
- Added `GET /wallet/provider/circle-app-kit/status` for safe runtime wallet-provider configuration checks.
- Validated backend build after Circle/App Kit status integration; build passed.
- Added Arc Testnet USDC contract address `0x3600000000000000000000000000000000000000` to `backend/.env.example`.
- Replaced wallet unified-balance placeholder with live Arc Testnet JSON-RPC balance lookup using ERC-20 `balanceOf(address)` against configured `ARC_USDC_ADDRESS`.
- Kept native-USDC fallback support for `ARC_USDC_ADDRESS=native`, but current Arc Testnet config uses the provided contract address.
- Added EVM address validation and RPC error handling for balance lookup.
- Validated backend build after live balance implementation; build passed.
- Added Android `UnifiedBalanceResponse` fields for live backend balance payloads including chain ID, token balances, native-USDC mode, source, and timestamp.
- Added `WalletRepository.refreshPortfolio(smartAccountAddress)` and wired `MockWalletRepository` to call `ArcOraApi.unifiedBalance`.
- Dashboard now refreshes live Arc Testnet USDC balance through the backend endpoint, updates portfolio state, and shows a `Live RPC` refresh chip plus safe error text.
- Updated `AuthRepository.currentUser` to `StateFlow` so ViewModels can safely read the current wallet address for refresh.
- Validated Android debug build with `D:\\Arcora\\arcora\\build-local.bat`; build passed.
- Revalidated backend build after Android integration work; build passed.
- Added real QR code rendering dependency and replaced the Receive placeholder with generated static and payment-request QR codes.
- Added `ReceiveViewModel` to derive QR payloads from the current authenticated user and generate amount-locked request payloads with request IDs.
- Updated QR payload generation to URL-encode all query parameter values.
- Added API/network error normalization for Android repositories so common HTTP/network failures surface user-safe messages.
- Revalidated Android debug build after QR and error-normalization work; build passed.
- Revalidated backend build after this work; build passed.
- Added API-backed AI assistant repository and bound it in Hilt, replacing the mock AI parser for app runtime.
- Expanded Android API contracts for `POST /ai/parse-intent` responses.
- Upgraded backend AI intent parsing response with confidence, source-chain detection, and confirmation titles.
- Added AI confirmation execution routing in `AssistantViewModel`: send intents prepare payment quotes through the existing send use case, bridge intents prepare bridge execution through the bridge use case, request intents route users toward Receive request QR generation, and spending intents show analytics-readiness feedback.
- Updated Assistant UI with action-specific metadata, execution copy, confirm/prepare buttons, and result cards.
- Revalidated backend build after AI parsing work; build passed.
- Revalidated Android debug build after AI parsing and confirmation routing work; build passed.
- Added backend merchant account creation, merchant dashboard aggregation, and merchant checkout-link payload generation endpoints.
- Added backend subscription create/list/status/pause/renew/cancel lifecycle endpoints.
- Added a global backend common module to export `PrismaService` consistently to feature modules.
- Added Android merchant API contracts, `MerchantDashboardViewModel`, and wired Merchant Suite UI to create demo merchants, fetch dashboard metrics, and create checkout links.
- Added Android subscription API contracts, `SubscriptionsViewModel`, and wired Subscriptions UI to create, list, pause, renew, and cancel subscriptions.
- Revalidated backend build after merchant/subscription backend work; build passed.
- Revalidated Android debug build after merchant/subscription UI/API integration; build passed.
- Added API-backed Android auth repository and bound it in Hilt, replacing the mock auth repository for app runtime.
- Backend signup now returns both user profile and a signed testnet session payload; Android persists the returned session through encrypted storage.
- Added idempotent backend signup behavior for existing email, username, or smart-account records so repeat local onboarding attempts do not hard-fail on unique constraints.
- Added API-backed Android wallet repository and bound it in Hilt, replacing the mock wallet repository for app runtime.
- Android wallet repository now resolves recipients through backend username lookup, refreshes portfolio through live backend balance, maps backend activity responses, and prepares payment quotes through `POST /payments/quote`.
- Bridge execution remains quote/preparation-only until Arc/Circle route credentials and signing policy are available.
- Added backend `prisma:migrate:deploy` and `prisma:studio` scripts to support production migration deployment and local inspection.
- Updated backend README with migration deployment guidance and production warning not to run `prisma migrate dev` against production databases.
- Revalidated backend build after API-backed auth/session and migration script work; build passed.
- Revalidated Android debug build after API repository wiring; build passed.
- Added backend compliance module with `POST /compliance/screen-counterparty` for local policy counterparty screening.
- Added Android compliance API contracts and an API-backed `CompliancePolicy` implementation bound in Hilt.
- Updated send and bridge use cases to run compliance pre-checks before transaction authorization and show risk score in approval copy.
- Revalidated backend build after compliance work with `npm --prefix arcora\\backend run build`; build passed.
- Revalidated Android debug build after compliance work with `arcora\\build-local.bat`; build passed.
- Added `BiometricTransactionAuthorizer` to replace the mock auto-approval authorizer with Android `BiometricPrompt` transaction confirmation.
- Updated `MainActivity` to extend `FragmentActivity` and register the active activity for biometric prompt presentation.
- Rebound `TransactionAuthorizer` in Hilt from the mock implementation to the biometric implementation.
- Revalidated Android debug build after biometric transaction authorization work with `arcora\\build-local.bat`; build passed.
- Added `arcora/docs/RELEASE_SECURITY_CHECKLIST.md` covering Android release signing, testnet disclosures, privacy/terms readiness, backend runtime safety, migration controls, dependency audit tracking, and observability planning.
- Marked release signing documentation, privacy/terms/testnet disclaimer checklist, and crash/logging/observability planning complete at the documentation level.

### 2026-06-17
- Created/reset the local PostgreSQL `arcora` role and `arcora` database to match backend `.env` for live local runtime validation.
- Synced the Prisma schema to the local PostgreSQL database and confirmed expected runtime tables exist.
- Started the NestJS backend locally and verified `/health` returns `ok` while Android development continues.
- Updated Android debug networking for a physical device by switching debug `API_BASE_URL` to `http://127.0.0.1:8080/`, enabling cleartext traffic, and using `adb reverse tcp:8080 tcp:8080`.
- Rebuilt, manually reinstalled, cleared app data, and relaunched the debug app on the connected Android device.
- Added reusable Android network status/retry UI through `ArcOraStatusCard`.
- Added user-visible retry cards for onboarding wallet creation, dashboard live balance sync, send payment approval, bridge quote/execution, merchant dashboard/setup, and subscriptions refresh.
- Revalidated Android debug build with `./gradlew :app:assembleDebug`; build passed.
- Revalidated backend build with `npm run build`; build passed.

## Manual Inputs Needed Later
- Arc App Kit endpoint/details beyond public RPC, if required for wallet execution.
- Arc/Circle/API keys required for wallet and transaction execution.
- Production PostgreSQL `DATABASE_URL`.
- Production backend deployment URL.
- Auth provider credentials such as Google OAuth client IDs.
- Release signing keystore details.