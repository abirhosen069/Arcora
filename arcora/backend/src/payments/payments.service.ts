import {
  BadRequestException,
  ForbiddenException,
  Injectable,
  ServiceUnavailableException,
} from '@nestjs/common';
import { TransactionStatus, TransactionType } from '@prisma/client';
import { PrismaService } from '../common/prisma.service';
import { AuditService } from '../common/audit.service';
import { IdempotencyService } from '../common/idempotency.service';
import { AuthenticatedUser } from '../auth/current-user.decorator';
import { TransactionExecutionService } from '../wallet/transaction-execution.service';
import { CreatePaymentRequestDto, QuotePaymentDto, ResolvePaymentRequestDto, SendPaymentDto } from './payments.dto';

@Injectable()
export class PaymentsService {
  constructor(
    private readonly prisma: PrismaService,
    private readonly execution: TransactionExecutionService,
    private readonly audit: AuditService,
    private readonly idempotency: IdempotencyService,
  ) {}

  createRequest(dto: CreatePaymentRequestDto) {
    return this.prisma.paymentRequest.create({ data: dto });
  }

  inbox(userId: string) {
    return this.prisma.paymentRequest.findMany({
      where: { toUserId: userId },
      include: { fromUser: true },
      orderBy: { createdAt: 'desc' },
    });
  }

  quotePayment(dto: QuotePaymentDto) {
    return {
      fromAddress: dto.fromAddress,
      toAddress: dto.toAddress,
      amount: dto.amount,
      token: 'USDC',
      chain: 'Arc Testnet',
      chainId: 5042002,
      status: this.execution.isReady() ? 'quote_ready' : 'quote_ready_signing_required',
      estimatedFee: '0',
      feeToken: 'USDC',
      note: dto.note,
      signingProvider: 'arcora-server-relay',
      relayerAddress: this.execution.getRelayerAddress(),
      message: this.execution.isReady()
        ? 'Quote ready. Confirm with biometric approval to broadcast on Arc Testnet.'
        : 'Quote prepared. Configure RELAYER_PRIVATE_KEY on the server to enable broadcast.',
    };
  }

  async sendPayment(dto: SendPaymentDto, user: AuthenticatedUser) {
    if (dto.fromAddress.toLowerCase() !== user.smartAccountAddress.toLowerCase()) {
      throw new ForbiddenException('fromAddress must match the authenticated user wallet.');
    }

    if (!this.execution.isReady()) {
      throw new ServiceUnavailableException({
        status: 'execution_not_configured',
        message:
          'Server transaction relay is not configured. Set RELAYER_PRIVATE_KEY and fund the relayer with Arc Testnet USDC.',
        relayerAddress: this.execution.getRelayerAddress(),
      });
    }

    const idempotencyKey = dto.idempotencyKey ?? `payment_${user.id}_${dto.toAddress}_${dto.amount}_${Date.now()}`;
    const { duplicate, existingResponse } = await this.idempotency.checkOrCreate(idempotencyKey, user.id, 'payment');
    if (duplicate) {
      return existingResponse;
    }

    const receiver = await this.prisma.user.findFirst({
      where: { smartAccountAddress: dto.toAddress.toLowerCase() },
    });

    let blockchainHash: string;
    try {
      blockchainHash = await this.execution.sendUsdc(dto.fromAddress, dto.toAddress, dto.amount);
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Transaction broadcast failed.';
      await this.audit.logPayment(user.id, 'payment.failed', '', { error: message, toAddress: dto.toAddress, amount: dto.amount });
      throw new BadRequestException({ status: 'broadcast_failed', message });
    }

    const transaction = await this.prisma.transaction.create({
      data: {
        blockchainHash,
        senderId: user.id,
        receiverId: receiver?.id,
        amount: dto.amount,
        token: 'USDC',
        sourceChain: 'Arc Testnet',
        destinationChain: 'Arc Testnet',
        type: TransactionType.PAYMENT,
        status: TransactionStatus.COMPLETED,
        metadata: {
          note: dto.note ?? null,
          executionMode: 'server_relay',
          relayerAddress: this.execution.getRelayerAddress(),
          idempotencyKey,
        },
      },
    });

    const response = {
      id: transaction.id,
      blockchainHash,
      fromAddress: dto.fromAddress,
      toAddress: dto.toAddress,
      amount: dto.amount,
      token: 'USDC',
      chain: 'Arc Testnet',
      chainId: 5042002,
      status: 'completed',
      explorerUrl: `https://testnet.arcscan.app/tx/${blockchainHash}`,
      message: 'Payment broadcast on Arc Testnet.',
    };

    await this.idempotency.store(idempotencyKey, user.id, 'payment', response, transaction.id);
    await this.audit.logPayment(user.id, 'payment.sent', transaction.id, {
      toAddress: dto.toAddress,
      amount: dto.amount,
      blockchainHash,
    });

    return response;
  }

  async resolve(id: string, dto: ResolvePaymentRequestDto, userId?: string) {
    const result = await this.prisma.paymentRequest.update({ where: { id }, data: { status: dto.status } });
    if (userId) {
      await this.audit.logPayment(userId, `payment_request.${dto.status.toLowerCase()}`, id, { fromUserId: result.fromUserId, toUserId: result.toUserId });
    }
    return result;
  }
}
