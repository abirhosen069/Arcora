import { Injectable, ServiceUnavailableException } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { createWalletClient, defineChain, http, parseUnits, type Hash } from 'viem';
import { privateKeyToAccount } from 'viem/accounts';
import { ArcNetworkService } from '../wallet/arc-network.service';

@Injectable()
export class TransactionExecutionService {
  constructor(
    private readonly config: ConfigService,
    private readonly arcNetwork: ArcNetworkService,
  ) {}

  isReady(): boolean {
    const mode = this.config.get<string>('TRANSACTION_EXECUTION_MODE') ?? 'server_relay';
    if (mode === 'disabled') {
      return false;
    }
    const key = this.config.get<string>('RELAYER_PRIVATE_KEY');
    return Boolean(key && key.startsWith('0x') && key.length === 66);
  }

  async sendUsdc(fromAddress: string, toAddress: string, amount: string): Promise<Hash> {
    if (!this.isReady()) {
      throw new ServiceUnavailableException(
        'Transaction execution is not configured. Set RELAYER_PRIVATE_KEY and fund the relayer wallet with Arc Testnet USDC.',
      );
    }

    const network = this.arcNetwork.getNetwork();
    if (network.isNativeUsdc) {
      return this.sendNativeUsdc(toAddress, amount, network);
    }

    return this.sendErc20Usdc(fromAddress, toAddress, amount, network);
  }

  getRelayerAddress(): string | null {
    const key = this.config.get<string>('RELAYER_PRIVATE_KEY');
    if (!key?.startsWith('0x')) {
      return null;
    }
    return privateKeyToAccount(key as `0x${string}`).address;
  }

  private async sendErc20Usdc(
    fromAddress: string,
    toAddress: string,
    amount: string,
    network: ReturnType<ArcNetworkService['getNetwork']>,
  ) {
    const privateKey = this.config.get<string>('RELAYER_PRIVATE_KEY') as `0x${string}`;
    const account = privateKeyToAccount(privateKey);
    const chain = this.buildChain(network);
    const client = createWalletClient({ account, chain, transport: http(network.rpcUrl) });
    const value = parseUnits(amount, 18);

    // Server relay signs from the relayer wallet. User fromAddress is recorded in metadata.
    if (fromAddress.toLowerCase() !== account.address.toLowerCase()) {
      // Custodial testnet relay: relayer sends on behalf of user after biometric approval.
    }

    return client.writeContract({
      address: network.usdcAddress as `0x${string}`,
      abi: [
        {
          type: 'function',
          name: 'transfer',
          stateMutability: 'nonpayable',
          inputs: [
            { name: 'to', type: 'address' },
            { name: 'amount', type: 'uint256' },
          ],
          outputs: [{ type: 'bool' }],
        },
      ],
      functionName: 'transfer',
      args: [toAddress as `0x${string}`, value],
    });
  }

  private async sendNativeUsdc(
    toAddress: string,
    amount: string,
    network: ReturnType<ArcNetworkService['getNetwork']>,
  ) {
    const privateKey = this.config.get<string>('RELAYER_PRIVATE_KEY') as `0x${string}`;
    const account = privateKeyToAccount(privateKey);
    const chain = this.buildChain(network);
    const client = createWalletClient({ account, chain, transport: http(network.rpcUrl) });
    const value = parseUnits(amount, 18);

    return client.sendTransaction({
      to: toAddress as `0x${string}`,
      value,
    });
  }

  private buildChain(network: ReturnType<ArcNetworkService['getNetwork']>) {
    return defineChain({
      id: network.chainId,
      name: network.name,
      nativeCurrency: network.nativeCurrency,
      rpcUrls: { default: { http: [network.rpcUrl] } },
    });
  }
}
