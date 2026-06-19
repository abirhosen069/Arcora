import { CanActivate, ExecutionContext, Injectable, UnauthorizedException } from '@nestjs/common';
import { Reflector } from '@nestjs/core';
import { PrismaService } from '../common/prisma.service';
import { IS_PUBLIC_KEY } from './public.decorator';
import { SessionService } from './session.service';

@Injectable()
export class AuthGuard implements CanActivate {
  constructor(
    private readonly reflector: Reflector,
    private readonly sessions: SessionService,
    private readonly prisma: PrismaService,
  ) {}

  async canActivate(context: ExecutionContext): Promise<boolean> {
    const isPublic = this.reflector.getAllAndOverride<boolean>(IS_PUBLIC_KEY, [
      context.getHandler(),
      context.getClass(),
    ]);
    if (isPublic) {
      return true;
    }

    const request = context.switchToHttp().getRequest<{
      headers: Record<string, string | string[] | undefined>;
      user?: unknown;
    }>();

    const authorization = request.headers.authorization;
    const header = Array.isArray(authorization) ? authorization[0] : authorization;
    if (!header?.startsWith('Bearer ')) {
      throw new UnauthorizedException('Missing or invalid authorization token.');
    }

    const token = header.slice('Bearer '.length).trim();
    const payload = this.sessions.verifyAccessToken(token);
    if (!payload) {
      throw new UnauthorizedException('Session expired or invalid.');
    }

    const user = await this.prisma.user.findUnique({ where: { id: payload.sub } });
    if (!user || user.status !== 'ACTIVE') {
      throw new UnauthorizedException('User account is not active.');
    }

    request.user = {
      id: user.id,
      email: user.email,
      username: user.username,
      smartAccountAddress: user.smartAccountAddress,
    };

    return true;
  }
}
