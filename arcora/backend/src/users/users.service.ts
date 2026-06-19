import { Injectable, NotFoundException } from '@nestjs/common';
import { PrismaService } from '../common/prisma.service';

@Injectable()
export class UsersService {
  constructor(private readonly prisma: PrismaService) {}

  findByUsername(username: string) {
    const normalized = username.startsWith('@') ? username.toLowerCase() : `@${username.toLowerCase()}`;
    return this.prisma.user.findUnique({ where: { username: normalized } });
  }

  async requireByUsername(username: string) {
    const user = await this.findByUsername(username);
    if (!user) throw new NotFoundException('User not found');
    return user;
  }
}
