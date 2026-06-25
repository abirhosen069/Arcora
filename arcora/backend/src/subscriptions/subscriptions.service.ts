import { BadRequestException, Injectable, NotFoundException } from '@nestjs/common';
import { TransactionStatus } from '@prisma/client';
import { PrismaService } from '../common/prisma.service';
import { AuditService } from '../common/audit.service';
import { CreateSubscriptionDto, UpdateSubscriptionStatusDto } from './subscriptions.dto';

@Injectable()
export class SubscriptionsService {
  constructor(
    private readonly prisma: PrismaService,
    private readonly audit: AuditService,
  ) {}

  list(userId: string) {
    return this.prisma.subscription.findMany({
      where: { userId },
      orderBy: { createdAt: 'desc' },
      take: 50,
    });
  }

  async create(dto: CreateSubscriptionDto) {
    if (!dto.merchantId && !dto.agentWalletId) {
      throw new BadRequestException('A subscription requires merchantId or agentWalletId.');
    }

    const subscription = await this.prisma.subscription.create({
      data: {
        userId: dto.userId,
        merchantId: dto.merchantId,
        agentWalletId: dto.agentWalletId,
        amount: dto.amount,
        interval: dto.interval,
        status: TransactionStatus.PENDING,
        nextChargeAt: dto.nextChargeAt ? new Date(dto.nextChargeAt) : this.defaultNextChargeDate(dto.interval),
      },
    });

    await this.audit.logSubscription(dto.userId, 'subscription.created', subscription.id, {
      amount: dto.amount,
      interval: dto.interval,
      merchantId: dto.merchantId ?? 'none',
      agentWalletId: dto.agentWalletId ?? 'none',
    });

    return subscription;
  }

  async updateStatus(id: string, dto: UpdateSubscriptionStatusDto, userId?: string) {
    await this.requireSubscription(id);
    const updated = await this.prisma.subscription.update({
      where: { id },
      data: { status: dto.status },
    });
    if (userId) {
      await this.audit.logSubscription(userId, `subscription.${dto.status.toLowerCase()}`, id);
    }
    return updated;
  }

  async pause(id: string, userId?: string) {
    return this.updateStatus(id, { status: TransactionStatus.REJECTED }, userId);
  }

  async renew(id: string, userId?: string) {
    await this.requireSubscription(id);
    const updated = await this.prisma.subscription.update({
      where: { id },
      data: {
        status: TransactionStatus.PENDING,
        nextChargeAt: this.defaultNextChargeDate('monthly'),
      },
    });
    if (userId) {
      await this.audit.logSubscription(userId, 'subscription.renewed', id);
    }
    return updated;
  }

  async cancel(id: string, userId?: string) {
    return this.updateStatus(id, { status: TransactionStatus.FAILED }, userId);
  }

  private async requireSubscription(id: string) {
    const subscription = await this.prisma.subscription.findUnique({ where: { id } });
    if (!subscription) throw new NotFoundException('Subscription not found');
    return subscription;
  }

  private defaultNextChargeDate(interval: string) {
    const date = new Date();
    const normalized = interval.toLowerCase();
    if (normalized.includes('week')) date.setDate(date.getDate() + 7);
    else if (normalized.includes('year')) date.setFullYear(date.getFullYear() + 1);
    else date.setMonth(date.getMonth() + 1);
    return date;
  }
}
