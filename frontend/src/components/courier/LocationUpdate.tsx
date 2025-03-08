import React, { useEffect, useState } from 'react';
import { Card, Button, message } from 'antd';
import { useStompClient } from '../../contexts/stomp.context';
import { useSelector } from 'react-redux';
import { RootState } from '../../store';

interface LocationUpdateProps {
  trackingNumber: string;
}

const LocationUpdate: React.FC<LocationUpdateProps> = ({ trackingNumber }) => {
  const { client, connected } = useStompClient();
  const [isUpdating, setIsUpdating] = useState(false);
  const [currentLocation, setCurrentLocation] = useState<{ latitude: number; longitude: number } | null>(null);
  const token = useSelector((state: RootState) => state.auth.token);

  const getCurrentLocation = () => {
    return new Promise<{ latitude: number; longitude: number }>((resolve, reject) => {
      if (!navigator.geolocation) {
        reject(new Error('Geolocation is not supported by your browser'));
        return;
      }

      navigator.geolocation.getCurrentPosition(
        (position) => {
          resolve({
            latitude: position.coords.latitude,
            longitude: position.coords.longitude,
          });
        },
        (error) => {
          reject(error);
        }
      );
    });
  };

  const sendLocationUpdate = async () => {
    if (!client || !connected) {
      message.error('Not connected to server');
      return;
    }

    try {
      setIsUpdating(true);
      const location = await getCurrentLocation();
      setCurrentLocation(location);

      const locationData = {
        latitude: location.latitude.toString(),
        longitude: location.longitude.toString(),
        zone: "NYC", // This should be determined based on the location
        timestamp: new Date().toISOString(),
      };

      client.publish({
        destination: `/app/package/${trackingNumber}/location`,
        body: JSON.stringify(locationData),
        headers: {
          'content-type': 'application/json'
        }
      });

      message.success('Location updated successfully');
    } catch (error) {
      console.error('Error updating location:', error);
      message.error('Failed to update location');
    } finally {
      setIsUpdating(false);
    }
  };

  useEffect(() => {
    let intervalId: NodeJS.Timeout;

    if (connected && client) {
      // Send location updates every 30 seconds
      intervalId = setInterval(sendLocationUpdate, 30000);
    }

    return () => {
      if (intervalId) {
        clearInterval(intervalId);
      }
    };
  }, [connected, client, trackingNumber]);

  return (
    <Card title="Location Update">
      <div style={{ marginBottom: 16 }}>
        <p>Current Location:</p>
        {currentLocation ? (
          <p>
            Latitude: {currentLocation.latitude.toFixed(6)}<br />
            Longitude: {currentLocation.longitude.toFixed(6)}
          </p>
        ) : (
          <p>No location data available</p>
        )}
      </div>
      <Button
        type="primary"
        onClick={sendLocationUpdate}
        loading={isUpdating}
      >
        Update Location
      </Button>
    </Card>
  );
};

export default LocationUpdate; 