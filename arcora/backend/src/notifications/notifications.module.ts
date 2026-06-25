import { Module } from '@nestjs/common';
import { NotificationsGateway } from './notifications.gateway';
import { PushService } from './push.service';
import { NotificationsController } from './notifications.controller';

@Module({
  controllers: [NotificationsController],
  providers: [NotificationsGateway, PushService],
  exports: [NotificationsGateway, PushService],
})
export class NotificationsModule {}
