import { IsString } from 'class-validator';

export class ScreenCounterpartyDto {
  @IsString()
  identifier!: string;

  @IsString()
  amountUsd!: string;
}
