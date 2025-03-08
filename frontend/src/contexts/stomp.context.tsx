import React, { createContext, useContext, useEffect, useState, useRef } from 'react';
import { Client, StompSubscription } from '@stomp/stompjs';
import { useSelector } from 'react-redux';
import { RootState } from '../store';

interface StompContextType {
  client: Client | null;
  connected: boolean;
}

const StompContext = createContext<StompContextType>({ client: null, connected: false });

export const useStompClient = () => useContext(StompContext);

const WS_URL = 'ws://localhost:8080';

export const StompProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [connected, setConnected] = useState(false);
  const clientRef = useRef<Client | null>(null);
  const token = useSelector((state: RootState) => state.auth.token);
  const reconnectTimeoutRef = useRef<NodeJS.Timeout | null>(null);

  useEffect(() => {
    let stompClient: Client | null = null;

    const initializeClient = () => {
      if (!token) {
        console.log('No token available, skipping WebSocket connection');
        return;
      }


      if (stompClient?.active) {
        console.log('Deactivating existing STOMP client');
        stompClient.deactivate();
      }

      console.log('Initializing STOMP client with token:', token.substring(0, 10) + '...');

      stompClient = new Client({
        brokerURL: `${WS_URL}/ws`,
        connectHeaders: {
          Authorization: `Bearer ${token}`,
        },
        debug: (str) => {
          console.log('STOMP Debug:', str);
        },
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
        onConnect: (frame) => {
          console.log('STOMP Connected:', frame);
          setConnected(true);
        },
        onDisconnect: () => {
          console.log('STOMP Disconnected');
          setConnected(false);
        },
        onStompError: (frame) => {
          console.error('STOMP Error:', frame);
          setConnected(false);
        },
        onWebSocketError: (event) => {
          console.error('WebSocket Error:', event);
          setConnected(false);
        },
        onWebSocketClose: () => {
          console.log('WebSocket Closed');
          setConnected(false);
        },
      });

      try {
        console.log('Attempting to connect to:', `${WS_URL}/ws`);
        stompClient.activate();
        clientRef.current = stompClient;
      } catch (error) {
        console.error('Failed to activate STOMP client:', error);

        if (reconnectTimeoutRef.current) {
          clearTimeout(reconnectTimeoutRef.current);
        }
        reconnectTimeoutRef.current = setTimeout(initializeClient, 5000);
      }
    };


    initializeClient();


    const connectionCheckInterval = setInterval(() => {
      if (stompClient && !stompClient.active) {
        console.log('STOMP client inactive, attempting to reconnect');
        initializeClient();
      }
    }, 10000);

    return () => {
      clearInterval(connectionCheckInterval);
      if (reconnectTimeoutRef.current) {
        clearTimeout(reconnectTimeoutRef.current);
      }
      if (stompClient?.active) {
        console.log('Cleaning up STOMP client');
        stompClient.deactivate();
        clientRef.current = null;
        setConnected(false);
      }
    };
  }, [token]);

  return (
    <StompContext.Provider value={{ client: clientRef.current, connected }}>
      {children}
    </StompContext.Provider>
  );
};


export const usePackageUpdates = (onUpdate: (update: any) => void) => {
  const { client, connected } = useStompClient();
  const subscriptionRef = useRef<StompSubscription | null>(null);

  useEffect(() => {
    if (client && connected && !subscriptionRef.current) {
      try {
        subscriptionRef.current = client.subscribe('/topic/package/{trackingNumber}/location/status', (message) => {
          const update = JSON.parse(message.body);
          onUpdate(update);
        });
        console.log('Subscribed to package updates');
      } catch (error) {
        console.error('Failed to subscribe to package updates:', error);
      }
    }

    return () => {
      if (subscriptionRef.current) {
        try {
          subscriptionRef.current.unsubscribe();
          subscriptionRef.current = null;
          console.log('Unsubscribed from package updates');
        } catch (error) {
          console.error('Failed to unsubscribe from package updates:', error);
        }
      }
    };
  }, [client, connected, onUpdate]);

  return { connected };
}; 