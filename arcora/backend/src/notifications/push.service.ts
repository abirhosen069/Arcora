import { Injectable, Logger, OnModuleInit } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import * as admin from 'firebase-admin';
import { PrismaService } from '../common/prisma.service';

@Injectable()
export class PushService implements OnModuleInit {
  private readonly logger = new Logger(PushService.name);
  private initialized = false;

  constructor(
    private readonly config: ConfigService,
    private readonly prisma: PrismaService,
  ) {}

  onModuleInit() {
    const serviceAccount = this.config.get<string>('FIREBASE_SERVICE_ACCOUNT');
    if (serviceAccount) {
      try {
        admin.initializeApp({
          credential: admin.credential.cert(JSON.parse(serviceAccount)),
        });
        this.initialized = true;
        this.logger.log('Firebase Admin initialized for push notifications');
      } catch (error: unknown) {
        const message = error instanceof Error ? error.message : 'Unknown error';
        this.logger.warn(`Failed to initialize Firebase Admin: ${message}`);
      }
    } else {
      this.logger.warn('FIREBASE_SERVICE_ACCOUNT not set — push notifications disabled');
    }
  }

  async registerToken(userId: string, token: string, platform: string = 'android') {
    await this.prisma.pushToken.upsert({
      where: { token },
      update: { userId, platform, lastUsedAt: new Date() },
      create: { userId, token, platform },
    });
  }

  async removeToken(token: string) {
    await this.prisma.pushToken.deleteMany({ where: { token } });
  }

  async sendToUser(userId: string, title: string, body: string, data?: Record<string, string>) {
    if (!this.initialized) return;

    const tokens = await this.prisma.pushToken.findMany({
      where: { userId },
      select: { token: true },
    });

    if (tokens.length === 0) return;

    const message: admin.messaging.MulticastMessage = {
      tokens: tokens.map((t) => t.token),
      notification: { title, body },
      data: data ?? {},
      android: {
        priority: 'high',
        notification: {
          channelId: 'arcora_transactions',
          sound: 'default',
        },
      },
    };

    try {
      const response = await admin.messaging().sendEachForMulticast(message);
      this.logger.log(`Push sent to ${userId}: ${response.successCount}/${response.successCount + response.failureCount} delivered`);

      const failedTokens: string[] = [];
      response.responses.forEach((resp: { success?: boolean }, idx: number) => {
        if (!resp.success) {
          failedTokens.push(tokens[idx].token);
        }
      });

      if (failedTokens.length > 0) {
        await this.prisma.pushToken.deleteMany({
          where: { token: { in: failedTokens } },
        });
      }
    } catch (error: unknown) {
      const message = error instanceof Error ? error.message : 'Unknown error';
      this.logger.error(`Failed to send push notification: ${message}`);
    }
  }

  async sendPaymentReceived(userId: string, amount: string, from: string) {
    return this.sendToUser(userId, 'Payment Received', `${from} sent you ${amount} USDC`, {
      type: 'payment_received',
      amount,
      from,
    });
  }

  async sendPaymentSent(userId: string, amount: string, to: string) {
    return this.sendToUser(userId, 'Payment Sent', `You sent ${amount} USDC to ${to}`, {
      type: 'payment_sent',
      amount,
      to,
    });
  }

  async sendBridgeCompleted(userId: string, amount: string) {
    return this.sendToUser(userId, 'Bridge Completed', `${amount} USDC has been bridged to Arc`, {
      type: 'bridge_completed',
      amount,
    });
  }

  async sendSubscriptionCharged(userId: string, amount: string, merchantName: string) {
    return this.sendToUser(userId, 'Subscription Charged', `${merchantName} charged ${amount} USDC`, {
      type: 'subscription_charged',
      amount,
      merchant: merchantName,
    });
  }
}
