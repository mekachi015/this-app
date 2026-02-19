import { Inject,  Injectable, PLATFORM_ID } from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import { isPlatformBrowser } from '@angular/common';


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
  private routeLayer: any;
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

  invalidateSize(): void {
    setTimeout(() => this.map?.invalidateSize(), 100);
  }
}
