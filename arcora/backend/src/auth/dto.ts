import { IsEmail, IsString, Matches, MinLength, MaxLength } from 'class-validator';

export class RegisterStartDto {
  @IsEmail()
  email!: string;

  @IsString()
  @MinLength(6)
  @MaxLength(128)
  password!: string;

  @IsString()
  @MinLength(2)
  displayName!: string;

  @IsString()
  @Matches(/^@?[a-zA-Z0-9_]{3,20}$/)
  username!: string;
}

export class RegisterVerifyDto {
  @IsEmail()
  email!: string;

  @IsString()
  @MinLength(4)
  @MaxLength(8)
  code!: string;
}

export class LoginDto {
  @IsEmail()
  email!: string;

  @IsString()
  password!: string;
}

export class RequestOtpDto {
  @IsEmail()
  email!: string;
}
