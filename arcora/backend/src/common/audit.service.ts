import { Injectable, Logger } from '@nestjs/common';
import { PrismaService } from './prisma.service';
import { Prisma } from '@prisma/client';

export interface AuditLogEntry {
  userId?: string;
  action: string;
  entity: string;
  entityId?: string;
  metadata?: Record<string, string>;
  ipAddress?: string;
  userAgent?: string;
}

@Injectable()
export class AuditService {
  private readonly logger = new Logger(AuditService.name);

  constructor(private readonly prisma: PrismaService) {}

  async log(entry: AuditLogEntry): Promise<void> {
    try {
      await this.prisma.auditLog.create({
        data: {
          userId: entry.userId,
          action: entry.action,
          entity: entry.entity,
          entityId: entry.entityId,
          metadata: (entry.metadata as Prisma.InputJsonValue) ?? Prisma.JsonNull,
          ipAddress: entry.ipAddress,
          userAgent: entry.userAgent,
        },
      });
    } catch (error: unknown) {
      const message = error instanceof Error ? error.message : 'Unknown error';
      this.logger.error(`Failed to write audit log: ${message}`);
    }
  }

  async logAuth(userId: string, action: string, ipAddress?: string, userAgent?: string) {
    return this.log({ userId, action, entity: 'auth', ipAddress, userAgent });
  }

  async logPayment(userId: string, action: string, transactionId: string, metadata?: Record<string, string>) {
    return this.log({ userId, action, entity: 'transaction', entityId: transactionId, metadata });
  }

  async logAgent(userId: string, action: string, agentId: string, metadata?: Record<string, string>) {
    return this.log({ userId, action, entity: 'agent_wallet', entityId: agentId, metadata });
  }

  async logMerchant(userId: string, action: string, merchantId: string, metadata?: Record<string, string>) {
    return this.log({ userId, action, entity: 'merchant', entityId: merchantId, metadata });
  }

  async logSubscription(userId: string, action: string, subscriptionId: string, metadata?: Record<string, string>) {
    return this.log({ userId, action, entity: 'subscription', entityId: subscriptionId, metadata });
  }

  async logCompliance(userId: string, action: string, metadata?: Record<string, string>) {
    return this.log({ userId, action, entity: 'compliance', metadata });
  }
}
