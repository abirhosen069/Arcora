import { Injectable } from '@nestjs/common';
import { PrismaService } from '../common/prisma.service';

@Injectable()
export class ReputationService {
  constructor(private readonly prisma: PrismaService) {}

  async calculateReputation(userId: string) {
    const user = await this.prisma.user.findUnique({ where: { id: userId } });
    if (!user) return { score: 0, level: 'Unknown', factors: [] };

    const sentTransactions = await this.prisma.transaction.count({
      where: { senderId: userId, status: 'COMPLETED' },
    });

    const receivedTransactions = await this.prisma.transaction.count({
      where: { receiverId: userId, status: 'COMPLETED' },
    });

    const totalVolume = await this.prisma.transaction.aggregate({
      where: {
        OR: [{ senderId: userId }, { receiverId: userId }],
        status: 'COMPLETED',
      },
      _sum: { amount: true },
    });

    const agentWallets = await this.prisma.agentWallet.count({
      where: { ownerId: userId },
    });

    let score = 50;
    const factors: string[] = [];

    if (sentTransactions > 0) {
      score += Math.min(sentTransactions * 2, 15);
      factors.push(`${sentTransactions} completed sends (+${Math.min(sentTransactions * 2, 15)})`);
    }

    if (receivedTransactions > 0) {
      score += Math.min(receivedTransactions * 1.5, 10);
      factors.push(`${receivedTransactions} received transactions (+${Math.min(Math.round(receivedTransactions * 1.5), 10)})`);
    }

    const volume = parseFloat(totalVolume._sum.amount?.toString() || '0');
    if (volume > 1000) {
      score += 10;
      factors.push('High volume trader (+10)');
    } else if (volume > 100) {
      score += 5;
      factors.push('Active trader (+5)');
    }

    if (agentWallets > 0) {
      score += Math.min(agentWallets * 3, 9);
      factors.push(`${agentWallets} agent wallets (+${Math.min(agentWallets * 3, 9)})`);
    }

    if (user.isVerified) {
      score += 5;
      factors.push('Verified account (+5)');
    }

    score = Math.min(Math.max(score, 0), 100);

    const level = score >= 90 ? 'Platinum' : score >= 75 ? 'Gold' : score >= 60 ? 'Silver' : score >= 40 ? 'Bronze' : 'New';

    await this.prisma.user.update({
      where: { id: userId },
      data: { reputationScore: score },
    });

    return {
      score,
      level,
      factors,
      sentTransactions,
      receivedTransactions,
      totalVolume: volume.toString(),
      agentWallets,
      isVerified: user.isVerified,
    };
  }

  async getLeaderboard(limit: number = 10) {
    return this.prisma.user.findMany({
      select: {
        id: true,
        username: true,
        displayName: true,
        reputationScore: true,
        isVerified: true,
      },
      orderBy: { reputationScore: 'desc' },
      take: limit,
    });
  }
}
