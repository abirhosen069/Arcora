import { Test, TestingModule } from '@nestjs/testing';
import { SubscriptionsService } from '../subscriptions.service';
import { PrismaService } from '../../common/prisma.service';
import { AuditService } from '../../common/audit.service';
import { BadRequestException, NotFoundException } from '@nestjs/common';
import { TransactionStatus } from '@prisma/client';

describe('SubscriptionsService', () => {
  let service: SubscriptionsService;
  let prisma: {
    subscription: { findMany: jest.Mock; create: jest.Mock; findUnique: jest.Mock; update: jest.Mock };
  };
  let audit: { logSubscription: jest.Mock };

  beforeEach(async () => {
    prisma = {
      subscription: { findMany: jest.fn(), create: jest.fn(), findUnique: jest.fn(), update: jest.fn() },
    };
    audit = { logSubscription: jest.fn() };

    const module: TestingModule = await Test.createTestingModule({
      providers: [
        SubscriptionsService,
        { provide: PrismaService, useValue: prisma },
        { provide: AuditService, useValue: audit },
      ],
    }).compile();

    service = module.get<SubscriptionsService>(SubscriptionsService);
  });

  describe('create', () => {
    it('should throw if neither merchantId nor agentWalletId provided', async () => {
      await expect(
        service.create({
          userId: 'u1',
          amount: '10',
          interval: 'monthly',
        })
      ).rejects.toThrow(BadRequestException);
    });

    it('should create subscription with merchantId', async () => {
      prisma.subscription.create.mockResolvedValue({ id: 'sub1' });
      const result = await service.create({
        userId: 'u1',
        merchantId: 'm1',
        amount: '10',
        interval: 'monthly',
      });
      expect(result.id).toBe('sub1');
      expect(audit.logSubscription).toHaveBeenCalled();
    });

    it('should create subscription with agentWalletId', async () => {
      prisma.subscription.create.mockResolvedValue({ id: 'sub2' });
      const result = await service.create({
        userId: 'u1',
        agentWalletId: 'a1',
        amount: '20',
        interval: 'weekly',
      });
      expect(result.id).toBe('sub2');
    });
  });

  describe('pause', () => {
    it('should throw for non-existent subscription', async () => {
      prisma.subscription.findUnique.mockResolvedValue(null);
      await expect(service.pause('nonexistent')).rejects.toThrow(NotFoundException);
    });

    it('should pause subscription', async () => {
      prisma.subscription.findUnique.mockResolvedValue({ id: 'sub1' });
      prisma.subscription.update.mockResolvedValue({ id: 'sub1', status: 'REJECTED' });
      const result = await service.pause('sub1', 'u1');
      expect(result.status).toBe('REJECTED');
    });
  });

  describe('cancel', () => {
    it('should cancel subscription', async () => {
      prisma.subscription.findUnique.mockResolvedValue({ id: 'sub1' });
      prisma.subscription.update.mockResolvedValue({ id: 'sub1', status: 'FAILED' });
      const result = await service.cancel('sub1', 'u1');
      expect(result.status).toBe('FAILED');
    });
  });

  describe('list', () => {
    it('should return subscriptions for user', async () => {
      prisma.subscription.findMany.mockResolvedValue([{ id: 'sub1' }]);
      const result = await service.list('u1');
      expect(result).toHaveLength(1);
    });
  });
});
