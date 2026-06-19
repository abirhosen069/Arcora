import { Injectable } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { createHmac, timingSafeEqual } from 'crypto';

export type SessionPayload = {
  sub: string;
  typ: string;
  exp: number;
};

@Injectable()
export class SessionService {
  constructor(private readonly config: ConfigService) {}

  verifyAccessToken(token: string): SessionPayload | null {
    const parts = token.split('.');
    if (parts.length !== 2) {
      return null;
    }

    const [encodedPayload, signature] = parts;
    const secret = this.config.get<string>('JWT_SECRET') ?? 'development_only_arcora_secret';
    const expectedSignature = createHmac('sha256', secret).update(encodedPayload).digest('base64url');

    const provided = Buffer.from(signature);
    const expected = Buffer.from(expectedSignature);
    if (provided.length !== expected.length || !timingSafeEqual(provided, expected)) {
      return null;
    }

    try {
      const payload = JSON.parse(Buffer.from(encodedPayload, 'base64url').toString('utf8')) as SessionPayload;
      if (payload.typ !== 'arcora_testnet_session') {
        return null;
      }
      if (payload.exp * 1000 <= Date.now()) {
        return null;
      }
      return payload;
    } catch {
      return null;
    }
  }
}
