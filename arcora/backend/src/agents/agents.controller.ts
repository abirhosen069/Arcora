import { Controller, Get, Post, Patch, Delete, Body, Param, Query } from '@nestjs/common';
import { Public } from '../auth/public.decorator';
import { CurrentUser, AuthenticatedUser } from '../auth/current-user.decorator';
import { AgentsService } from './agents.service';

class CreateAgentWalletDto {
  name!: string;
  description?: string;
  monthlyBudget!: string;
  permissions: string[] = [];
}

class UpdateAgentWalletDto {
  name?: string;
  description?: string;
  monthlyBudget?: string;
  permissions?: string[];
}

@Controller('agents')
export class AgentsController {
  constructor(private readonly agents: AgentsService) {}

  @Public()
  @Get('marketplace')
  marketplace() {
    return this.agents.marketplace();
  }

  @Post('wallets')
  createWallet(@CurrentUser() user: AuthenticatedUser, @Body() dto: CreateAgentWalletDto) {
    return this.agents.createWallet(user.id, dto.name, dto.description || '', dto.monthlyBudget, dto.permissions);
  }

  @Get('wallets')
  listWallets(@CurrentUser() user: AuthenticatedUser) {
    return this.agents.listWallets(user.id);
  }

  @Get('wallets/:id')
  getWallet(@Param('id') id: string) {
    return this.agents.getWallet(id);
  }

  @Patch('wallets/:id')
  updateWallet(@CurrentUser() user: AuthenticatedUser, @Param('id') id: string, @Body() dto: UpdateAgentWalletDto) {
    return this.agents.updateWallet(id, user.id, dto);
  }

  @Delete('wallets/:id')
  deleteWallet(@CurrentUser() user: AuthenticatedUser, @Param('id') id: string) {
    return this.agents.deleteWallet(id, user.id);
  }
}
