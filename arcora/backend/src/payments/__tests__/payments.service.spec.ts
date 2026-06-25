import { Test, TestingModule } from '@nestjs/testing';
import { PaymentsService } from '../payments.service';
import { PrismaService } from '../../common/prisma.service';
import { TransactionExecutionService } from '../../wallet/transaction-execution.service';
import { AuditService } from '../../common/audit.service';
import { IdempotencyService } from '../../common/idempotency.service';
import { ForbiddenException, ServiceUnavailableException } from '@nestjs/common';

describe('PaymentsService', () => {
  let service: PaymentsService;
  let prisma: {
    paymentRequest: { create: jest.Mock; findMany: jest.Mock; update: jest.Mock };
    transaction: { create: jest.Mock };
    user: { findFirst: jest.Mock };
  };
  let execution: { isReady: jest.Mock; getRelayerAddress: jest.Mock; sendUsdc: jest.Mock };
  let audit: { logPayment: jest.Mock };
  let idempotency: { checkOrCreate: jest.Mock; store: jest.Mock };

  beforeEach(async () => {
    prisma = {
      paymentRequest: { create: jest.fn(), findMany: jest.fn(), update: jest.fn() },
      transaction: { create: jest.fn() },
      user: { findFirst: jest.fn() },
    };
    execution = { isReady: jest.fn(), getRelayerAddress: jest.fn(), sendUsdc: jest.fn() };
    audit = { logPayment: jest.fn() };
    idempotency = { checkOrCreate: jest.fn().mockResolvedValue({ duplicate: false }), store: jest.fn() };

    const module: TestingModule = await Test.createTestingModule({
      providers: [
        PaymentsService,
        { provide: PrismaService, useValue: prisma },
        { provide: TransactionExecutionService, useValue: execution },
        { provide: AuditService, useValue: audit },
        { provide: IdempotencyService, useValue: idempotency },
      ],
    }).compile();

    service = module.get<PaymentsService>(PaymentsService);
  });

  describe('quotePayment', () => {
    it('should return quote with relayer info', () => {
      execution.isReady.mockReturnValue(true);
      execution.getRelayerAddress.mockReturnValue('0xrelayer');

      const result = service.quotePayment({
        fromAddress: '0x123',
        toAddress: '0x456',
        amount: '100',
      });

      expect(result.status).toBe('quote_ready');
      expect(result.relayerAddress).toBe('0xrelayer');
    });

    it('should indicate signing required when not ready', () => {
      execution.isReady.mockReturnValue(false);

      const result = service.quotePayment({
        fromAddress: '0x123',
        toAddress: '0x456',
        amount: '100',
      });

      expect(result.status).toBe('quote_ready_signing_required');
    });
  });

  describe('sendPayment', () => {
    it('should throw if fromAddress does not match user', async () => {
      await expect(
        service.sendPayment(
          { fromAddress: '0xother', toAddress: '0x456', amount: '100' },
          { id: 'user1', smartAccountAddress: '0x123' } as any
        )
      ).rejects.toThrow(ForbiddenException);
    });

    it('should throw if execution not ready', async () => {
      execution.isReady.mockReturnValue(false);
      await expect(
        service.sendPayment(
          { fromAddress: '0x123', toAddress: '0x456', amount: '100' },
          { id: 'user1', smartAccountAddress: '0x123' } as any
        )
      ).rejects.toThrow(ServiceUnavailableException);
    });

    it('should return cached response for duplicate idempotency key', async () => {
      execution.isReady.mockReturnValue(true);
      const cachedResponse = { id: 'cached', status: 'completed' };
      idempotency.checkOrCreate.mockResolvedValue({ duplicate: true, existingResponse: cachedResponse });

      const result = await service.sendPayment(
        { fromAddress: '0x123', toAddress: '0x456', amount: '100', idempotencyKey: 'dup_key' },
        { id: 'user1', smartAccountAddress: '0x123' } as any
      );

      expect(result).toEqual(cachedResponse);
      expect(execution.sendUsdc).not.toHaveBeenCalled();
    });
  });

  describe('createRequest', () => {
    it('should create payment request', async () => {
      prisma.paymentRequest.create.mockResolvedValue({ id: 'pr1' });
      const result = await service.createRequest({
        fromUserId: 'u1',
        toUserId: 'u2',
        amount: '50',
      });
      expect(result.id).toBe('pr1');
    });
  });
});
