import { Body, Controller, Post } from '@nestjs/common';
import { ComplianceService } from './compliance.service';
import { ScreenCounterpartyDto } from './compliance.dto';

@Controller('compliance')
export class ComplianceController {
  constructor(private readonly compliance: ComplianceService) {}

  @Post('screen-counterparty')
  screenCounterparty(@Body() dto: ScreenCounterpartyDto) {
    return this.compliance.screenCounterparty(dto.identifier, dto.amountUsd);
  }
}
