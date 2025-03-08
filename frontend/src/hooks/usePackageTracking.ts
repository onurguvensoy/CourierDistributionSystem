import { useState, useEffect, useRef } from 'react';
import { useStompClient } from '../contexts/stomp.context';
import { LocationData } from '../types/location';
import { StompSubscription } from '@stomp/stompjs';

export const usePackageTracking = (trackingNumber: string) => {
  const { client, connected } = useStompClient();
  const [liveLocation, setLiveLocation] = useState<LocationData | null>(null);
  const [locationHistory, setLocationHistory] = useState<LocationData[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const subscriptionRef = useRef<StompSubscription | null>(null);
  const hasRequestedHistory = useRef<boolean>(false);

  useEffect(() => {
    if (!trackingNumber) {
      setError('No tracking number provided');
      setIsLoading(false);
      return;
    }

    if (!client) {
      setError('WebSocket client not available');
      setIsLoading(false);
      return;
    }

    if (!connected) {
      setError('Not connected to server');
      setIsLoading(false);
      return;
    }

    try {

      const destination = `/topic/package/${trackingNumber}/location`;
      console.log('STOMP: Subscribing to:', destination);
      

      if (subscriptionRef.current) {
        console.log('Cleaning up existing subscription');
        subscriptionRef.current.unsubscribe();
        subscriptionRef.current = null;
      }


      subscriptionRef.current = client.subscribe(destination, (message) => {
        try {
          const data = JSON.parse(message.body);
          console.log('Received location update:', data);
          

          if (Array.isArray(data)) {
            setLocationHistory(data);
            console.log('Updated location history:', data);
          } else {

            setLiveLocation(data);
            setLocationHistory(prev => [...prev, data].slice(-50)); // Keep last 50 locations
            console.log('Updated live location:', data);
          }
        } catch (error) {
          console.error('Error parsing location message:', error);
        }
      });
      console.log('Successfully subscribed to:', destination);

      setIsLoading(false);
      setError(null);
    } catch (error) {
      console.error('Failed to subscribe to package tracking:', error);
      setError('Failed to connect to package tracking');
      setIsLoading(false);
    }

    return () => {
      if (subscriptionRef.current) {
        try {
          subscriptionRef.current.unsubscribe();
          subscriptionRef.current = null;
          console.log('Unsubscribed from package tracking');
        } catch (error) {
          console.error('Failed to unsubscribe from package tracking:', error);
        }
      }
    };
  }, [client, connected, trackingNumber]);

  return { liveLocation, locationHistory, isLoading, error };
}; 