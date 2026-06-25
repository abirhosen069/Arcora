import { Test, TestingModule } from '@nestjs/testing';
import { AgentsService } from '../agents.service';
import { PrismaService } from '../../common/prisma.service';
import { AuditService } from '../../common/audit.service';
import { NotFoundException, BadRequestException } from '@nestjs/common';

describe('AgentsService', () => {
  let service: AgentsService;
  let prisma: {
    agentWallet: { create: jest.Mock; findMany: jest.Mock; findUnique: jest.Mock; update: jest.Mock; delete: jest.Mock };
    agentListing: { findMany: jest.Mock; create: jest.Mock };
  };
  let audit: { logAgent: jest.Mock };

  beforeEach(async () => {
    prisma = {
      agentWallet: { create: jest.fn(), findMany: jest.fn(), findUnique: jest.fn(), update: jest.fn(), delete: jest.fn() },
      agentListing: { findMany: jest.fn(), create: jest.fn() },
    };
    audit = { logAgent: jest.fn() };

    const module: TestingModule = await Test.createTestingModule({
      providers: [
        AgentsService,
        { provide: PrismaService, useValue: prisma },
        { provide: AuditService, useValue: audit },
      ],
    }).compile();

    service = module.get<AgentsService>(AgentsService);
  });

  describe('marketplace', () => {
    it('should seed marketplace if empty', async () => {
      prisma.agentListing.findMany.mockResolvedValueOnce([]).mockResolvedValueOnce([
        { id: '1', name: 'Research Scout', category: 'Research', description: 'Test', monthlyBudget: { toString: () => '100' }, token: 'USDC', reputationLabel: 'Verified', riskLevel: 'low', permissions: [] },
      ]);
      prisma.agentListing.create.mockResolvedValue({});

      const result = await service.marketplace();
      expect(result.agents.length).toBeGreaterThan(0);
      expect(prisma.agentListing.create).toHaveBeenCalled();
    });

    it('should return existing marketplace', async () => {
      prisma.agentListing.findMany.mockResolvedValue([
        { id: '1', name: 'Test Agent', category: 'Coding', description: 'Desc', monthlyBudget: { toString: () => '50' }, token: 'USDC', reputationLabel: 'Label', riskLevel: 'low', permissions: ['read'] },
      ]);

      const result = await service.marketplace();
      expect(result.agents).toHaveLength(1);
      expect(result.categories).toContain('Coding');
    });
  });

  describe('createWallet', () => {
    it('should create wallet and audit', async () => {
      prisma.agentWallet.create.mockResolvedValue({ id: 'w1', name: 'My Agent' });
      const result = await service.createWallet('owner1', 'My Agent', 'Desc', '50', ['read']);
      expect(result.id).toBe('w1');
      expect(audit.logAgent).toHaveBeenCalledWith('owner1', 'agent_wallet.created', 'w1', expect.any(Object));
    });
  });

  describe('deleteWallet', () => {
    it('should throw for non-existent wallet', async () => {
      prisma.agentWallet.findUnique.mockResolvedValue(null);
      await expect(service.deleteWallet('nonexistent', 'owner1')).rejects.toThrow(NotFoundException);
    });

    it('should throw if not owner', async () => {
      prisma.agentWallet.findUnique.mockResolvedValue({ id: 'w1', ownerId: 'other' });
      await expect(service.deleteWallet('w1', 'owner1')).rejects.toThrow(BadRequestException);
    });

    it('should delete owned wallet', async () => {
      prisma.agentWallet.findUnique.mockResolvedValue({ id: 'w1', ownerId: 'owner1', name: 'Agent' });
      prisma.agentWallet.delete.mockResolvedValue({});
      await service.deleteWallet('w1', 'owner1');
      expect(prisma.agentWallet.delete).toHaveBeenCalled();
      expect(audit.logAgent).toHaveBeenCalled();
    });
  });
});
