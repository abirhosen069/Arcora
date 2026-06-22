import { Injectable, NotFoundException, BadRequestException } from '@nestjs/common';
import { PrismaService } from '../common/prisma.service';

@Injectable()
export class AgentsService {
  constructor(private readonly prisma: PrismaService) {}

  marketplace() {
    return {
      categories: ['Research', 'Coding', 'Marketing', 'Trading', 'Operations'],
      agents: [
        {
          id: 'research-scout',
          name: 'Research Scout',
          category: 'Research',
          description: 'Summarizes markets, protocol updates, and on-chain activity before requesting approval for paid reports.',
          monthlyBudget: '100.00',
          token: 'USDC',
          reputationLabel: 'Verified testnet agent',
          riskLevel: 'low',
          permissions: ['Read public market data', 'Draft paid report requests', 'Request user approval before spend'],
        },
        {
          id: 'dev-tool-runner',
          name: 'Dev Tool Runner',
          category: 'Coding',
          description: 'Pays for API calls, hosted previews, and code-analysis tools within a strict monthly allowance.',
          monthlyBudget: '250.00',
          token: 'USDC',
          reputationLabel: 'Budget-limited agent',
          riskLevel: 'medium',
          permissions: ['Use approved developer APIs', 'Create spending requests', 'Never self-approve transactions'],
        },
        {
          id: 'merchant-ops',
          name: 'Merchant Ops Copilot',
          category: 'Operations',
          description: 'Monitors checkout volume, drafts refund workflows, and flags subscription anomalies for merchants.',
          monthlyBudget: '150.00',
          token: 'USDC',
          reputationLabel: 'Merchant suite ready',
          riskLevel: 'low',
          permissions: ['Read merchant dashboard metrics', 'Draft refund requests', 'Flag suspicious payment patterns'],
        },
      ],
      settlementToken: 'USDC',
      network: 'Arc Testnet',
      policy: 'Agents can request spend from delegated budgets but cannot bypass user biometric approval.',
    };
  }

  async createWallet(ownerId: string, name: string, description: string, monthlyBudget: string, permissions: string[]) {
    const walletAddress = `0x${Buffer.from(`${ownerId}-${name}-${Date.now()}`).toString('hex').padEnd(40, '0').slice(0, 40)}`;
    return this.prisma.agentWallet.create({
      data: {
        ownerId,
        name,
        description,
        walletAddress,
        monthlyBudget: parseFloat(monthlyBudget),
        permissions,
      },
    });
  }

  async listWallets(ownerId: string) {
    return this.prisma.agentWallet.findMany({
      where: { ownerId },
      orderBy: { createdAt: 'desc' },
    });
  }

  async getWallet(id: string) {
    const wallet = await this.prisma.agentWallet.findUnique({ where: { id } });
    if (!wallet) throw new NotFoundException('Agent wallet not found.');
    return wallet;
  }

  async updateWallet(id: string, ownerId: string, data: { name?: string; description?: string; monthlyBudget?: string; permissions?: string[] }) {
    const wallet = await this.prisma.agentWallet.findUnique({ where: { id } });
    if (!wallet) throw new NotFoundException('Agent wallet not found.');
    if (wallet.ownerId !== ownerId) throw new BadRequestException('You can only update your own agent wallets.');

    return this.prisma.agentWallet.update({
      where: { id },
      data: {
        ...(data.name && { name: data.name }),
        ...(data.description && { description: data.description }),
        ...(data.monthlyBudget && { monthlyBudget: parseFloat(data.monthlyBudget) }),
        ...(data.permissions && { permissions: data.permissions }),
      },
    });
  }

  async deleteWallet(id: string, ownerId: string) {
    const wallet = await this.prisma.agentWallet.findUnique({ where: { id } });
    if (!wallet) throw new NotFoundException('Agent wallet not found.');
    if (wallet.ownerId !== ownerId) throw new BadRequestException('You can only delete your own agent wallets.');
    return this.prisma.agentWallet.delete({ where: { id } });
  }
}
