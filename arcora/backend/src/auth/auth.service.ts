import { Injectable, UnauthorizedException, BadRequestException } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { createHmac, randomBytes, timingSafeEqual } from 'crypto';
import { hash, compare } from 'bcrypt';
import { PrismaService } from '../common/prisma.service';
import { AuditService } from '../common/audit.service';
import { EmailService } from '../email/email.service';
import { RegisterStartDto, RegisterVerifyDto, LoginDto, RequestOtpDto, GoogleAuthDto } from './dto';

const SESSION_TTL_MS = 7 * 24 * 60 * 60 * 1000;
const REFRESH_TTL_MS = 30 * 24 * 60 * 60 * 1000;
const OTP_TTL_MS = 10 * 60 * 1000;
const BCRYPT_ROUNDS = 10;

@Injectable()
export class AuthService {
  constructor(
    private readonly prisma: PrismaService,
    private readonly config: ConfigService,
    private readonly emailService: EmailService,
    private readonly audit: AuditService,
  ) {}

  private generateSmartAccountAddress(seed: string): string {
    const secret = this.config.get<string>('JWT_SECRET') ?? 'development_only_arcora_secret';
    const hash = createHmac('sha256', secret).update(seed).digest('hex');
    return `0x${hash.padEnd(40, '0').slice(0, 40)}`;
  }

  private generateOtpCode(): string {
    return randomBytes(3).toString('hex').toUpperCase();
  }

  async registerStart(dto: RegisterStartDto) {
    const email = dto.email.toLowerCase();
    const username = dto.username.startsWith('@') ? dto.username.toLowerCase() : `@${dto.username.toLowerCase()}`;

    const existingUser = await this.prisma.user.findFirst({
      where: { OR: [{ email }, { username }] },
    });
    if (existingUser) {
      if (existingUser.email === email) {
        throw new BadRequestException('An account with this email already exists. Please log in.');
      }
      throw new BadRequestException('This username is already taken. Choose another.');
    }

    const passwordHash = await hash(dto.password, BCRYPT_ROUNDS);
    const code = this.generateOtpCode();
    const expiresAt = new Date(Date.now() + OTP_TTL_MS);

    await this.prisma.otpCode.create({
      data: { email, code, purpose: 'register', expiresAt },
    });

    await this.emailService.sendOtpCode(email, code, 'register');

    return {
      message: `Verification code sent to ${email}`,
      email,
      displayName: dto.displayName,
      username,
      passwordHash,
    };
  }

  async registerVerify(dto: RegisterVerifyDto, pendingData: { passwordHash: string; displayName: string; username: string }) {
    const email = dto.email.toLowerCase();
    const code = dto.code.toUpperCase();

    const otp = await this.prisma.otpCode.findFirst({
      where: { email, code, purpose: 'register', used: false },
      orderBy: { createdAt: 'desc' },
    });

    if (!otp) {
      throw new BadRequestException('Invalid verification code.');
    }
    if (otp.expiresAt < new Date()) {
      throw new BadRequestException('Verification code has expired. Request a new one.');
    }

    await this.prisma.otpCode.update({ where: { id: otp.id }, data: { used: true } });

    const smartAccountAddress = this.generateSmartAccountAddress(email);
    const username = pendingData.username.startsWith('@') ? pendingData.username : `@${pendingData.username}`;

    const user = await this.prisma.user.create({
      data: {
        email,
        username,
        displayName: pendingData.displayName,
        passwordHash: pendingData.passwordHash,
        smartAccountAddress,
        isVerified: true,
      },
    });

    await this.audit.logAuth(user.id, 'register.success');

    return {
      user,
      session: this.createSession(user.id),
    };
  }

  async login(dto: LoginDto) {
    const email = dto.email.toLowerCase();

    const user = await this.prisma.user.findUnique({ where: { email } });
    if (!user) {
      await this.audit.logAuth('', 'login.failed', undefined, `Email not found: ${email}`);
      throw new UnauthorizedException('No account found with this email. Please register first.');
    }
    if (!user.passwordHash) {
      await this.audit.logAuth(user.id, 'login.failed', undefined, 'Social login account');
      throw new UnauthorizedException('This account uses social login. Please use the original login method.');
    }

    const passwordValid = await compare(dto.password, user.passwordHash);
    if (!passwordValid) {
      await this.audit.logAuth(user.id, 'login.failed', undefined, 'Invalid password');
      throw new UnauthorizedException('Incorrect password. Please try again.');
    }

    await this.audit.logAuth(user.id, 'login.success');

    return {
      user,
      session: this.createSession(user.id),
    };
  }

  async requestOtp(dto: RequestOtpDto) {
    const email = dto.email.toLowerCase();
    const user = await this.prisma.user.findUnique({ where: { email } });
    if (!user) {
      throw new BadRequestException('No account found with this email.');
    }

    const code = this.generateOtpCode();
    const expiresAt = new Date(Date.now() + OTP_TTL_MS);

    await this.prisma.otpCode.create({
      data: { email, code, purpose: 'login', expiresAt },
    });

    await this.emailService.sendOtpCode(email, code, 'login');

    return { message: `Verification code sent to ${email}` };
  }

  async requestRegisterOtp(dto: RequestOtpDto) {
    const email = dto.email.toLowerCase();

    const existingUser = await this.prisma.user.findUnique({ where: { email } });
    if (existingUser) {
      throw new BadRequestException('An account with this email already exists.');
    }

    const code = this.generateOtpCode();
    const expiresAt = new Date(Date.now() + OTP_TTL_MS);

    await this.prisma.otpCode.create({
      data: { email, code, purpose: 'register', expiresAt },
    });

    await this.emailService.sendOtpCode(email, code, 'register');

    return { message: `Verification code sent to ${email}` };
  }

  async getProfile(userId: string) {
    return this.prisma.user.findUniqueOrThrow({ where: { id: userId } });
  }

  async googleAuth(dto: GoogleAuthDto) {
    const googleUser = this.verifyGoogleToken(dto.idToken);
    if (!googleUser) {
      throw new UnauthorizedException('Invalid Google ID token.');
    }

    let user = await this.prisma.user.findUnique({ where: { email: googleUser.email } });

    if (!user) {
      const username = `@${googleUser.email.split('@')[0].toLowerCase().replace(/[^a-z0-9_]/g, '')}`;
      const smartAccountAddress = this.generateSmartAccountAddress(googleUser.email);

      const existingUsername = await this.prisma.user.findUnique({ where: { username } });
      const finalUsername = existingUsername ? `${username}${Date.now()}` : username;

      user = await this.prisma.user.create({
        data: {
          email: googleUser.email,
          username: finalUsername,
          displayName: googleUser.name || dto.displayName || googleUser.email.split('@')[0],
          smartAccountAddress,
          profileImageUrl: googleUser.picture,
          isVerified: true,
        },
      });
    }

    return {
      user,
      session: this.createSession(user.id),
    };
  }

  private verifyGoogleToken(idToken: string): { email: string; name?: string; picture?: string } | null {
    try {
      const parts = idToken.split('.');
      if (parts.length !== 3) return null;

      const payload = JSON.parse(Buffer.from(parts[1], 'base64url').toString('utf8'));

      const clientId = this.config.get<string>('GOOGLE_WEB_CLIENT_ID');
      if (clientId && payload.aud !== clientId) {
        const androidClientId = this.config.get<string>('GOOGLE_ANDROID_CLIENT_ID');
        if (androidClientId && payload.aud !== androidClientId) {
          return null;
        }
      }

      if (payload.exp && payload.exp * 1000 < Date.now()) {
        return null;
      }

      return {
        email: payload.email,
        name: payload.name,
        picture: payload.picture,
      };
    } catch {
      return null;
    }
  }

  async updateProfileImage(userId: string, file: Express.Multer.File) {
    const imageUrl = `data:${file.mimetype};base64,${file.buffer.toString('base64')}`;
    const user = await this.prisma.user.update({
      where: { id: userId },
      data: { profileImageUrl: imageUrl },
    });
    return { profileImageUrl: user.profileImageUrl };
  }

  async refreshSession(refreshToken: string) {
    const secret = this.config.get<string>('JWT_SECRET') ?? 'development_only_arcora_secret';
    const parts = refreshToken.split('.');
    if (parts.length !== 2) {
      throw new UnauthorizedException('Invalid refresh token.');
    }

    const [encodedPayload, signature] = parts;
    const expectedSignature = createHmac('sha256', secret).update(encodedPayload).digest('base64url');

    const provided = Buffer.from(signature);
    const expected = Buffer.from(expectedSignature);
    if (provided.length !== expected.length || !timingSafeEqual(provided, expected)) {
      throw new UnauthorizedException('Invalid refresh token.');
    }

    try {
      const payload = JSON.parse(Buffer.from(encodedPayload, 'base64url').toString('utf8'));
      if (payload.typ !== 'arcora_refresh_token') {
        throw new UnauthorizedException('Invalid refresh token.');
      }
      if (payload.exp * 1000 <= Date.now()) {
        throw new UnauthorizedException('Refresh token expired.');
      }
      return this.createSession(payload.sub);
    } catch {
      throw new UnauthorizedException('Invalid refresh token.');
    }
  }

  private createSession(userId: string) {
    const expiresAtEpochMillis = Date.now() + SESSION_TTL_MS;
    const refreshExpiresAtEpochMillis = Date.now() + REFRESH_TTL_MS;

    const accessPayload = {
      sub: userId,
      typ: 'arcora_testnet_session',
      exp: Math.floor(expiresAtEpochMillis / 1000),
    };
    const accessPayloadJson = JSON.stringify(accessPayload);
    const encodedAccessPayload = Buffer.from(accessPayloadJson).toString('base64url');
    const secret = this.config.get<string>('JWT_SECRET') ?? 'development_only_arcora_secret';
    const accessSignature = createHmac('sha256', secret).update(encodedAccessPayload).digest('base64url');

    const refreshPayload = {
      sub: userId,
      typ: 'arcora_refresh_token',
      exp: Math.floor(refreshExpiresAtEpochMillis / 1000),
    };
    const refreshPayloadJson = JSON.stringify(refreshPayload);
    const encodedRefreshPayload = Buffer.from(refreshPayloadJson).toString('base64url');
    const refreshSignature = createHmac('sha256', secret).update(encodedRefreshPayload).digest('base64url');

    return {
      accessToken: `${encodedAccessPayload}.${accessSignature}`,
      refreshToken: `${encodedRefreshPayload}.${refreshSignature}`,
      expiresAtEpochMillis,
      refreshExpiresAtEpochMillis,
    };
  }
}
