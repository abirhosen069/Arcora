import { Injectable, Logger } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import * as nodemailer from 'nodemailer';

@Injectable()
export class EmailService {
  private readonly logger = new Logger(EmailService.name);
  private transporter: nodemailer.Transporter | null = null;

  constructor(private readonly config: ConfigService) {
    const host = this.config.get<string>('SMTP_HOST');
    const port = this.config.get<number>('SMTP_PORT');
    const user = this.config.get<string>('SMTP_USER');
    const pass = this.config.get<string>('SMTP_PASS');

    if (host && user && pass) {
      this.transporter = nodemailer.createTransport({
        host,
        port: port || 587,
        secure: (port || 587) === 465,
        auth: { user, pass },
      });
      this.logger.log(`Email transporter configured: ${host}:${port}`);
    } else {
      this.logger.warn('SMTP not configured — OTP codes logged to console only');
    }
  }

  async sendOtpCode(email: string, code: string, purpose: string): Promise<void> {
    const subject = purpose === 'register'
      ? 'ArcOra — Verify your email'
      : 'ArcOra — Your login code';

    const html = `
      <div style="font-family: -apple-system, sans-serif; max-width: 480px; margin: 0 auto; padding: 32px;">
        <h2 style="color: #1a1a1a;">ArcOra</h2>
        <p style="color: #666; font-size: 16px;">
          ${purpose === 'register'
            ? 'Thanks for signing up! Enter the code below to verify your email.'
            : 'Enter the code below to log in to your account.'}
        </p>
        <div style="background: #f5f5f5; border-radius: 12px; padding: 24px; text-align: center; margin: 24px 0;">
          <span style="font-size: 32px; font-weight: bold; letter-spacing: 8px; color: #1a1a1a;">${code}</span>
        </div>
        <p style="color: #999; font-size: 13px;">
          This code expires in 10 minutes. If you didn't request this, ignore this email.
        </p>
      </div>
    `;

    if (this.transporter) {
      try {
        await this.transporter.sendMail({
          from: this.config.get<string>('SMTP_FROM') || '"ArcOra" <noreply@arcora.app>',
          to: email,
          subject,
          html,
        });
        this.logger.log(`OTP email sent to ${email}`);
      } catch (err) {
        this.logger.error(`Failed to send email to ${email}: ${err}`);
        this.logger.log(`[OTP] ${purpose} code for ${email}: ${code} (fallback: console)`);
      }
    } else {
      this.logger.log(`[OTP] ${purpose} code for ${email}: ${code}`);
    }
  }
}
