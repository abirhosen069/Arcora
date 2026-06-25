import { Test, TestingModule } from '@nestjs/testing';
import { AuthService } from '../auth.service';
import { PrismaService } from '../../common/prisma.service';
import { ConfigService } from '@nestjs/config';
import { EmailService } from '../../email/email.service';
import { AuditService } from '../../common/audit.service';
import { BadRequestException, UnauthorizedException } from '@nestjs/common';

describe('AuthService', () => {
  let service: AuthService;
  let prisma: {
    user: { findFirst: jest.Mock; findUnique: jest.Mock; findUniqueOrThrow: jest.Mock; create: jest.Mock; update: jest.Mock };
    otpCode: { create: jest.Mock; findFirst: jest.Mock; update: jest.Mock };
  };
  let emailService: { sendOtpCode: jest.Mock };
  let audit: { logAuth: jest.Mock };

  beforeEach(async () => {
    prisma = {
      user: {
        findFirst: jest.fn(),
        findUnique: jest.fn(),
        findUniqueOrThrow: jest.fn(),
        create: jest.fn(),
        update: jest.fn(),
      },
      otpCode: {
        create: jest.fn(),
        findFirst: jest.fn(),
        update: jest.fn(),
      },
    };
    emailService = { sendOtpCode: jest.fn() };
    audit = { logAuth: jest.fn() };

    const module: TestingModule = await Test.createTestingModule({
      providers: [
        AuthService,
        { provide: PrismaService, useValue: prisma },
        { provide: ConfigService, useValue: { get: (key: string) => key === 'JWT_SECRET' ? 'test_secret_key_123' : undefined } },
        { provide: EmailService, useValue: emailService },
        { provide: AuditService, useValue: audit },
      ],
    }).compile();

    service = module.get<AuthService>(AuthService);
  });

  describe('registerStart', () => {
    it('should throw if email already exists', async () => {
      prisma.user.findFirst.mockResolvedValue({ email: 'test@example.com' });
      await expect(
        service.registerStart({ email: 'test@example.com', password: 'password123', displayName: 'Test', username: '@test' })
      ).rejects.toThrow(BadRequestException);
    });

    it('should throw if username already exists', async () => {
      prisma.user.findFirst.mockResolvedValue({ username: '@taken' });
      await expect(
        service.registerStart({ email: 'new@example.com', password: 'password123', displayName: 'Test', username: '@taken' })
      ).rejects.toThrow(BadRequestException);
    });

    it('should create OTP and send email on successful start', async () => {
      prisma.user.findFirst.mockResolvedValue(null);
      prisma.otpCode.create.mockResolvedValue({});
      emailService.sendOtpCode.mockResolvedValue({});

      const result = await service.registerStart({
        email: 'new@example.com',
        password: 'password123',
        displayName: 'Test',
        username: '@newuser',
      });

      expect(result.message).toContain('Verification code sent');
      expect(prisma.otpCode.create).toHaveBeenCalled();
      expect(emailService.sendOtpCode).toHaveBeenCalled();
    });
  });

  describe('login', () => {
    it('should throw for non-existent email', async () => {
      prisma.user.findUnique.mockResolvedValue(null);
      await expect(
        service.login({ email: 'noone@example.com', password: 'pass' })
      ).rejects.toThrow(UnauthorizedException);
    });

    it('should throw for social login accounts', async () => {
      prisma.user.findUnique.mockResolvedValue({ passwordHash: null });
      await expect(
        service.login({ email: 'social@example.com', password: 'pass' })
      ).rejects.toThrow(UnauthorizedException);
    });
  });

  describe('getProfile', () => {
    it('should return user profile', async () => {
      const user = { id: 'user1', email: 'test@example.com' };
      prisma.user.findUniqueOrThrow.mockResolvedValue(user);

      const result = await service.getProfile('user1');
      expect(result).toEqual(user);
    });
  });
});
