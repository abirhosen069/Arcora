import { Body, Controller, Get, Post, Session } from '@nestjs/common';
import { CurrentUser, AuthenticatedUser } from './current-user.decorator';
import { Public } from './public.decorator';
import { AuthService } from './auth.service';
import { SignupDto, GoogleAuthDto, PasskeyRegistrationStartDto, PasskeyRegistrationFinishDto, PasskeyAuthenticationStartDto, PasskeyAuthenticationFinishDto } from './dto';

@Controller('auth')
export class AuthController {
  constructor(private readonly auth: AuthService) {}

  @Public()
  @Post('signup')
  signup(@Body() dto: SignupDto) {
    return this.auth.signup(dto);
  }

  @Public()
  @Post('google')
  googleAuth(@Body() dto: GoogleAuthDto) {
    return this.auth.googleAuth(dto);
  }

  @Public()
  @Post('passkey/register/start')
  passkeyRegistrationStart(@Body() dto: PasskeyRegistrationStartDto) {
    return this.auth.passkeyRegistrationStart(dto.email);
  }

  @Public()
  @Post('passkey/register/finish')
  passkeyRegistrationFinish(@Body() dto: PasskeyRegistrationFinishDto) {
    return this.auth.passkeyRegistrationFinish(dto, '');
  }

  @Public()
  @Post('passkey/authenticate/start')
  passkeyAuthenticationStart(@Body() dto: PasskeyAuthenticationStartDto) {
    return this.auth.passkeyAuthenticationStart(dto.email);
  }

  @Public()
  @Post('passkey/authenticate/finish')
  passkeyAuthenticationFinish(@Body() dto: PasskeyAuthenticationFinishDto) {
    return this.auth.passkeyAuthenticationFinish(dto, '');
  }

  @Get('me')
  me(@CurrentUser() user: AuthenticatedUser) {
    return this.auth.getProfile(user.id);
  }
}
