import { Controller, Get } from '@nestjs/common';
import { Public } from '../auth/public.decorator';
import { AgentsService } from './agents.service';

@Controller('agents')
export class AgentsController {
  constructor(private readonly agents: AgentsService) {}

  @Public()
  @Get('marketplace')
  marketplace() {
    return this.agents.marketplace();
  }
}
