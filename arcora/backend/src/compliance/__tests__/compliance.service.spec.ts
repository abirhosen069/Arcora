import { Test, TestingModule } from '@nestjs/testing';
import { ComplianceService } from '../compliance.service';

describe('ComplianceService', () => {
  let service: ComplianceService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [ComplianceService],
    }).compile();

    service = module.get<ComplianceService>(ComplianceService);
  });

  describe('screenCounterparty', () => {
    it('should return low risk for clean identifier and small amount', () => {
      const result = service.screenCounterparty('alex', '50');
      expect(result.allowed).toBe(true);
      expect(result.riskScore).toBe(12);
      expect(result.requiresKybOrKyc).toBe(false);
      expect(result.reason).toContain('Low-risk');
    });

    it('should block sanctioned keyword', () => {
      const result = service.screenCounterparty('sanctioned_wallet', '100');
      expect(result.allowed).toBe(false);
      expect(result.riskScore).toBe(95);
      expect(result.requiresKybOrKyc).toBe(true);
      expect(result.reason).toContain('blocked-risk keyword');
    });

    it('should block fraud keyword', () => {
      const result = service.screenCounterparty('fraudster', '10');
      expect(result.allowed).toBe(false);
      expect(result.riskScore).toBe(95);
    });

    it('should flag high-value transactions', () => {
      const result = service.screenCounterparty('merchant123', '5000');
      expect(result.allowed).toBe(true);
      expect(result.riskScore).toBe(72);
      expect(result.requiresKybOrKyc).toBe(true);
      expect(result.reason).toContain('High-value');
    });

    it('should flag medium-value transactions', () => {
      const result = service.screenCounterparty('user42', '500');
      expect(result.allowed).toBe(true);
      expect(result.riskScore).toBe(38);
      expect(result.requiresKybOrKyc).toBe(false);
      expect(result.reason).toContain('Medium-value');
    });

    it('should handle non-numeric amount gracefully', () => {
      const result = service.screenCounterparty('test', 'not_a_number');
      expect(result.allowed).toBe(true);
      expect(result.riskScore).toBe(12);
    });

    it('should include source and checkedAt', () => {
      const result = service.screenCounterparty('test', '10');
      expect(result.source).toBe('arcora_local_policy');
      expect(result.checkedAt).toBeDefined();
    });
  });
});
