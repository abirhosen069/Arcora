import { Controller, Get } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { PrismaService } from './common/prisma.service';
import { Public } from './auth/public.decorator';
import { TransactionExecutionService } from './wallet/transaction-execution.service';

@Controller('health')
export class HealthController {
  constructor(
    private readonly config: ConfigService,
    private readonly prisma: PrismaService,
    private readonly execution: TransactionExecutionService,
  ) {}

  @Public()
  @Get()
  async getHealth() {
    let database: 'ok' | 'error' = 'ok';
    try {
      await this.prisma.$queryRaw`SELECT 1`;
    } catch {
      database = 'error';
    }

    return {
      status: database === 'ok' ? 'ok' : 'degraded',
      service: 'arcora-backend',
      environment: this.config.get<string>('NODE_ENV') ?? 'development',
      database,
      transactionExecutionReady: this.execution.isReady(),
      relayerAddress: this.execution.getRelayerAddress(),
      timestamp: new Date().toISOString(),
    };
  }
}
