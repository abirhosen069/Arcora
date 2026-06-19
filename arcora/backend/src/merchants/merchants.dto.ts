import { IsOptional, IsString, Matches, MinLength } from 'class-validator';

export class CreateMerchantDto {
  @IsString()
  ownerId!: string;

  @IsString()
  @MinLength(2)
  businessName!: string;

  @IsString()
  @Matches(/^@?[a-zA-Z0-9_]{3,24}$/)
  merchantHandle!: string;

  @IsString()
  @Matches(/^0x[a-fA-F0-9]{40}$/)
  settlementAddress!: string;
}

export class CreateCheckoutLinkDto {
  @IsString()
  amount!: string;

  @IsOptional()
  @IsString()
  memo?: string;

  @IsOptional()
  @IsString()
  customerReference?: string;
}
