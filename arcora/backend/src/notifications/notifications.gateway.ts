import {
  WebSocketGateway,
  WebSocketServer,
  SubscribeMessage,
  OnGatewayInit,
  OnGatewayConnection,
  OnGatewayDisconnect,
  ConnectedSocket,
  MessageBody,
} from '@nestjs/websockets';
import { Server, Socket } from 'socket.io';

@WebSocketGateway({
  cors: {
    origin: '*',
    credentials: true,
  },
  namespace: '/notifications',
})
export class NotificationsGateway implements OnGatewayInit, OnGatewayConnection, OnGatewayDisconnect {
  @WebSocketServer()
  server!: Server;

  private userSockets = new Map<string, string[]>();

  afterInit() {}

  handleConnection(client: Socket) {}

  handleDisconnect(client: Socket) {
    const userId = client.handshake.query.userId as string;
    if (userId) {
      const sockets = this.userSockets.get(userId) || [];
      this.userSockets.set(userId, sockets.filter(s => s !== client.id));
      if (this.userSockets.get(userId)?.length === 0) {
        this.userSockets.delete(userId);
      }
    }
  }

  @SubscribeMessage('auth')
  handleAuth(@ConnectedSocket() client: Socket, @MessageBody() data: { userId: string }) {
    const sockets = this.userSockets.get(data.userId) || [];
    sockets.push(client.id);
    this.userSockets.set(data.userId, sockets);
    return { event: 'auth', data: { success: true } };
  }

  sendToUser(userId: string, event: string, data: unknown) {
    const sockets = this.userSockets.get(userId);
    if (sockets) {
      sockets.forEach(socketId => {
        this.server.to(socketId).emit(event, data);
      });
    }
  }

  broadcastTransactionUpdate(userId: string, transaction: unknown) {
    this.sendToUser(userId, 'transaction:update', transaction);
  }

  broadcastPaymentRequest(userId: string, request: unknown) {
    this.sendToUser(userId, 'payment:request', request);
  }

  broadcastNotification(userId: string, notification: { title: string; message: string; type: string }) {
    this.sendToUser(userId, 'notification', notification);
  }
}
