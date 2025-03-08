import React from 'react';
import { useParams } from 'react-router-dom';
import { Card, Typography, Timeline, Result } from 'antd';
import { EnvironmentOutlined } from '@ant-design/icons';
import { MapContainer, TileLayer, Marker, Popup, Polyline } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import L from 'leaflet';
import { usePackageTracking } from '../../hooks/usePackageTracking';

delete (L.Icon.Default.prototype as any)._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon-2x.png',
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
});

const { Title } = Typography;

const PackageTracking: React.FC = () => {
  const { trackingNumber = '' } = useParams<{ trackingNumber?: string }>();
  const { liveLocation, locationHistory, isLoading, error } = usePackageTracking(trackingNumber);

  if (!trackingNumber) {
    return (
      <Result
        status="error"
        title="Invalid Tracking Number"
        subTitle="Please provide a valid tracking number"
      />
    );
  }

  if (error) {
    return (
      <Result
        status="error"
        title="Error"
        subTitle={error}
      />
    );
  }

  if (isLoading) {
    return (
      <Result
        status="warning"
        title="Loading Package Location"
        subTitle="Please wait while we fetch the package location..."
      />
    );
  }

  const locationHistoryPoints = locationHistory.map(loc => [
    parseFloat(loc.latitude),
    parseFloat(loc.longitude)
  ]);

  return (
    <Card title={`Package Tracking - ${trackingNumber}`}>
      {(liveLocation || locationHistory.length > 0) && (
        <div style={{ marginTop: 24 }}>
          <Title level={5}>Package Location</Title>
          <div style={{ height: '400px', marginBottom: '24px' }}>
            <MapContainer
              center={
                liveLocation
                  ? [parseFloat(liveLocation.latitude), parseFloat(liveLocation.longitude)]
                  : locationHistory.length > 0
                  ? [parseFloat(locationHistory[0].latitude), parseFloat(locationHistory[0].longitude)]
                  : [0, 0]
              }
              zoom={13}
              style={{ height: '100%', width: '100%' }}
            >
              <TileLayer
                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
              />
              {liveLocation && (
                <Marker
                  position={[parseFloat(liveLocation.latitude), parseFloat(liveLocation.longitude)]}
                >
                  <Popup>
                    Current Courier Location<br />
                    Zone: {liveLocation.zone}<br />
                    Last updated: {new Date(liveLocation.timestamp).toLocaleString()}
                  </Popup>
                </Marker>
              )}
              {locationHistory.length > 0 && (
                <>
                  <Polyline
                    positions={locationHistoryPoints as [number, number][]}
                    color="blue"
                    weight={3}
                    opacity={0.6}
                  />
                  {locationHistory.map((location, index) => (
                    <Marker
                      key={location.timestamp}
                      position={[parseFloat(location.latitude), parseFloat(location.longitude)]}
                      opacity={0.7}
                    >
                      <Popup>
                        Location Update {index + 1}<br />
                        Zone: {location.zone}<br />
                        Time: {new Date(location.timestamp).toLocaleString()}
                      </Popup>
                    </Marker>
                  ))}
                </>
              )}
            </MapContainer>
          </div>
        </div>
      )}

      {locationHistory.length > 0 && (
        <div style={{ marginTop: 24 }}>
          <Title level={5}>Location History</Title>
          <Timeline
            items={locationHistory.map((location, index) => ({
              dot: <EnvironmentOutlined style={{ fontSize: '16px' }} />,
              children: (
                <div>
                  <p style={{ margin: 0 }}>
                    Zone: {location.zone}<br />
                    Latitude: {parseFloat(location.latitude).toFixed(6)}<br />
                    Longitude: {parseFloat(location.longitude).toFixed(6)}
                  </p>
                  <small style={{ color: '#999' }}>
                    {new Date(location.timestamp).toLocaleString()}
                  </small>
                </div>
              ),
            }))}
          />
        </div>
      )}
    </Card>
  );
};

export default PackageTracking; 