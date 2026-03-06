import { Inject, Injectable, PLATFORM_ID } from '@angular/core';
import { HttpClient, HttpHeaders } from "@angular/common/http";
import { isPlatformBrowser } from '@angular/common';
import { Observable, switchMap, forkJoin } from 'rxjs';
import { map } from 'rxjs/operators';

export interface RouteResponseDTO {
  originLat: number;
  originLng: number;
  originAddress: string;
  destinationLat: number;
  destinationLng: number;
  destinationAddress: string;
  eta: string;
  distanceKm: number;
  durationSeconds: number;
}


//fix leaflet marker icon issue with angular
// delete(L.Icon.Default.prototype as any)._getIconUrl;

// L.Icon.Default.mergeOptions({
//   iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon-2x.png',
//   iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon.png',
//   shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-shadow.png',
// });

@Injectable({
  providedIn: 'root',
})
export class MapService {
  private map: any;
  private L: any; // Leaflet instance
  private driverMarker: any;
  private destinationMarker: any;
  private routeLayer: any; // Can be a single polyline or LayerGroup for multi-segment routes
  private ORS_API_KEY = 'eyJvcmciOiI1YjNjZTM1OTc4NTExMTAwMDFjZjYyNDgiLCJpZCI6IjJlNTg1M2Q3ZDBmMzRmZmNiM2Q4ZjUxN2IzZTgwM2ZhIiwiaCI6Im11cm11cjY0In0=';
  public eta: string = '';

  constructor(
    private http: HttpClient,
    @Inject(PLATFORM_ID) private platformId: Object,
  ) {}

  async initMap(containerId: string): Promise<void> {
    // Only run in the browser — never on the server
    if (!isPlatformBrowser(this.platformId)) return;

    // Dynamically import Leaflet (avoids SSR window error)
    this.L = await import('leaflet');

    // Fix marker icons
    delete (this.L.Icon.Default.prototype as any)._getIconUrl;
    this.L.Icon.Default.mergeOptions({
      iconRetinaUrl:
        'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon-2x.png',
      iconUrl:
        'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon.png',
      shadowUrl:
        'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-shadow.png',
    });

    this.map = this.L.map(containerId).setView([-25.7479, 28.2293], 13);

    this.L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap contributors',
      maxZoom: 19,
    }).addTo(this.map);
  }

  updateDriverLocation(lat: number, lng: number): void {
    if (!this.map || !this.L) return;

    const pos = this.L.latLng(lat, lng);

    if (this.driverMarker) {
      this.driverMarker.setLatLng(pos);
    } else {
      const driverIcon = this.L.divIcon({
        className: '',
        html: `<div style="background:#e91e8c;width:20px;height:20px;
               border-radius:50%;border:3px solid white;
               box-shadow:0 2px 8px rgba(0,0,0,0.4)"></div>`,
        iconSize: [20, 20],
        iconAnchor: [10, 10],
      });
      this.driverMarker = this.L.marker(pos, { icon: driverIcon })
        .addTo(this.map)
        .bindPopup('You are here');
    }
  }

  setDestination(address: string, city: string, postalCode: string): void {
    if (!this.map || !this.L) return;

    const query = encodeURIComponent(`${address} ${city} South Africa`);
    const url = `https://photon.komoot.io/api/?q=${query}&limit=1`;

    this.http.get<any>(url).subscribe({
      next: (result) => {
        if (!result.features?.length) {
          console.warn('No geocoding results for:', address, city);
          return;
        }

        const [lng, lat] = result.features[0].geometry.coordinates;
        const destPos = this.L.latLng(lat, lng);

        if (this.destinationMarker) {
          this.destinationMarker.setLatLng(destPos);
        } else {
          this.destinationMarker = this.L.marker(destPos)
            .addTo(this.map)
            .bindPopup(`📦 Deliver here: ${address}, ${city}`)
            .openPopup();
        }

        const bounds = this.driverMarker
          ? this.L.latLngBounds([this.driverMarker.getLatLng(), destPos]).pad(
              0.3,
            )
          : this.L.latLngBounds([destPos]).pad(0.5);

        this.map.fitBounds(bounds);
        if (this.driverMarker)
          this.drawRoute(this.driverMarker.getLatLng(), destPos);
      },
      error: (err) => console.error('Geocoding failed:', err),
    });
  }

 private drawRoute(from: any, to: any): void {
  if (this.routeLayer) this.map.removeLayer(this.routeLayer);

  const headers = new HttpHeaders({
    'Authorization': this.ORS_API_KEY,
    'Content-Type': 'application/json'
  });

  const body = {
    coordinates: [[from.lng, from.lat], [to.lng, to.lat]]
  };

  this.http.post<any>(
    'https://api.openrouteservice.org/v2/directions/driving-car/geojson',
    body,
    { headers }
  ).subscribe({
    next: (data) => {
      const coords = data.features[0].geometry.coordinates.map(
        ([lng, lat]: [number, number]) => this.L.latLng(lat, lng)
      );

      // Extract ETA from response
      const summary = data.features[0].properties.summary;
      const durationSeconds = summary.duration;
      const distanceMeters = summary.distance;
      this.eta = this.formatEta(durationSeconds, distanceMeters);

      this.routeLayer = this.L.polyline(coords, {
        color: '#e91e8c',
        weight: 5,
        opacity: 0.8,
      }).addTo(this.map);
    },
    error: (err) => {
      console.error('Routing failed:', err);
      this.eta = '';
      this.routeLayer = this.L.polyline([from, to], {
        color: '#e91e8c',
        weight: 4,
        dashArray: '10, 10',
        opacity: 0.8,
      }).addTo(this.map);
    }
  });
}

  private formatEta(seconds: number, meters: number): string {
  const minutes = Math.round(seconds / 60);
  const km = (meters / 1000).toFixed(1);

  if (minutes < 60) {
    return `${minutes} min away · ${km} km`;
  }

  const hours = Math.floor(minutes / 60);
  const remainingMins = minutes % 60;
  return `${hours}h ${remainingMins}min away · ${km} km`;
}

  /**
   * Takes a RouteResponseDTO (from getOrderRoute) and draws it on the Leaflet map:
   * - Places a shop marker at the store (origin) coords
   * - Places a package marker at the delivery (destination) coords
   * - Draws the road route polyline via ORS
   * - Sets this.eta so the ETA banner in the template updates
   */
  showOrderRoute(route: RouteResponseDTO): void {
    if (!this.map || !this.L) return;

    // Store marker
    const storeIcon = this.L.divIcon({
      className: '',
      html: `<div style="background:#4caf50;width:22px;height:22px;
             border-radius:50%;border:3px solid white;
             box-shadow:0 2px 8px rgba(0,0,0,0.4);display:flex;
             align-items:center;justify-content:center;font-size:11px;">🏪</div>`,
      iconSize: [22, 22],
      iconAnchor: [11, 11],
    });

    const originPos = this.L.latLng(route.originLat, route.originLng);
    const destPos   = this.L.latLng(route.destinationLat, route.destinationLng);

    // Remove old destination marker and place store + delivery markers
    if (this.destinationMarker) this.map.removeLayer(this.destinationMarker);

    this.L.marker(originPos, { icon: storeIcon })
      .addTo(this.map)
      .bindPopup(`🏪 Pickup: ${route.originAddress}`);

    this.destinationMarker = this.L.marker(destPos)
      .addTo(this.map)
      .bindPopup(`📦 Deliver to: ${route.destinationAddress}`)
      .openPopup();

    // Fit map to show both markers
    this.map.fitBounds(this.L.latLngBounds([originPos, destPos]).pad(0.25));

    // Draw the road route polyline via ORS directly (coords already available)
    this.drawRoute(originPos, destPos);

    // Set ETA from the backend response (already formatted)
    this.eta = route.eta;
  }

  /**
   * Geocodes storeAddress and deliveryAddress via Photon (browser — no rate limiting issues),
   * then POSTs the coordinates to the backend which calls ORS routing and returns ETA + distance.
   *
   * Usage:
   *   this.mapService.getOrderRoute(2, '73 Juta St Braamfontein', 'Sandton Johannesburg', token)
   *     .subscribe(route => console.log(route.eta));
   */
  getOrderRoute(
    storeAddress: string,
    deliveryAddress: string,
    backendBaseUrl: string,
    token: string
  ): Observable<RouteResponseDTO> {
    const geocode = (query: string): Observable<[number, number]> => {
      const url = `https://photon.komoot.io/api/?q=${encodeURIComponent(query + ' South Africa')}&limit=1`;
      return this.http.get<any>(url).pipe(
        map(res => {
          const coords = res.features?.[0]?.geometry?.coordinates;
          if (!coords) throw new Error(`Could not geocode: ${query}`);
          return [coords[1], coords[0]] as [number, number]; // [lat, lng]
        })
      );
    };

    return forkJoin({
      origin: geocode(storeAddress),
      dest: geocode(deliveryAddress)
    }).pipe(
      switchMap(({ origin, dest }) => {
        const body = {
          originLat: origin[0],
          originLng: origin[1],
          originAddress: storeAddress,
          destLat: dest[0],
          destLng: dest[1],
          destAddress: deliveryAddress
        };
        const headers = new HttpHeaders({
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        });
        return this.http.post<RouteResponseDTO>(
          `${backendBaseUrl}/api/map/route/by-coords`,
          body,
          { headers }
        );
      })
    );
  }

  /**
   * Multi-stop routing: geocodes all store addresses + delivery address,
   * then calls POST /api/map/route/multi-stop on the backend with all waypoints.
   * Returns a route with multiple stops (stores) before final delivery.
   */
  getMultiStopRoute(
    storeAddresses: string[],
    deliveryAddress: string,
    backendBaseUrl: string,
    token: string
  ): Observable<any> {
    const geocode = (query: string): Observable<[number, number]> => {
      const url = `https://photon.komoot.io/api/?q=${encodeURIComponent(query + ' South Africa')}&limit=1`;
      return this.http.get<any>(url).pipe(
        map(res => {
          const coords = res.features?.[0]?.geometry?.coordinates;
          if (!coords) throw new Error(`Could not geocode: ${query}`);
          return [coords[1], coords[0]] as [number, number]; // [lat, lng]
        })
      );
    };

    // Geocode all stores + delivery
    const geocodeRequests = [
      ...storeAddresses.map(addr => geocode(addr)),
      geocode(deliveryAddress)
    ];

    return forkJoin(geocodeRequests).pipe(
      switchMap((coords) => {
        // All coords except last are stores, last is delivery
        const storeCoords = coords.slice(0, -1);
        const deliveryCoords = coords[coords.length - 1];

        const body = {
          storeCoordinates: storeCoords.map(([lat, lng]) => ({ lat, lng })),
          deliveryCoordinates: { lat: deliveryCoords[0], lng: deliveryCoords[1] },
          storeAddresses,
          deliveryAddress
        };

        const headers = new HttpHeaders({
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        });

        return this.http.post<any>(
          `${backendBaseUrl}/api/map/route/multi-stop`,
          body,
          { headers }
        );
      })
    );
  }

  /**
   * Displays a multi-stop route on the map:
   * - Places store markers for each pickup location
   * - Places delivery marker at final destination
   * - Draws the full polyline connecting all waypoints
   */
  showMultiStopRoute(route: any): void {
    if (!this.map || !this.L) return;

    // Remove old route layer
    if (this.routeLayer) this.map.removeLayer(this.routeLayer);
    if (this.destinationMarker) this.map.removeLayer(this.destinationMarker);

    const bounds = this.L.latLngBounds([]);

    // Place numbered store markers
    if (route.storeCoordinates) {
      route.storeCoordinates.forEach((store: any, index: number) => {
        const pos = this.L.latLng(store.lat, store.lng);
        const stopNumber = index + 1;
        const storeAddress = route.storeAddresses?.[index] || 'Store';
        
        // Create numbered marker icon
        const numberedIcon = this.L.divIcon({
          className: '',
          html: `<div style="background:linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                 width:32px;height:32px;border-radius:50%;
                 border:3px solid white;box-shadow:0 3px 10px rgba(0,0,0,0.4);
                 display:flex;align-items:center;justify-content:center;
                 color:white;font-weight:bold;font-size:14px;">
                 ${stopNumber}
                 </div>`,
          iconSize: [32, 32],
          iconAnchor: [16, 16],
        });

        const marker = this.L.marker(pos, { icon: numberedIcon })
          .addTo(this.map)
          .bindPopup(`<div style="text-align:center;font-weight:500;">
                      <div style="font-size:16px;margin-bottom:4px;">🏪 Pickup Stop ${stopNumber}</div>
                      <div style="font-size:13px;color:#666;">${storeAddress}</div>
                      </div>`);
        
        // Open popup for first store
        if (index === 0) {
          marker.openPopup();
        }
        
        bounds.extend(pos);
      });
    }

    // Place delivery marker (final destination)
    if (route.deliveryCoordinates) {
      const destPos = this.L.latLng(route.deliveryCoordinates.lat, route.deliveryCoordinates.lng);
      const deliveryIcon = this.L.divIcon({
        className: '',
        html: `<div style="background:#ff3366;width:36px;height:36px;
               border-radius:50%;border:3px solid white;
               box-shadow:0 3px 10px rgba(0,0,0,0.4);display:flex;
               align-items:center;justify-content:center;font-size:18px;">📦</div>`,
        iconSize: [36, 36],
        iconAnchor: [18, 18],
      });
      
      this.destinationMarker = this.L.marker(destPos, { icon: deliveryIcon })
        .addTo(this.map)
        .bindPopup(`<div style="text-align:center;font-weight:500;">
                    <div style="font-size:16px;margin-bottom:4px;">📦 Delivery Destination</div>
                    <div style="font-size:13px;color:#666;">${route.deliveryAddress || 'Customer'}</div>
                    </div>`)
        .openPopup();
      bounds.extend(destPos);
    }

    // Draw polyline from ORS route with different colors for each segment
    if (route.geometry?.coordinates && route.segments) {
      const allCoords = route.geometry.coordinates.map(
        ([lng, lat]: [number, number]) => this.L.latLng(lat, lng)
      );

      // Define colors for each segment (store-to-store, then final store-to-delivery)
      const segmentColors = [
        '#667eea',  // Purple for first segment
        '#f093fb',  // Pink for second segment
        '#4facfe',  // Blue for third segment
        '#43e97b',  // Green for fourth segment
        '#fa709a',  // Coral for fifth segment
        '#feca57',  // Yellow for sixth segment
      ];

      // Create a layer group to hold all segment polylines
      const layerGroup = this.L.layerGroup();

      // Draw each segment with a different color
      route.segments.forEach((segment: any, index: number) => {
        const startIdx = segment.steps[0]?.way_points?.[0] || 0;
        const endIdx = segment.steps[segment.steps.length - 1]?.way_points?.[1] || allCoords.length - 1;
        
        const segmentCoords = allCoords.slice(startIdx, endIdx + 1);
        const color = segmentColors[index % segmentColors.length];
        
        const polyline = this.L.polyline(segmentCoords, {
          color: color,
          weight: 5,
          opacity: 0.8,
        });

        layerGroup.addLayer(polyline);
      });

      // Add the layer group to the map
      layerGroup.addTo(this.map);
      this.routeLayer = layerGroup;
    } else if (route.geometry?.coordinates) {
      // Fallback: draw single-color route if segments not available
      const coords = route.geometry.coordinates.map(
        ([lng, lat]: [number, number]) => this.L.latLng(lat, lng)
      );
      this.routeLayer = this.L.polyline(coords, {
        color: '#e91e8c',
        weight: 5,
        opacity: 0.8,
      }).addTo(this.map);
    }

    // Fit map to show all markers
    if (bounds.isValid()) {
      this.map.fitBounds(bounds.pad(0.25));
    }

    // Set ETA if available
    this.eta = route.eta || '';
  }

  invalidateSize(): void {
    setTimeout(() => this.map?.invalidateSize(), 100);
  }
}
