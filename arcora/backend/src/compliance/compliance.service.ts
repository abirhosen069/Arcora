import { Injectable } from '@nestjs/common';

@Injectable()
export class ComplianceService {
  screenCounterparty(identifier: string, amountUsd: string) {
    const normalizedIdentifier = identifier.toLowerCase();
    const amount = Number.parseFloat(amountUsd);
    const hasSanctionKeyword = ['sanction', 'blocked', 'fraud', 'risk'].some((word) => normalizedIdentifier.includes(word));
    const highValue = Number.isFinite(amount) && amount >= 1000;
    const mediumValue = Number.isFinite(amount) && amount >= 250;
    const riskScore = hasSanctionKeyword ? 95 : highValue ? 72 : mediumValue ? 38 : 12;
    const requiresKybOrKyc = riskScore >= 70 || highValue;

    return {
      allowed: riskScore < 90,
      riskScore,
      reason: hasSanctionKeyword
        ? 'Counterparty matched a blocked-risk keyword in local policy pre-check.'
        : highValue
          ? 'High-value testnet transfer requires additional review before production execution.'
          : mediumValue
            ? 'Medium-value transfer passed with standard monitoring.'
            : 'Low-risk counterparty pre-check passed.',
      requiresKybOrKyc,
      source: 'arcora_local_policy',
      checkedAt: new Date().toISOString(),
    };
  }
}
