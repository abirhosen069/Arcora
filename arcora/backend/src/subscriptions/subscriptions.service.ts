import { BadRequestException, Injectable, NotFoundException } from '@nestjs/common';
import { TransactionStatus } from '@prisma/client';
import { PrismaService } from '../common/prisma.service';
import { CreateSubscriptionDto, UpdateSubscriptionStatusDto } from './subscriptions.dto';

@Injectable()
export class SubscriptionsService {
  constructor(private readonly prisma: PrismaService) {}

  list(userId: string) {
    return this.prisma.subscription.findMany({
      where: { userId },
      orderBy: { createdAt: 'desc' },
      take: 50,
    });
  }

  create(dto: CreateSubscriptionDto) {
    if (!dto.merchantId && !dto.agentWalletId) {
      throw new BadRequestException('A subscription requires merchantId or agentWalletId.');
    }

    return this.prisma.subscription.create({
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
  }

  async updateStatus(id: string, dto: UpdateSubscriptionStatusDto) {
    await this.requireSubscription(id);
    return this.prisma.subscription.update({
      where: { id },
      data: { status: dto.status },
    });
  }

  async pause(id: string) {
    return this.updateStatus(id, { status: TransactionStatus.REJECTED });
  }

  async renew(id: string) {
    await this.requireSubscription(id);
    return this.prisma.subscription.update({
      where: { id },
      data: {
        status: TransactionStatus.PENDING,
        nextChargeAt: this.defaultNextChargeDate('monthly'),
      },
    });
  }

  async cancel(id: string) {
    return this.updateStatus(id, { status: TransactionStatus.FAILED });
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
