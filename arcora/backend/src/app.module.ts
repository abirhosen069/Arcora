import { Module } from '@nestjs/common';
import { APP_GUARD } from '@nestjs/core';
import { ConfigModule } from '@nestjs/config';
import { ActivityModule } from './activity/activity.module';
import { AgentsModule } from './agents/agents.module';
import { AiModule } from './ai/ai.module';
import { AuthGuard } from './auth/auth.guard';
import { AuthModule } from './auth/auth.module';
import { CommonModule } from './common/common.module';
import { ComplianceModule } from './compliance/compliance.module';
import { EmailModule } from './email/email.module';
import { validateEnvironment } from './env.validation';
import { HealthController } from './health.controller';
import { MerchantsModule } from './merchants/merchants.module';
import { NotificationsModule } from './notifications/notifications.module';
import { PaymentsModule } from './payments/payments.module';
import { ReputationModule } from './reputation/reputation.module';
import { SubscriptionsModule } from './subscriptions/subscriptions.module';
import { UsersModule } from './users/users.module';
import { WalletModule } from './wallet/wallet.module';

@Module({
  imports: [
    ConfigModule.forRoot({ isGlobal: true, validate: validateEnvironment }),
    CommonModule,
    AuthModule,
    UsersModule,
    PaymentsModule,
    WalletModule,
    ActivityModule,
    AiModule,
    MerchantsModule,
    SubscriptionsModule,
    AgentsModule,
    ComplianceModule,
    NotificationsModule,
    ReputationModule,
    EmailModule,
  ],
  controllers: [HealthController],
  providers: [
    {
      provide: APP_GUARD,
      useClass: AuthGuard,
    },
  ],
})
export class AppModule {}
