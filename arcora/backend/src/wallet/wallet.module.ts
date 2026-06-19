import { Module } from '@nestjs/common';
import { ArcNetworkService } from './arc-network.service';
import { CircleAppKitService } from './circle-app-kit.service';
import { TransactionExecutionService } from './transaction-execution.service';
import { WalletController } from './wallet.controller';
import { WalletService } from './wallet.service';

@Module({
  controllers: [WalletController],
  providers: [WalletService, ArcNetworkService, CircleAppKitService, TransactionExecutionService],
  exports: [ArcNetworkService, TransactionExecutionService],
})
export class WalletModule {}
