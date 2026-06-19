import { Injectable } from '@nestjs/common';

@Injectable()
export class AgentsService {
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
}
