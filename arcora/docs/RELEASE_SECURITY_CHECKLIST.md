# ArcOra Release and Security Checklist

This checklist captures the remaining manual release, compliance, and observability setup needed before ArcOra moves from Arc Testnet MVP validation to a public or production distribution.

## Scope

- Android release signing and build hygiene.
- Testnet user disclosure, privacy, and terms readiness.
- Backend runtime safety checks.
- Crash reporting, logging, monitoring, and alerting plan.
- Manual inputs that cannot be safely committed to source control.

## Android Release Signing

### Manual inputs required

- Release keystore file stored outside the repository.
- Keystore password.
- Key alias.
- Key password.
- Google Play upload key registration, if Play distribution is used.

### Source-control rules

- Do not commit keystore files.
- Do not commit signing passwords.
- Do not commit generated Play Console credentials.
- Keep signing material outside [arcora/](..).
- Keep local signing property files excluded by [arcora/.gitignore](../.gitignore).

### Recommended Gradle setup

Use local-only properties such as releaseStoreFile, releaseStorePassword, releaseKeyAlias, and releaseKeyPassword from an untracked properties file. Wire those values into the release signing config in [arcora/app/build.gradle.kts](../app/build.gradle.kts) only after the keystore exists.

### Release verification

- Build release APK or AAB from a clean checkout.
- Confirm the release artifact points to the production HTTPS API URL, not emulator localhost.
- Confirm minification or shrinking decisions are intentional.
- Confirm the release artifact installs on a physical device.
- Confirm biometric payment approval works on a real device with enrolled biometrics.

## Testnet Disclosure Checklist

ArcOra currently targets Arc Testnet and must make that clear before external testing.

### Required copy

- Funds are testnet assets unless a production chain is explicitly enabled.
- Testnet balances may reset or become unavailable.
- ArcOra can prepare flows, but real payment broadcast depends on Arc and Circle signing credentials and provider policy.
- Users should not send production funds to testnet addresses.
- Merchant checkout links and subscription actions are MVP/testnet flows until production settlement is enabled.

### Suggested app locations

- Onboarding screen.
- Dashboard network chip or banner.
- Send confirmation prompt.
- Bridge confirmation prompt.
- Receive QR screen.
- Merchant checkout screen.

## Privacy and Terms Checklist

### Privacy policy must disclose

- Account identifiers collected during signup, such as email, display name, username, and smart-account address.
- Backend session token storage on device encrypted storage.
- Payment intent, merchant, subscription, and activity records stored by the backend.
- Chain and wallet provider interactions through configured Arc and Circle infrastructure.
- Crash and observability telemetry if enabled.
- Data retention and deletion process.

### Terms must disclose

- Testnet status and no guarantee of asset value.
- No custody or production-money promise until production wallet execution is enabled.
- User responsibility for addresses, QR codes, and transaction approvals.
- Merchant/subscription feature limitations during MVP.
- Compliance pre-check limitations and production KYB/KYC requirements.

## Backend Runtime Safety

### Environment checks

Production deployments must set real values for database, session secret, Arc network, wallet provider, and CORS settings. The backend template is [arcora/backend/.env.example](../backend/.env.example).

Required production validation currently covers database URL, JWT secret, Arc chain ID, Arc RPC URL, and Arc USDC address through [arcora/backend/src/env.validation.ts](../backend/src/env.validation.ts).

### Migration checklist

- Generate and review Prisma migrations locally.
- Apply deployed migrations with npm run prisma:migrate:deploy from [arcora/backend/package.json](../backend/package.json).
- Never run development migrations against production databases.
- Back up production PostgreSQL before schema changes.

### Dependency audit checklist

- Run production dependency audit before release.
- Track remaining Nest and Express transitive advisories.
- Plan a Nest major-version upgrade before production launch if no safe patch exists.

## Observability Plan

### Backend

- Add request logging with request ID, route, latency, status, and user/session identifier when available.
- Exclude session tokens, authorization headers, wallet-provider secrets, and private API keys from logs.
- Wire Sentry or an equivalent error tracker through SENTRY_DSN in [arcora/backend/.env.example](../backend/.env.example).
- Add alerts for health-check failure, elevated API error rate, payment-quote failures, compliance blocks, and wallet-provider failures.

### Android

- Add crash reporting for release builds after a provider is selected.
- Redact access tokens, wallet-provider secrets, and QR payloads from logs.
- Track non-sensitive events for onboarding success, balance refresh failure, payment quote failure, biometric rejection, and compliance block.
- Keep telemetry opt-out and privacy policy aligned.

## Manual Blockers Before Production Execution

- Real PostgreSQL URL and migration access.
- Production backend URL.
- Arc or Circle signing credentials and execution policy.
- Release keystore details.
- Google OAuth client IDs.
- Privacy policy and terms publication URLs.
- Crash reporting provider project and DSN.
