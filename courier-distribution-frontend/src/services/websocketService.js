import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import authService from './authService';

class WebSocketService {
    constructor() {
        this.client = null;
        this.subscriptions = new Map();
        this.connected = false;
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 5;
    }

    connect = () => {
        const user = authService.getCurrentUser();
        if (!user || !user.token) {
            console.error('No authentication token found');
            return;
        }

        this.client = new Client({
            webSocketFactory: () => new SockJS('http://localhost:8080/websocket'),
            connectHeaders: {
                Authorization: `Bearer ${user.token}`,
            },
            debug: function (str) {
                console.debug(str);
            },
            reconnectDelay: 5000,
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000,
        });

        this.client.onConnect = () => {
            console.log('Connected to WebSocket');
            this.connected = true;
            this.reconnectAttempts = 0;
            this.subscriptions.forEach((callback, destination) => {
                this.subscribe(destination, callback);
            });
        };

        this.client.onStompError = (frame) => {
            console.error('WebSocket error:', frame);
            this.handleError(frame);
        };

        this.client.onWebSocketClose = () => {
            console.log('WebSocket connection closed');
            this.connected = false;
            this.handleReconnect();
        };

        this.client.activate();
    };

    handleError = (frame) => {
        if (frame.headers['message'] === 'Invalid JWT token') {
            authService.logout();
        }
    };

    handleReconnect = () => {
        if (this.reconnectAttempts < this.maxReconnectAttempts) {
            this.reconnectAttempts++;
            setTimeout(() => {
                console.log(`Attempting to reconnect (${this.reconnectAttempts}/${this.maxReconnectAttempts})`);
                this.connect();
            }, 5000 * this.reconnectAttempts);
        } else {
            console.error('Max reconnection attempts reached');
            authService.logout();
        }
    };

    subscribe = (destination, callback) => {
        if (!this.client || !this.connected) {
            this.subscriptions.set(destination, callback);
            return;
        }

        return this.client.subscribe(destination, (message) => {
            try {
                const data = JSON.parse(message.body);
                callback(data);
            } catch (error) {
                console.error('Error parsing message:', error);
            }
        });
    };

    unsubscribe = (destination) => {
        this.subscriptions.delete(destination);
        if (this.client && this.connected) {
            const subscription = this.client.subscriptions[destination];
            if (subscription) {
                subscription.unsubscribe();
            }
        }
    };

    send = (destination, data) => {
        if (this.client && this.connected) {
            this.client.publish({
                destination,
                body: JSON.stringify(data),
            });
        } else {
            console.warn('WebSocket not connected. Message not sent.');
        }
    };

    disconnect = () => {
        if (this.client) {
            this.client.deactivate();
            this.connected = false;
            this.subscriptions.clear();
        }
    };
}

const websocketService = new WebSocketService();
export default websocketService; 