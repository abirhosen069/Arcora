import { Injectable, NotFoundException, BadRequestException } from '@nestjs/common';
import { PrismaService } from '../common/prisma.service';
import { AuditService } from '../common/audit.service';

@Injectable()
export class AgentsService {
  constructor(
    private readonly prisma: PrismaService,
    private readonly audit: AuditService,
  ) {}

  async marketplace() {
    const listings = await this.prisma.agentListing.findMany({
      where: { isActive: true },
      orderBy: { createdAt: 'asc' },
    });

    if (listings.length === 0) {
      await this.seedMarketplace();
      const seeded = await this.prisma.agentListing.findMany({
        where: { isActive: true },
        orderBy: { createdAt: 'asc' },
      });
      return this.formatMarketplace(seeded);
    }

    return this.formatMarketplace(listings);
  }

  private formatMarketplace(listings: Array<{ id: string; name: string; category: string; description: string; monthlyBudget: { toString(): string }; token: string; reputationLabel: string; riskLevel: string; permissions: unknown }>) {
    const categories = [...new Set(listings.map((l) => l.category))];
    return {
      categories,
      agents: listings.map((l) => ({
        id: l.id,
        name: l.name,
        category: l.category,
        description: l.description,
        monthlyBudget: l.monthlyBudget.toString(),
        token: l.token,
        reputationLabel: l.reputationLabel,
        riskLevel: l.riskLevel,
        permissions: l.permissions as string[],
      })),
      settlementToken: 'USDC',
      network: 'Arc Testnet',
      policy: 'Agents can request spend from delegated budgets but cannot bypass user biometric approval.',
    };
  }

  private async seedMarketplace() {
    const seeds = [
      {
        name: 'Research Scout',
        category: 'Research',
        description: 'Summarizes markets, protocol updates, and on-chain activity before requesting approval for paid reports.',
        monthlyBudget: 100,
        reputationLabel: 'Verified testnet agent',
        riskLevel: 'low',
        permissions: ['Read public market data', 'Draft paid report requests', 'Request user approval before spend'],
      },
      {
        name: 'Dev Tool Runner',
        category: 'Coding',
        description: 'Pays for API calls, hosted previews, and code-analysis tools within a strict monthly allowance.',
        monthlyBudget: 250,
        reputationLabel: 'Budget-limited agent',
        riskLevel: 'medium',
        permissions: ['Use approved developer APIs', 'Create spending requests', 'Never self-approve transactions'],
      },
      {
        name: 'Merchant Ops Copilot',
        category: 'Operations',
        description: 'Monitors checkout volume, drafts refund workflows, and flags subscription anomalies for merchants.',
        monthlyBudget: 150,
        reputationLabel: 'Merchant suite ready',
        riskLevel: 'low',
        permissions: ['Read merchant dashboard metrics', 'Draft refund requests', 'Flag suspicious payment patterns'],
      },
    ];

    for (const seed of seeds) {
      await this.prisma.agentListing.create({ data: seed });
    }
  }

  async createWallet(ownerId: string, name: string, description: string, monthlyBudget: string, permissions: string[]) {
    const walletAddress = `0x${Buffer.from(`${ownerId}-${name}-${Date.now()}`).toString('hex').padEnd(40, '0').slice(0, 40)}`;
    const wallet = await this.prisma.agentWallet.create({
      data: {
        ownerId,
        name,
        description,
        walletAddress,
        monthlyBudget: parseFloat(monthlyBudget),
        permissions,
      },
    });
    await this.audit.logAgent(ownerId, 'agent_wallet.created', wallet.id, { name, monthlyBudget });
    return wallet;
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

    const updated = await this.prisma.agentWallet.update({
      where: { id },
      data: {
        ...(data.name && { name: data.name }),
        ...(data.description && { description: data.description }),
        ...(data.monthlyBudget && { monthlyBudget: parseFloat(data.monthlyBudget) }),
        ...(data.permissions && { permissions: data.permissions }),
      },
    });
    await this.audit.logAgent(ownerId, 'agent_wallet.updated', id, {
      name: data.name ?? '',
      monthlyBudget: data.monthlyBudget ?? '',
    });
    return updated;
  }

  async deleteWallet(id: string, ownerId: string) {
    const wallet = await this.prisma.agentWallet.findUnique({ where: { id } });
    if (!wallet) throw new NotFoundException('Agent wallet not found.');
    if (wallet.ownerId !== ownerId) throw new BadRequestException('You can only delete your own agent wallets.');
    await this.audit.logAgent(ownerId, 'agent_wallet.deleted', id, { name: wallet.name });
    return this.prisma.agentWallet.delete({ where: { id } });
  }
}
