import { Injectable, Logger } from '@nestjs/common';
import { PrismaService } from './prisma.service';

const IDEMPOTENCY_TTL_MS = 24 * 60 * 60 * 1000;

@Injectable()
export class IdempotencyService {
  private readonly logger = new Logger(IdempotencyService.name);

  constructor(private readonly prisma: PrismaService) {}

  async checkOrCreate(key: string, userId: string, entityType: string): Promise<{ duplicate: boolean; existingResponse?: unknown }> {
    const existing = await this.prisma.idempotencyKey.findUnique({ where: { key } });

    if (existing && existing.expiresAt > new Date()) {
      return { duplicate: true, existingResponse: existing.response };
    }

    if (existing && existing.expiresAt <= new Date()) {
      await this.prisma.idempotencyKey.delete({ where: { key } });
    }

    return { duplicate: false };
  }

  async store(key: string, userId: string, entityType: string, response: unknown, entityId?: string): Promise<void> {
    const expiresAt = new Date(Date.now() + IDEMPOTENCY_TTL_MS);

    try {
      await this.prisma.idempotencyKey.upsert({
        where: { key },
        update: { response: response as Record<string, string>, expiresAt, entityId },
        create: {
          key,
          userId,
          entityType,
          entityId,
          response: response as Record<string, string>,
          expiresAt,
        },
      });
    } catch (error: unknown) {
      const message = error instanceof Error ? error.message : 'Unknown error';
      this.logger.error(`Failed to store idempotency key: ${message}`);
    }
  }

  async cleanup(): Promise<number> {
    const result = await this.prisma.idempotencyKey.deleteMany({
      where: { expiresAt: { lt: new Date() } },
    });
    return result.count;
  }
}
