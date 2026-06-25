import { Injectable, Logger } from '@nestjs/common';
import { Cron, CronExpression } from '@nestjs/schedule';
import { PrismaService } from '../common/prisma.service';
import { TransactionExecutionService } from '../wallet/transaction-execution.service';
import { TransactionStatus, TransactionType } from '@prisma/client';

@Injectable()
export class SubscriptionsScheduler {
  private readonly logger = new Logger(SubscriptionsScheduler.name);

  constructor(
    private readonly prisma: PrismaService,
    private readonly txExecutor: TransactionExecutionService,
  ) {}

  @Cron(CronExpression.EVERY_5_MINUTES)
  async processDueSubscriptions() {
    const now = new Date();
    const dueSubscriptions = await this.prisma.subscription.findMany({
      where: {
        status: TransactionStatus.PENDING,
        nextChargeAt: { lte: now },
      },
    });

    if (dueSubscriptions.length === 0) return;

    this.logger.log(`Processing ${dueSubscriptions.length} due subscription(s)`);

    for (const sub of dueSubscriptions) {
      try {
        const user = await this.prisma.user.findUnique({ where: { id: sub.userId } });
        if (!user) {
          this.logger.warn(`User ${sub.userId} not found for subscription ${sub.id}, skipping`);
          continue;
        }

        if (!this.txExecutor.isReady()) {
          this.logger.warn('Transaction executor not ready, skipping subscription charge');
          break;
        }

        const relayerAddress = this.txExecutor.getRelayerAddress();
        if (!relayerAddress) {
          this.logger.warn('Relayer address not available, skipping subscription charge');
          break;
        }

        const txHash = await this.txExecutor.sendUsdc(
          relayerAddress,
          user.smartAccountAddress,
          sub.amount.toString(),
        );

        await this.prisma.transaction.create({
          data: {
            blockchainHash: txHash,
            senderId: sub.userId,
            amount: sub.amount,
            token: sub.token,
            type: TransactionType.SUBSCRIPTION,
            status: TransactionStatus.COMPLETED,
            metadata: { subscriptionId: sub.id, interval: sub.interval },
          },
        });

        const nextChargeAt = this.computeNextChargeDate(sub.interval);
        await this.prisma.subscription.update({
          where: { id: sub.id },
          data: {
            status: TransactionStatus.PENDING,
            nextChargeAt,
          },
        });

        this.logger.log(`Subscription ${sub.id} charged successfully, next charge: ${nextChargeAt.toISOString()}`);
      } catch (error: unknown) {
        const message = error instanceof Error ? error.message : 'Unknown error';
        this.logger.error(`Failed to charge subscription ${sub.id}: ${message}`);
        await this.prisma.subscription.update({
          where: { id: sub.id },
          data: { status: TransactionStatus.FAILED },
        });
      }
    }
  }

  private computeNextChargeDate(interval: string): Date {
    const date = new Date();
    const normalized = interval.toLowerCase();
    if (normalized.includes('daily') || normalized.includes('day')) {
      date.setDate(date.getDate() + 1);
    } else if (normalized.includes('week')) {
      date.setDate(date.getDate() + 7);
    } else if (normalized.includes('year')) {
      date.setFullYear(date.getFullYear() + 1);
    } else {
      date.setMonth(date.getMonth() + 1);
    }
    return date;
  }
}
