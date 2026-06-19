import { Body, Controller, Get, Param, Patch, Post, Query } from '@nestjs/common';
import { CreateSubscriptionDto, UpdateSubscriptionStatusDto } from './subscriptions.dto';
import { SubscriptionsService } from './subscriptions.service';

@Controller('subscriptions')
export class SubscriptionsController {
  constructor(private readonly subscriptions: SubscriptionsService) {}

  @Get()
  list(@Query('userId') userId: string) {
    return this.subscriptions.list(userId);
  }

  @Post()
  create(@Body() dto: CreateSubscriptionDto) {
    return this.subscriptions.create(dto);
  }

  @Patch(':id/status')
  updateStatus(@Param('id') id: string, @Body() dto: UpdateSubscriptionStatusDto) {
    return this.subscriptions.updateStatus(id, dto);
  }

  @Post(':id/pause')
  pause(@Param('id') id: string) {
    return this.subscriptions.pause(id);
  }

  @Post(':id/renew')
  renew(@Param('id') id: string) {
    return this.subscriptions.renew(id);
  }

  @Post(':id/cancel')
  cancel(@Param('id') id: string) {
    return this.subscriptions.cancel(id);
  }
}
