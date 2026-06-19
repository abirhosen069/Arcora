import { Body, Controller, Post } from '@nestjs/common';
import { IsString } from 'class-validator';
import { AiService } from './ai.service';

class ParseIntentDto {
  @IsString()
  input!: string;
}

@Controller('ai')
export class AiController {
  constructor(private readonly ai: AiService) {}

  @Post('parse-intent')
  parseIntent(@Body() dto: ParseIntentDto) {
    return this.ai.parseIntent(dto.input);
  }
}
