import { Controller, Get, Query } from '@nestjs/common';
import { CurrentUser, AuthenticatedUser } from '../auth/current-user.decorator';
import { ActivityService } from './activity.service';

@Controller('activity')
export class ActivityController {
  constructor(private readonly activity: ActivityService) {}

  @Get()
  list(@Query('userId') userId: string, @CurrentUser() user: AuthenticatedUser) {
    return this.activity.list(userId || user.id);
  }
}
