const requiredInProduction = [
  'DATABASE_URL',
  'JWT_SECRET',
  'ARC_CHAIN_ID',
  'ARC_RPC_URL',
  'ARC_USDC_ADDRESS',
];

export function validateEnvironment(config: Record<string, unknown>) {
  const nodeEnv = String(config.NODE_ENV ?? 'development');

  if (nodeEnv === 'production') {
    const missing = requiredInProduction.filter((key) => !config[key]);
    if (missing.length > 0) {
      throw new Error(`Missing required production environment variables: ${missing.join(', ')}`);
    }

    if (config.JWT_SECRET === 'replace_with_strong_secret') {
      throw new Error('JWT_SECRET must be replaced for production.');
    }
  }

  return config;
}
