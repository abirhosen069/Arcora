import { BadRequestException, Injectable, ServiceUnavailableException } from '@nestjs/common';
import { ArcNetworkService } from './arc-network.service';

type JsonRpcSuccess = { result: string };
type JsonRpcError = { error: { code: number; message: string } };
type JsonRpcResponse = JsonRpcSuccess | JsonRpcError;

const ERC20_BALANCE_OF_SELECTOR = '0x70a08231';
const ADDRESS_REGEX = /^0x[a-fA-F0-9]{40}$/;

@Injectable()
export class WalletService {
  constructor(private readonly arcNetwork: ArcNetworkService) {}

  async unifiedBalance(address: string) {
    if (!ADDRESS_REGEX.test(address)) {
      throw new BadRequestException('Invalid EVM address.');
    }

    const network = this.arcNetwork.getNetwork();
    const usdcBalance = await this.getUsdcBalance(address, network.rpcUrl, network.usdcAddress);

    return {
      address,
      chain: network.name,
      chainId: network.chainId,
      balances: [usdcBalance],
      total: usdcBalance.formatted,
      token: 'USDC',
      isNativeUsdc: network.isNativeUsdc,
      source: 'arc-testnet-rpc',
      updatedAt: new Date().toISOString(),
    };
  }

  private async getUsdcBalance(address: string, rpcUrl: string, usdcAddress: string) {
    if (usdcAddress.toLowerCase() === 'native') {
      const raw = await this.rpcCall(rpcUrl, 'eth_getBalance', [address, 'latest']);
      return this.formatBalance('USDC', raw, 18, 'native');
    }

    if (!ADDRESS_REGEX.test(usdcAddress)) {
      throw new ServiceUnavailableException('ARC_USDC_ADDRESS is not configured with a valid contract address.');
    }

    const paddedAddress = address.toLowerCase().replace(/^0x/, '').padStart(64, '0');
    const data = `${ERC20_BALANCE_OF_SELECTOR}${paddedAddress}`;
    const raw = await this.rpcCall(rpcUrl, 'eth_call', [{ to: usdcAddress, data }, 'latest']);

    return this.formatBalance('USDC', raw, 18, usdcAddress);
  }

  private async rpcCall(rpcUrl: string, method: string, params: unknown[]): Promise<string> {
    const response = await fetch(rpcUrl, {
      method: 'POST',
      headers: { 'content-type': 'application/json' },
      body: JSON.stringify({
        jsonrpc: '2.0',
        id: Date.now(),
        method,
        params,
      }),
    });

    if (!response.ok) {
      throw new ServiceUnavailableException(`Arc RPC request failed with HTTP ${response.status}.`);
    }

    const payload = (await response.json()) as JsonRpcResponse;
    if ('error' in payload) {
      throw new ServiceUnavailableException(`Arc RPC error ${payload.error.code}: ${payload.error.message}`);
    }

    return payload.result;
  }

  private formatBalance(symbol: string, rawHex: string, decimals: number, contractAddress: string) {
    const raw = BigInt(rawHex);
    const formatted = this.formatUnits(raw, decimals);

    return {
      symbol,
      raw: raw.toString(),
      formatted,
      decimals,
      contractAddress,
    };
  }

  private formatUnits(value: bigint, decimals: number) {
    const base = 10n ** BigInt(decimals);
    const whole = value / base;
    const fraction = value % base;

    if (fraction === 0n) {
      return whole.toString();
    }

    const paddedFraction = fraction.toString().padStart(decimals, '0').replace(/0+$/, '');
    return `${whole.toString()}.${paddedFraction}`;
  }
}
