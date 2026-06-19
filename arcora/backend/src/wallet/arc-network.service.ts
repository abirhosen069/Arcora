import { Injectable } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';

@Injectable()
export class ArcNetworkService {
  constructor(private readonly config: ConfigService) {}

  getNetwork() {
    const usdcAddress =
      this.config.get<string>('ARC_USDC_ADDRESS') ?? '0x3600000000000000000000000000000000000000';

    return {
      name: this.config.get<string>('ARC_NETWORK_NAME') ?? 'Arc Testnet',
      chainId: Number(this.config.get<string>('ARC_CHAIN_ID') ?? '5042002'),
      rpcUrl: this.config.get<string>('ARC_RPC_URL') ?? 'https://rpc.testnet.arc.network',
      explorerUrl: this.config.get<string>('ARC_EXPLORER_URL') ?? 'https://testnet.arcscan.app',
      gasTrackerUrl:
        this.config.get<string>('ARC_GAS_TRACKER_URL') ?? 'https://testnet.arcscan.app/gas-tracker',
      faucetUrl: this.config.get<string>('ARC_FAUCET_URL') ?? 'https://faucet.circle.com',
      nativeCurrency: {
        name: 'USDC',
        symbol: 'USDC',
        decimals: 18,
      },
      usdcAddress,
      isNativeUsdc: usdcAddress.toLowerCase() === 'native',
    };
  }
}