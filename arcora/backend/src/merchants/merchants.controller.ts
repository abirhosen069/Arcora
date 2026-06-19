import { Body, Controller, Get, Param, Post } from '@nestjs/common';
import { CreateCheckoutLinkDto, CreateMerchantDto } from './merchants.dto';
import { MerchantsService } from './merchants.service';

@Controller('merchants')
export class MerchantsController {
  constructor(private readonly merchants: MerchantsService) {}

  @Post()
  create(@Body() dto: CreateMerchantDto) {
    return this.merchants.create(dto);
  }

  @Get(':id/dashboard')
  dashboard(@Param('id') id: string) {
    return this.merchants.dashboard(id);
  }

  @Post(':id/checkout-links')
  createCheckoutLink(@Param('id') id: string, @Body() dto: CreateCheckoutLinkDto) {
    return this.merchants.createCheckoutLink(id, dto);
  }
}
