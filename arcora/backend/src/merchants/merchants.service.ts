import { Injectable, NotFoundException } from '@nestjs/common';
import { PrismaService } from '../common/prisma.service';
import { CreateCheckoutLinkDto, CreateMerchantDto } from './merchants.dto';

@Injectable()
export class MerchantsService {
  constructor(private readonly prisma: PrismaService) {}

  async create(dto: CreateMerchantDto) {
    const merchantHandle = dto.merchantHandle.startsWith('@')
      ? dto.merchantHandle.toLowerCase()
      : `@${dto.merchantHandle.toLowerCase()}`;

    return this.prisma.merchantAccount.create({
      data: {
        ownerId: dto.ownerId,
        businessName: dto.businessName,
        merchantHandle,
        settlementAddress: dto.settlementAddress.toLowerCase(),
      },
    });
  }

  async dashboard(merchantId: string) {
    const merchant = await this.prisma.merchantAccount.findUnique({ where: { id: merchantId } });
    if (!merchant) throw new NotFoundException('Merchant account not found');

    const [dailyVolume, weeklyVolume, monthlyVolume, recentTransactions] = await Promise.all([
      this.volumeSince(merchant.settlementAddress, this.daysAgo(1)),
      this.volumeSince(merchant.settlementAddress, this.daysAgo(7)),
      this.volumeSince(merchant.settlementAddress, this.daysAgo(30)),
      this.prisma.transaction.findMany({
        where: { metadata: { path: ['merchantId'], equals: merchantId } },
        orderBy: { createdAt: 'desc' },
        take: 10,
      }),
    ]);

    return {
      merchant,
      dailyVolume,
      weeklyVolume,
      monthlyVolume,
      token: 'USDC',
      recentTransactions,
    };
  }

  async createCheckoutLink(merchantId: string, dto: CreateCheckoutLinkDto) {
    const merchant = await this.prisma.merchantAccount.findUnique({ where: { id: merchantId } });
    if (!merchant) throw new NotFoundException('Merchant account not found');

    const checkoutId = `checkout_${crypto.randomUUID()}`;
    const payload = this.arcoraPayPayload({
      username: merchant.merchantHandle,
      address: merchant.settlementAddress,
      amount: dto.amount,
      memo: dto.memo ?? `Payment to ${merchant.businessName}`,
      requestId: checkoutId,
    });

    return {
      checkoutId,
      merchantId,
      businessName: merchant.businessName,
      amount: dto.amount,
      token: 'USDC',
      payload,
      checkoutUrl: `arcora://checkout/${checkoutId}?payload=${encodeURIComponent(payload)}`,
      customerReference: dto.customerReference,
      status: 'READY_FOR_QR_OR_LINK_SHARING',
    };
  }

  private async volumeSince(settlementAddress: string, since: Date) {
    const result = await this.prisma.transaction.aggregate({
      where: {
        createdAt: { gte: since },
        type: 'PAYMENT',
        status: 'COMPLETED',
        metadata: { path: ['settlementAddress'], equals: settlementAddress.toLowerCase() },
      },
      _sum: { amount: true },
    });

    return result._sum.amount?.toString() ?? '0.00';
  }

  private daysAgo(days: number) {
    const date = new Date();
    date.setDate(date.getDate() - days);
    return date;
  }

  private arcoraPayPayload(params: { username: string; address: string; amount: string; memo: string; requestId: string }) {
    const query = new URLSearchParams({
      username: params.username.replace(/^@/, ''),
      address: params.address,
      chain: 'Arc_Testnet',
      token: 'USDC',
      amount: params.amount,
      memo: params.memo,
      requestId: params.requestId,
    });
    return `arcora://pay?${query.toString()}`;
  }
}
