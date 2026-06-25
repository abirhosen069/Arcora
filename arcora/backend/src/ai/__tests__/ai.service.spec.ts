import { Test, TestingModule } from '@nestjs/testing';
import { AiService } from '../ai.service';

describe('AiService', () => {
  let service: AiService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [AiService],
    }).compile();

    service = module.get<AiService>(AiService);
  });

  describe('parseIntent', () => {
    it('should parse send payment with recipient and amount', () => {
      const result = service.parseIntent('Send 50 USDC to @alex');
      expect(result.action).toBe('SEND_PAYMENT');
      expect(result.amount).toBe('50');
      expect(result.recipient).toBe('@alex');
      expect(result.requiresConfirmation).toBe(true);
      expect(result.confidence).toBeGreaterThan(0.8);
    });

    it('should parse request payment', () => {
      const result = service.parseIntent('Request 100 USDC from @sarah');
      expect(result.action).toBe('REQUEST_PAYMENT');
      expect(result.amount).toBe('100');
      expect(result.recipient).toBe('@sarah');
      expect(result.requiresConfirmation).toBe(true);
    });

    it('should parse bridge intent', () => {
      const result = service.parseIntent('Bridge 200 USDC to Arc');
      expect(result.action).toBe('BRIDGE_TO_ARC');
      expect(result.amount).toBe('200');
      expect(result.requiresConfirmation).toBe(true);
    });

    it('should detect base source chain', () => {
      const result = service.parseIntent('Move 50 from Base to Arc');
      expect(result.sourceChain).toBe('Base Sepolia');
    });

    it('should detect ethereum source chain', () => {
      const result = service.parseIntent('Move 50 from Ethereum to Arc');
      expect(result.sourceChain).toBe('Ethereum Sepolia');
    });

    it('should parse spending query', () => {
      const result = service.parseIntent('Show my spending this month');
      expect(result.action).toBe('SHOW_SPENDING');
      expect(result.requiresConfirmation).toBe(false);
      expect(result.confidence).toBe(0.84);
    });

    it('should return UNKNOWN for unrecognized input', () => {
      const result = service.parseIntent('hello world');
      expect(result.action).toBe('UNKNOWN');
      expect(result.confidence).toBe(0.2);
      expect(result.requiresConfirmation).toBe(false);
    });

    it('should generate confirmation title', () => {
      const result = service.parseIntent('Send 25 USDC to @bob');
      expect(result.confirmationTitle).toContain('25');
      expect(result.confirmationTitle).toContain('@bob');
    });
  });
});
