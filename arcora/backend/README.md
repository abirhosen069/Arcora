# ArcOra Backend

NestJS API for the ArcOra Arc Testnet wallet.

## Production Deploy (Recommended)

See **[docs/DEPLOY.md](../../docs/DEPLOY.md)** for Render blueprint deployment — no local PostgreSQL or `npm run dev` required.

Quick verify after deploy:

```bash
curl https://arcora-api.onrender.com/health
```

## Local Setup (Optional)

1. Copy the environment template:

```bash
copy .env.example .env
```

2. Fill required local values in `.env`. For local compile-only validation, placeholder Arc values are acceptable. For runtime with database access, set a working PostgreSQL `DATABASE_URL`.

3. Install dependencies:

```bash
npm install
```

4. Generate Prisma client:

```bash
npm run prisma:generate
```

5. Run migrations once PostgreSQL is available:

```bash
npm run prisma:migrate
```

For deployed/staging/production environments, apply already-created migrations with:

```bash
npm run prisma:migrate:deploy
```

Use Prisma Studio for local inspection only:

```bash
npm run prisma:studio
```

6. Start development server:

```bash
npm run dev
```

7. Health check:

```bash
curl http://localhost:8080/health
```

## Docker

```bash
docker build -t arcora-api .
docker run -p 8080:8080 --env-file .env arcora-api
```

Container entrypoint runs `prisma migrate deploy` then starts the API.

## Production Environment Requirements

When `NODE_ENV=production`, required variables are `DATABASE_URL`, `JWT_SECRET`, `ARC_CHAIN_ID`, `ARC_RPC_URL`, and `ARC_USDC_ADDRESS`.

For live payments, also set `RELAYER_PRIVATE_KEY` (0x-prefixed hex) and fund the relayer address shown in `/health` with Arc Testnet USDC.

Additional live values: `ARC_EXPLORER_URL`, Circle keys, Google OAuth client IDs, and `CORS_ORIGINS`.

## Deployment Notes

- Deploy with HTTPS only.
- Use managed PostgreSQL for production.
- Run `npm run build` during deployment.
- Use `npm run start:prod` (or Docker CMD) to migrate and start.
- Never run `prisma migrate dev` against production databases.
- Point Android `ARCORA_API_BASE_URL` at the deployed HTTPS backend URL.
