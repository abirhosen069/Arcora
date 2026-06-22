import { Injectable, UnauthorizedException } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { createHmac, randomBytes } from 'crypto';
import { PrismaService } from '../common/prisma.service';
import { SignupDto, GoogleAuthDto, PasskeyRegistrationFinishDto, PasskeyAuthenticationFinishDto } from './dto';

const SESSION_TTL_MS = 7 * 24 * 60 * 60 * 1000;

@Injectable()
export class AuthService {
  constructor(
    private readonly prisma: PrismaService,
    private readonly config: ConfigService,
  ) {}

  private generateSmartAccountAddress(seed: string): string {
    const secret = this.config.get<string>('JWT_SECRET') ?? 'development_only_arcora_secret';
    const hash = createHmac('sha256', secret).update(seed).digest('hex');
    return `0x${hash.padEnd(40, '0').slice(0, 40)}`;
  }

  async signup(dto: SignupDto) {
    const email = dto.email.toLowerCase();
    const username = dto.username.startsWith('@') ? dto.username.toLowerCase() : `@${dto.username.toLowerCase()}`;
    const smartAccountAddress = dto.smartAccountAddress
      ? dto.smartAccountAddress.toLowerCase()
      : this.generateSmartAccountAddress(email);

    const existingUser = await this.prisma.user.findFirst({
      where: {
        OR: [{ email }, { username }, { smartAccountAddress }],
      },
    });

    const user = existingUser
      ? await this.prisma.user.update({
          where: { id: existingUser.id },
          data: {
            email: existingUser.email === email ? email : existingUser.email,
            username: existingUser.username === username ? username : existingUser.username,
            displayName: dto.displayName,
            smartAccountAddress:
              existingUser.smartAccountAddress === smartAccountAddress ? smartAccountAddress : existingUser.smartAccountAddress,
          },
        })
      : await this.prisma.user.create({
          data: {
            email,
            username,
            displayName: dto.displayName,
            smartAccountAddress,
          },
        });

    return {
      user,
      session: this.createSession(user.id),
    };
  }

  async googleAuth(dto: GoogleAuthDto) {
    const googleClientId = this.config.get<string>('GOOGLE_CLIENT_ID');
    if (!googleClientId) {
      throw new UnauthorizedException('Google authentication is not configured on this server.');
    }

    let email: string;
    let name: string;
    try {
      const parts = dto.idToken.split('.');
      if (parts.length !== 3) throw new Error('Invalid token format');
      const payload = JSON.parse(Buffer.from(parts[1], 'base64url').toString('utf8'));
      email = payload.email;
      name = payload.name || dto.displayName;
      if (!email) throw new Error('No email in token');
    } catch {
      throw new UnauthorizedException('Invalid Google ID token.');
    }

    const normalizedEmail = email.toLowerCase();
    const username = dto.username.startsWith('@') ? dto.username.toLowerCase() : `@${dto.username.toLowerCase()}`;
    const smartAccountAddress = dto.smartAccountAddress
      ? dto.smartAccountAddress.toLowerCase()
      : this.generateSmartAccountAddress(normalizedEmail);

    const existingUser = await this.prisma.user.findFirst({
      where: {
        OR: [{ email: normalizedEmail }, { username }, { smartAccountAddress }],
      },
    });

    const user = existingUser
      ? await this.prisma.user.update({
          where: { id: existingUser.id },
          data: {
            displayName: name,
            isVerified: true,
            smartAccountAddress:
              existingUser.smartAccountAddress === smartAccountAddress ? smartAccountAddress : existingUser.smartAccountAddress,
          },
        })
      : await this.prisma.user.create({
          data: {
            email: normalizedEmail,
            username,
            displayName: name,
            smartAccountAddress,
            isVerified: true,
          },
        });

    return {
      user,
      session: this.createSession(user.id),
    };
  }

  async passkeyRegistrationStart(email: string) {
    const challenge = Buffer.from(crypto.getRandomValues(new Uint8Array(32))).toString('base64url');
    const rpId = this.config.get<string>('PASSKEY_RPID') || 'arcora.app';
    return {
      challenge,
      rp: { id: rpId, name: 'ArcOra' },
      userVerification: 'preferred',
      timeout: 60000,
      email,
    };
  }

  async passkeyRegistrationFinish(dto: PasskeyRegistrationFinishDto, challenge: string) {
    const email = dto.email.toLowerCase();
    const username = dto.username.startsWith('@') ? dto.username.toLowerCase() : `@${dto.username.toLowerCase()}`;
    const smartAccountAddress = dto.smartAccountAddress.toLowerCase();

    let user = await this.prisma.user.findFirst({ where: { email } });
    if (!user) {
      user = await this.prisma.user.create({
        data: {
          email,
          username,
          displayName: dto.displayName,
          smartAccountAddress,
          isVerified: true,
        },
      });
    } else {
      user = await this.prisma.user.update({
        where: { id: user.id },
        data: { isVerified: true },
      });
    }

    return {
      user,
      session: this.createSession(user.id),
    };
  }

  async passkeyAuthenticationStart(email: string) {
    const challenge = Buffer.from(crypto.getRandomValues(new Uint8Array(32))).toString('base64url');
    return {
      challenge,
      timeout: 60000,
      userVerification: 'preferred',
      email,
    };
  }

  async passkeyAuthenticationFinish(dto: PasskeyAuthenticationFinishDto, challenge: string) {
    const user = await this.prisma.user.findFirst({
      where: { email: { not: undefined } },
      orderBy: { createdAt: 'desc' },
    });

    if (!user) {
      throw new UnauthorizedException('No account found for passkey authentication.');
    }

    return {
      user,
      session: this.createSession(user.id),
    };
  }

  async getProfile(userId: string) {
    return this.prisma.user.findUniqueOrThrow({ where: { id: userId } });
  }

  private createSession(userId: string) {
    const expiresAtEpochMillis = Date.now() + SESSION_TTL_MS;
    const payload = {
      sub: userId,
      typ: 'arcora_testnet_session',
      exp: Math.floor(expiresAtEpochMillis / 1000),
    };
    const payloadJson = JSON.stringify(payload);
    const encodedPayload = Buffer.from(payloadJson).toString('base64url');
    const secret = this.config.get<string>('JWT_SECRET') ?? 'development_only_arcora_secret';
    const signature = createHmac('sha256', secret).update(encodedPayload).digest('base64url');

    return {
      accessToken: `${encodedPayload}.${signature}`,
      refreshToken: null,
      expiresAtEpochMillis,
    };
  }
}
