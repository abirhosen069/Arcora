import { Test, TestingModule } from '@nestjs/testing';
import { ReputationService } from '../reputation.service';
import { PrismaService } from '../../common/prisma.service';

describe('ReputationService', () => {
  let service: ReputationService;
  let prisma: { user: { findUnique: jest.Mock; update: jest.Mock; findMany: jest.Mock }; transaction: { count: jest.Mock; aggregate: jest.Mock }; agentWallet: { count: jest.Mock } };

  beforeEach(async () => {
    prisma = {
      user: {
        findUnique: jest.fn(),
        update: jest.fn(),
        findMany: jest.fn(),
      },
      transaction: {
        count: jest.fn(),
        aggregate: jest.fn(),
      },
      agentWallet: {
        count: jest.fn(),
      },
    };

    const module: TestingModule = await Test.createTestingModule({
      providers: [
        ReputationService,
        { provide: PrismaService, useValue: prisma },
      ],
    }).compile();

    service = module.get<ReputationService>(ReputationService);
  });

  describe('calculateReputation', () => {
    it('should return score 0 for unknown user', async () => {
      prisma.user.findUnique.mockResolvedValue(null);
      const result = await service.calculateReputation('unknown-id');
      expect(result.score).toBe(0);
      expect(result.level).toBe('Unknown');
    });

    it('should calculate base score for new user with no transactions', async () => {
      prisma.user.findUnique.mockResolvedValue({ id: 'user1', isVerified: false });
      prisma.transaction.count.mockResolvedValue(0);
      prisma.transaction.aggregate.mockResolvedValue({ _sum: { amount: null } });
      prisma.agentWallet.count.mockResolvedValue(0);
      prisma.user.update.mockResolvedValue({});

      const result = await service.calculateReputation('user1');
      expect(result.score).toBe(50);
      expect(result.level).toBe('Bronze');
      expect(result.sentTransactions).toBe(0);
    });

    it('should boost score for verified user', async () => {
      prisma.user.findUnique.mockResolvedValue({ id: 'user1', isVerified: true });
      prisma.transaction.count.mockResolvedValue(0);
      prisma.transaction.aggregate.mockResolvedValue({ _sum: { amount: null } });
      prisma.agentWallet.count.mockResolvedValue(0);
      prisma.user.update.mockResolvedValue({});

      const result = await service.calculateReputation('user1');
      expect(result.score).toBe(55);
      expect(result.factors).toContainEqual(expect.stringContaining('Verified'));
    });

    it('should boost score for transactions', async () => {
      prisma.user.findUnique.mockResolvedValue({ id: 'user1', isVerified: false });
      prisma.transaction.count
        .mockResolvedValueOnce(5)
        .mockResolvedValueOnce(3);
      prisma.transaction.aggregate.mockResolvedValue({ _sum: { amount: { toString: () => '500' } } });
      prisma.agentWallet.count.mockResolvedValue(0);
      prisma.user.update.mockResolvedValue({});

      const result = await service.calculateReputation('user1');
      expect(result.score).toBeGreaterThan(50);
      expect(result.sentTransactions).toBe(5);
      expect(result.receivedTransactions).toBe(3);
    });

    it('should boost score for high volume', async () => {
      prisma.user.findUnique.mockResolvedValue({ id: 'user1', isVerified: false });
      prisma.transaction.count.mockResolvedValue(0);
      prisma.transaction.aggregate.mockResolvedValue({ _sum: { amount: { toString: () => '5000' } } });
      prisma.agentWallet.count.mockResolvedValue(2);
      prisma.user.update.mockResolvedValue({});

      const result = await service.calculateReputation('user1');
      expect(result.score).toBeGreaterThan(50);
      expect(result.factors).toContainEqual(expect.stringContaining('High volume'));
    });

    it('should cap score at 100', async () => {
      prisma.user.findUnique.mockResolvedValue({ id: 'user1', isVerified: true });
      prisma.transaction.count
        .mockResolvedValueOnce(50)
        .mockResolvedValueOnce(50);
      prisma.transaction.aggregate.mockResolvedValue({ _sum: { amount: { toString: () => '50000' } } });
      prisma.agentWallet.count.mockResolvedValue(10);
      prisma.user.update.mockResolvedValue({});

      const result = await service.calculateReputation('user1');
      expect(result.score).toBeLessThanOrEqual(100);
    });
  });

  describe('getLeaderboard', () => {
    it('should return top users', async () => {
      const users = [
        { id: 'u1', username: '@alice', displayName: 'Alice', reputationScore: 95, isVerified: true },
      ];
      prisma.user.findMany.mockResolvedValue(users);

      const result = await service.getLeaderboard(5);
      expect(result).toEqual(users);
      expect(prisma.user.findMany).toHaveBeenCalledWith(
        expect.objectContaining({ take: 5 })
      );
    });
  });
});
