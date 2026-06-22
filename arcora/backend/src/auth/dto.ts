import { IsEmail, IsEthereumAddress, IsOptional, IsString, Matches, MinLength } from 'class-validator';

export class SignupDto {
  @IsEmail()
  email!: string;

  @IsString()
  @MinLength(2)
  displayName!: string;

  @IsString()
  @Matches(/^@?[a-zA-Z0-9_]{3,20}$/)
  username!: string;

  @IsOptional()
  @IsEthereumAddress()
  smartAccountAddress?: string;
}

export class GoogleAuthDto {
  @IsString()
  idToken!: string;

  @IsString()
  @MinLength(2)
  displayName!: string;

  @IsString()
  @Matches(/^@?[a-zA-Z0-9_]{3,20}$/)
  username!: string;

  @IsOptional()
  @IsEthereumAddress()
  smartAccountAddress?: string;
}

export class PasskeyRegistrationStartDto {
  @IsEmail()
  email!: string;
}

export class PasskeyRegistrationFinishDto {
  @IsString()
  id!: string;

  @IsString()
  rawId!: string;

  @IsString()
  type!: string;

  @IsString()
  attestationObject!: string;

  @IsString()
  clientDataJSON!: string;

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

export class PasskeyAuthenticationStartDto {
  @IsEmail()
  email!: string;
}

export class PasskeyAuthenticationFinishDto {
  @IsString()
  id!: string;

  @IsString()
  rawId!: string;

  @IsString()
  type!: string;

  @IsString()
  authenticatorData!: string;

  @IsString()
  clientDataJSON!: string;

  @IsString()
  signature!: string;
}
