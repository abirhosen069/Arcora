import { IsEmail, IsEthereumAddress, IsString, Matches, MinLength } from 'class-validator';

export class SignupDto {
  @IsEmail()
  email!: string;

  @IsString()
  @MinLength(2)
  displayName!: string;

  @IsString()
  @Matches(/^@?[a-zA-Z0-9_]{3,20}$/)
  username!: string;

  @IsEthereumAddress()
  smartAccountAddress!: string;
}
