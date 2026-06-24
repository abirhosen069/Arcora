import { Injectable, UnauthorizedException, BadRequestException } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { createHmac, randomBytes } from 'crypto';
import { hash, compare } from 'bcrypt';
import { PrismaService } from '../common/prisma.service';
import { EmailService } from '../email/email.service';
import { RegisterStartDto, RegisterVerifyDto, LoginDto, RequestOtpDto } from './dto';

const SESSION_TTL_MS = 7 * 24 * 60 * 60 * 1000;
const OTP_TTL_MS = 10 * 60 * 1000;
const BCRYPT_ROUNDS = 10;

@Injectable()
export class AuthService {
  constructor(
    private readonly prisma: PrismaService,
    private readonly config: ConfigService,
    private readonly emailService: EmailService,
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

    return {
      user,
      session: this.createSession(user.id),
    };
  }

  async login(dto: LoginDto) {
    const email = dto.email.toLowerCase();

    const user = await this.prisma.user.findUnique({ where: { email } });
    if (!user) {
      throw new UnauthorizedException('No account found with this email. Please register first.');
    }
    if (!user.passwordHash) {
      throw new UnauthorizedException('This account uses social login. Please use the original login method.');
    }

    const passwordValid = await compare(dto.password, user.passwordHash);
    if (!passwordValid) {
      throw new UnauthorizedException('Incorrect password. Please try again.');
    }

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
