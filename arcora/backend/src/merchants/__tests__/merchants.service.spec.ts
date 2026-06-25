import { Test, TestingModule } from '@nestjs/testing';
import { MerchantsService } from '../merchants.service';
import { PrismaService } from '../../common/prisma.service';
import { NotFoundException } from '@nestjs/common';

describe('MerchantsService', () => {
  let service: MerchantsService;
  let prisma: {
    merchantAccount: { create: jest.Mock; findUnique: jest.Mock };
    transaction: { aggregate: jest.Mock; findMany: jest.Mock };
  };

  beforeEach(async () => {
    prisma = {
      merchantAccount: {
        create: jest.fn(),
        findUnique: jest.fn(),
      },
      transaction: {
        aggregate: jest.fn(),
        findMany: jest.fn(),
      },
    };

    const module: TestingModule = await Test.createTestingModule({
      providers: [
        MerchantsService,
        { provide: PrismaService, useValue: prisma },
      ],
    }).compile();

    service = module.get<MerchantsService>(MerchantsService);
  });

  describe('create', () => {
    it('should normalize merchant handle', async () => {
      prisma.merchantAccount.create.mockResolvedValue({
        id: 'm1',
        merchantHandle: '@shop',
      });

      await service.create({
        ownerId: 'owner1',
        businessName: 'Shop',
        merchantHandle: 'Shop',
        settlementAddress: '0x123',
      });

      expect(prisma.merchantAccount.create).toHaveBeenCalledWith(
        expect.objectContaining({
          data: expect.objectContaining({ merchantHandle: '@shop' }),
        })
      );
    });
  });

  describe('dashboard', () => {
    it('should throw for non-existent merchant', async () => {
      prisma.merchantAccount.findUnique.mockResolvedValue(null);
      await expect(service.dashboard('nonexistent')).rejects.toThrow(NotFoundException);
    });

    it('should return dashboard data', async () => {
      prisma.merchantAccount.findUnique.mockResolvedValue({
        id: 'm1',
        settlementAddress: '0x123',
      });
      prisma.transaction.aggregate.mockResolvedValue({ _sum: { amount: null } });
      prisma.transaction.findMany.mockResolvedValue([]);

      const result = await service.dashboard('m1');
      expect(result.merchant).toBeDefined();
      expect(result.token).toBe('USDC');
    });
  });

  describe('createCheckoutLink', () => {
    it('should throw for non-existent merchant', async () => {
      prisma.merchantAccount.findUnique.mockResolvedValue(null);
      await expect(
        service.createCheckoutLink('nonexistent', { amount: '25' })
      ).rejects.toThrow(NotFoundException);
    });

    it('should generate checkout URL', async () => {
      prisma.merchantAccount.findUnique.mockResolvedValue({
        id: 'm1',
        businessName: 'Shop',
        merchantHandle: '@shop',
        settlementAddress: '0x123',
      });

      const result = await service.createCheckoutLink('m1', { amount: '25', memo: 'Test' });
      expect(result.checkoutUrl).toContain('arcora://checkout/');
      expect(result.amount).toBe('25');
      expect(result.status).toBe('READY_FOR_QR_OR_LINK_SHARING');
    });
  });
});
