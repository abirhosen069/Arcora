import { TransactionStatus } from '@prisma/client';
import { IsEnum, IsOptional, IsString, Matches } from 'class-validator';

export class CreatePaymentRequestDto {
  @IsString()
  fromUserId!: string;

  @IsString()
  toUserId!: string;

  @IsString()
  amount!: string;

  @IsOptional()
  @IsString()
  note?: string;
}

export class ResolvePaymentRequestDto {
  @IsEnum(TransactionStatus)
  status!: TransactionStatus;
}


export class QuotePaymentDto {
  @IsString()
  @Matches(/^0x[a-fA-F0-9]{40}$/)
  fromAddress!: string;

  @IsString()
  @Matches(/^0x[a-fA-F0-9]{40}$/)
  toAddress!: string;

  @IsString()
  amount!: string;

  @IsOptional()
  @IsString()
  note?: string;
}

export class SendPaymentDto extends QuotePaymentDto {
  @IsOptional()
  @IsString()
  userOperationHash?: string;

  @IsOptional()
  @IsString()
  idempotencyKey?: string;
}
