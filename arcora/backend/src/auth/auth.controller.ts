import { Body, Controller, Get, Post } from '@nestjs/common';
import { CurrentUser, AuthenticatedUser } from './current-user.decorator';
import { Public } from './public.decorator';
import { AuthService } from './auth.service';
import { RegisterStartDto, RegisterVerifyDto, LoginDto, RequestOtpDto } from './dto';

@Controller('auth')
export class AuthController {
  constructor(private readonly auth: AuthService) {}

  @Public()
  @Post('register/start')
  registerStart(@Body() dto: RegisterStartDto) {
    return this.auth.registerStart(dto);
  }

  @Public()
  @Post('register/verify')
  registerVerify(@Body() body: RegisterVerifyDto & { passwordHash: string; displayName: string; username: string }) {
    return this.auth.registerVerify(body, {
      passwordHash: body.passwordHash,
      displayName: body.displayName,
      username: body.username,
    });
  }

  @Public()
  @Post('login')
  login(@Body() dto: LoginDto) {
    return this.auth.login(dto);
  }

  @Public()
  @Post('otp/register')
  requestRegisterOtp(@Body() dto: RequestOtpDto) {
    return this.auth.requestRegisterOtp(dto);
  }

  @Public()
  @Post('otp/login')
  requestLoginOtp(@Body() dto: RequestOtpDto) {
    return this.auth.requestOtp(dto);
  }

  @Get('me')
  me(@CurrentUser() user: AuthenticatedUser) {
    return this.auth.getProfile(user.id);
  }
}
