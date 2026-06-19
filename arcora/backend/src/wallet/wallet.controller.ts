import { Controller, Get, Param } from '@nestjs/common';
import { Public } from '../auth/public.decorator';
import { ArcNetworkService } from './arc-network.service';
import { CircleAppKitService } from './circle-app-kit.service';
import { WalletService } from './wallet.service';

@Controller('wallet')
export class WalletController {
  constructor(
    private readonly wallet: WalletService,
    private readonly arcNetwork: ArcNetworkService,
    private readonly circleAppKit: CircleAppKitService,
  ) {}

  @Public()
  @Get('network/arc-testnet')
  arcTestnetNetwork() {
    return this.arcNetwork.getNetwork();
  }

  @Public()
  @Get('provider/circle-app-kit/status')
  circleAppKitStatus() {
    return this.circleAppKit.getStatus();
  }

  @Public()
  @Get(':address/unified-balance')
  unifiedBalance(@Param('address') address: string) {
    return this.wallet.unifiedBalance(address);
  }
}
