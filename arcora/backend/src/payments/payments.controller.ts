import { Body, Controller, Get, Param, Patch, Post, Query } from '@nestjs/common';
import { CurrentUser, AuthenticatedUser } from '../auth/current-user.decorator';
import { CreatePaymentRequestDto, QuotePaymentDto, ResolvePaymentRequestDto, SendPaymentDto } from './payments.dto';
import { PaymentsService } from './payments.service';

@Controller('payments')
export class PaymentsController {
  constructor(private readonly payments: PaymentsService) {}

  @Post('request')
  request(@Body() dto: CreatePaymentRequestDto, @CurrentUser() user: AuthenticatedUser) {
    return this.payments.createRequest({ ...dto, fromUserId: user.id });
  }

  @Get('inbox')
  inbox(@Query('userId') userId: string, @CurrentUser() user: AuthenticatedUser) {
    return this.payments.inbox(userId || user.id);
  }

  @Post('quote')
  quote(@Body() dto: QuotePaymentDto) {
    return this.payments.quotePayment(dto);
  }

  @Post('send')
  send(@Body() dto: SendPaymentDto, @CurrentUser() user: AuthenticatedUser) {
    return this.payments.sendPayment(dto, user);
  }

  @Patch('request/:id')
  resolve(@Param('id') id: string, @Body() dto: ResolvePaymentRequestDto) {
    return this.payments.resolve(id, dto);
  }
}
