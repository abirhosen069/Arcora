import { Injectable } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { createHmac } from 'crypto';
import { PrismaService } from '../common/prisma.service';
import { SignupDto } from './dto';

const SESSION_TTL_MS = 7 * 24 * 60 * 60 * 1000;

@Injectable()
export class AuthService {
  constructor(
    private readonly prisma: PrismaService,
    private readonly config: ConfigService,
  ) {}

  async signup(dto: SignupDto) {
    const email = dto.email.toLowerCase();
    const username = dto.username.startsWith('@') ? dto.username.toLowerCase() : `@${dto.username.toLowerCase()}`;
    const smartAccountAddress = dto.smartAccountAddress.toLowerCase();

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
