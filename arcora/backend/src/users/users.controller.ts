import { Controller, Get, Param } from '@nestjs/common';
import { Public } from '../auth/public.decorator';
import { UsersService } from './users.service';

@Controller('users')
export class UsersController {
  constructor(private readonly users: UsersService) {}

  @Public()
  @Get('profile/:username')
  profile(@Param('username') username: string) {
    return this.users.requireByUsername(username);
  }
}
