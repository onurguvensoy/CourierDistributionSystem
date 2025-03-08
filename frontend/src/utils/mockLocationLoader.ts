import { readFileSync } from 'fs';
import { join } from 'path';

export interface GPSPoint {
  time: string;
  lat: number;
  lon: number;
  speed: number;
  alt: number;
  track: number;
  class: string;
  mode: number;
}

export interface MockLocationConfig {
  nodeId: number;
  startIndex?: number;
  intervalMs?: number;
}


const generateMockRoute = (startLat: number, startLon: number, endLat: number, endLon: number, points: number): GPSPoint[] => {
  const route: GPSPoint[] = [];
  const now = new Date();

  for (let i = 0; i < points; i++) {
    const progress = i / (points - 1);
    const lat = startLat + (endLat - startLat) * progress;
    const lon = startLon + (endLon - startLon) * progress;
    

    const jitter = 0.001;
    const randomLat = lat + (Math.random() - 0.5) * jitter;
    const randomLon = lon + (Math.random() - 0.5) * jitter;

    route.push({
      time: new Date(now.getTime() + i * 60000).toISOString(),
      lat: randomLat,
      lon: randomLon,
      speed: 30 + Math.random() * 10,
      alt: 100 + Math.random() * 10, // Random altitude around 100m
      track: Math.random() * 360,
      class: 'TPV',
      mode: 3
    });
  }

  return route;
};

const MOCK_ROUTES = {
  1: { start: { lat: 40.7128, lon: -74.0060 }, end: { lat: 40.7614, lon: -73.9776 } }, // NYC Downtown to Midtown
  2: { start: { lat: 34.0522, lon: -118.2437 }, end: { lat: 34.0929, lon: -118.3774 } }, // LA Downtown to Beverly Hills
  3: { start: { lat: 51.5074, lon: -0.1278 }, end: { lat: 51.5007, lon: -0.1246 } }, // London City to Westminster
  4: { start: { lat: 48.8566, lon: 2.3522 }, end: { lat: 48.8859, lon: 2.3438 } }, // Paris Center to Montmartre
  5: { start: { lat: 35.6762, lon: 139.6503 }, end: { lat: 35.6586, lon: 139.7454 } }, // Tokyo Shinjuku to Ginza
  6: { start: { lat: 22.2796, lon: 114.1724 }, end: { lat: 22.3193, lon: 114.1694 } }, // Hong Kong Central to Kowloon
  7: { start: { lat: -33.8688, lon: 151.2093 }, end: { lat: -33.8568, lon: 151.2153 } }, // Sydney CBD to The Rocks
  8: { start: { lat: 1.3521, lon: 103.8198 }, end: { lat: 1.2847, lon: 103.8610 } }, // Singapore Downtown to Marina Bay
};

let gpsTraces: { [key: number]: GPSPoint[] } = {};

// Load GPS trace data
export const loadGPSTrace = (nodeId: number): GPSPoint[] => {
  if (gpsTraces[nodeId]) {
    return gpsTraces[nodeId];
  }

  const route = MOCK_ROUTES[nodeId as keyof typeof MOCK_ROUTES];
  if (!route) {
    console.error(`No route defined for node ${nodeId}`);
    return [];
  }

  const points = generateMockRoute(
    route.start.lat,
    route.start.lon,
    route.end.lat,
    route.end.lon,
    50
  );

  gpsTraces[nodeId] = points;
  return points;
};


export const startMockLocationUpdates = (
  config: MockLocationConfig,
  onLocationUpdate: (location: GPSPoint) => void
): (() => void) => {
  const { nodeId, startIndex = 0, intervalMs = 1000 } = config;
  const trace = loadGPSTrace(nodeId);
  
  if (trace.length === 0) {
    console.error('No GPS trace data available');
    return () => {};
  }

  let currentIndex = startIndex % trace.length;
  const intervalId = setInterval(() => {
    onLocationUpdate(trace[currentIndex]);
    currentIndex = (currentIndex + 1) % trace.length;
  }, intervalMs);


  return () => clearInterval(intervalId);
};


export const getRandomNodeId = (): number => {
  return Math.floor(Math.random() * 8) + 1;
}; 