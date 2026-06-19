import { Body, Controller, Get, Post } from '@nestjs/common';
import { CurrentUser, AuthenticatedUser } from './current-user.decorator';
import { Public } from './public.decorator';
import { AuthService } from './auth.service';
import { SignupDto } from './dto';

@Controller('auth')
export class AuthController {
  constructor(private readonly auth: AuthService) {}

  @Public()
  @Post('signup')
  signup(@Body() dto: SignupDto) {
    return this.auth.signup(dto);
  }

  @Get('me')
  me(@CurrentUser() user: AuthenticatedUser) {
    return this.auth.getProfile(user.id);
  }
}
