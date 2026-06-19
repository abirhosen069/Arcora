import { TransactionStatus } from '@prisma/client';
import { IsDateString, IsEnum, IsOptional, IsString } from 'class-validator';

export class CreateSubscriptionDto {
  @IsString()
  userId!: string;

  @IsOptional()
  @IsString()
  merchantId?: string;

  @IsOptional()
  @IsString()
  agentWalletId?: string;

  @IsString()
  amount!: string;

  @IsString()
  interval!: string;

  @IsOptional()
  @IsDateString()
  nextChargeAt?: string;
}

export class UpdateSubscriptionStatusDto {
  @IsEnum(TransactionStatus)
  status!: TransactionStatus;
}
