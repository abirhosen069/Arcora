import { Injectable } from '@nestjs/common';

@Injectable()
export class AiService {
  parseIntent(input: string) {
    const lower = input.toLowerCase();
    const amount = lower.match(/(\d+(?:\.\d+)?)/)?.[1];
    const recipient = lower.match(/@[a-z0-9_]+/)?.[0];
    const sourceChain = lower.includes('base')
      ? 'Base Sepolia'
      : lower.includes('ethereum') || lower.includes('eth')
        ? 'Ethereum Sepolia'
        : undefined;
    const action = lower.includes('send') ? 'SEND_PAYMENT'
      : lower.includes('request') ? 'REQUEST_PAYMENT'
      : lower.includes('bridge') || lower.includes('move') ? 'BRIDGE_TO_ARC'
      : lower.includes('spending') || lower.includes('spent') ? 'SHOW_SPENDING'
      : 'UNKNOWN';
    const requiresConfirmation = action !== 'SHOW_SPENDING' && action !== 'UNKNOWN';

    return {
      action,
      amount,
      recipient,
      sourceChain,
      requiresConfirmation,
      confidence: this.confidenceFor(action, amount, recipient),
      confirmationTitle: this.confirmationTitle(action, amount, recipient),
    };
  }

  private confidenceFor(action: string, amount?: string, recipient?: string) {
    if (action === 'UNKNOWN') return 0.2;
    if (action === 'SHOW_SPENDING') return 0.84;
    if (action === 'BRIDGE_TO_ARC') return amount ? 0.88 : 0.72;
    if (action === 'SEND_PAYMENT') return amount && recipient ? 0.92 : 0.68;
    if (action === 'REQUEST_PAYMENT') return amount ? 0.86 : 0.64;
    return 0.5;
  }

  private confirmationTitle(action: string, amount?: string, recipient?: string) {
    switch (action) {
      case 'SEND_PAYMENT':
        return `Send ${amount ?? 'USDC'} to ${recipient ?? 'recipient'}`;
      case 'REQUEST_PAYMENT':
        return `Request ${amount ?? 'USDC'}${recipient ? ` from ${recipient}` : ''}`;
      case 'BRIDGE_TO_ARC':
        return `Move ${amount ?? 'funds'} to Arc`;
      case 'SHOW_SPENDING':
        return 'Show spending summary';
      default:
        return 'I need more details';
    }
  }
}
