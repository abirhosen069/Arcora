import { Body, Controller, Post } from '@nestjs/common';
import { CurrentUser, AuthenticatedUser } from '../auth/current-user.decorator';
import { SkipThrottle } from '@nestjs/throttler';
import { PushService } from './push.service';
import { IsString } from 'class-validator';

class RegisterPushTokenDto {
  @IsString()
  token!: string;

  @IsString()
  platform!: string;
}

class RemovePushTokenDto {
  @IsString()
  token!: string;
}

@Controller('notifications')
export class NotificationsController {
  constructor(private readonly pushService: PushService) {}

  @SkipThrottle()
  @Post('push-token')
  registerToken(@CurrentUser() user: AuthenticatedUser, @Body() dto: RegisterPushTokenDto) {
    return this.pushService.registerToken(user.id, dto.token, dto.platform);
  }

  @SkipThrottle()
  @Post('push-token/remove')
  removeToken(@Body() dto: RemovePushTokenDto) {
    return this.pushService.removeToken(dto.token);
  }
}
