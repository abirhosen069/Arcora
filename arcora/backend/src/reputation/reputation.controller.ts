import { Controller, Get, Param, Query } from '@nestjs/common';
import { Public } from '../auth/public.decorator';
import { CurrentUser, AuthenticatedUser } from '../auth/current-user.decorator';
import { ReputationService } from './reputation.service';

@Controller('reputation')
export class ReputationController {
  constructor(private readonly reputation: ReputationService) {}

  @Get('me')
  getMyReputation(@CurrentUser() user: AuthenticatedUser) {
    return this.reputation.calculateReputation(user.id);
  }

  @Public()
  @Get('user/:id')
  getUserReputation(@Param('id') userId: string) {
    return this.reputation.calculateReputation(userId);
  }

  @Public()
  @Get('leaderboard')
  getLeaderboard(@Query('limit') limit?: string) {
    return this.reputation.getLeaderboard(limit ? parseInt(limit, 10) : 10);
  }
}
