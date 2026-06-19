import { Injectable } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';

@Injectable()
export class CircleAppKitService {
  constructor(private readonly config: ConfigService) {}

  getStatus() {
    const required = [
      'CIRCLE_KIT_KEY',
      'CIRCLE_PROJECT_ID',
      'CIRCLE_WALLET_SET_ID',
      'TRANSACTION_EXECUTION_MODE',
    ];
    const optional = ['CIRCLE_API_KEY', 'ARC_PAYMASTER_URL'];

    const configured = (key: string) => {
      const value = this.config.get<string>(key);
      return Boolean(value && value.trim() && value.trim().toLowerCase() !== 'unknown');
    };

    return {
      provider: 'Circle App Kit',
      executionMode:
        this.config.get<string>('TRANSACTION_EXECUTION_MODE') ?? 'Circle App Kit embedded wallets',
      ready: required.every(configured),
      required: Object.fromEntries(required.map((key) => [key, configured(key)])),
      optional: Object.fromEntries(optional.map((key) => [key, configured(key)])),
      note: 'Status intentionally exposes only booleans and never returns API keys.',
    };
  }
}
