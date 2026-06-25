import { Body, Controller, Get, Param, Patch, Post, Query } from '@nestjs/common';
import { CurrentUser, AuthenticatedUser } from '../auth/current-user.decorator';
import { CreateSubscriptionDto, UpdateSubscriptionStatusDto } from './subscriptions.dto';
import { SubscriptionsService } from './subscriptions.service';

@Controller('subscriptions')
export class SubscriptionsController {
  constructor(private readonly subscriptions: SubscriptionsService) {}

  @Get()
  list(@Query('userId') userId: string, @CurrentUser() user: AuthenticatedUser) {
    return this.subscriptions.list(userId || user.id);
  }

  @Post()
  create(@Body() dto: CreateSubscriptionDto, @CurrentUser() user: AuthenticatedUser) {
    return this.subscriptions.create({ ...dto, userId: dto.userId || user.id });
  }

  @Patch(':id/status')
  updateStatus(@Param('id') id: string, @Body() dto: UpdateSubscriptionStatusDto, @CurrentUser() user: AuthenticatedUser) {
    return this.subscriptions.updateStatus(id, dto, user.id);
  }

  @Post(':id/pause')
  pause(@Param('id') id: string, @CurrentUser() user: AuthenticatedUser) {
    return this.subscriptions.pause(id, user.id);
  }

  @Post(':id/renew')
  renew(@Param('id') id: string, @CurrentUser() user: AuthenticatedUser) {
    return this.subscriptions.renew(id, user.id);
  }

  @Post(':id/cancel')
  cancel(@Param('id') id: string, @CurrentUser() user: AuthenticatedUser) {
    return this.subscriptions.cancel(id, user.id);
  }
}
