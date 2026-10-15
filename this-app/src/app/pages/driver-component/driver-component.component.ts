import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { OrderDTO } from '../../models/order-model/OrderDTO';
import { AuthService } from '../../services/authentication-service/auth.service';
import { DriverService } from '../../services/driver-service/driver.service';
import { MapService } from '../../services/map-service/map.service';

const BACKEND_URL = 'http://localhost:9091';

@Component({
  selector: 'app-driver-component',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './driver-component.component.html',
  styleUrl: './driver-component.component.scss'
})
export class DriverComponentComponent implements OnInit{

  currentOrder: OrderDTO | null = null;
  availableOrders: OrderDTO[] = [];
  myOrders: OrderDTO[] = [];

  isLoading = false;
  errorMessage = '';
  successMessage = '';

  private watchId!: number;

  constructor(
    private driverService: DriverService,
    private authService: AuthService,
    public mapSerivce: MapService
  ) {}

  ngOnInit(): void {
    this.loadAvailableOrders();
    this.loadMyOrders();
  }

  async ngAfterViewInit(): Promise<void> {
    await this.mapSerivce.initMap('delivery-map');
      this.mapSerivce.invalidateSize(); // 👈 add this

    this.startTracking();
  }

  startTracking(): void{
    if(!navigator.geolocation){
      return;
    }

    this.watchId = navigator.geolocation.watchPosition(
      (pos) => {
        const { latitude, longitude } = pos.coords;
        // Only update the driver marker — route is drawn by loadRouteForCurrentOrder()
        this.mapSerivce.updateDriverLocation(latitude, longitude);
      },
      (err) => console.error('Geolocation error:', err),
      { enableHighAccuracy: true , maximumAge: 10000 }
    );
  }

  ngOnDestroy(): void {
    if(this.watchId){
      navigator.geolocation.clearWatch(this.watchId);
    }
  }

  // -------------------------------------------------------------------------
  // Load available orders (unclaimed)
  // -------------------------------------------------------------------------
  loadAvailableOrders(): void {
    this.isLoading = true;
    this.driverService.getAvailableOrders().subscribe({
      next: (orders) => {
        this.availableOrders = orders;
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = err;
        this.isLoading = false;
      }
    });
  }

  // -------------------------------------------------------------------------
  // Load orders already claimed by this driver
  // -------------------------------------------------------------------------
  loadMyOrders(): void {
    this.driverService.getMyOrders().subscribe({
      next: (orders) => {
        this.myOrders = orders;

        // Set the first OUT_FOR_DELIVERY order as the current active delivery
        const active = orders.find(o => o.orderStatus === 'OUT_FOR_DELIVERY') || null;
        this.currentOrder = active;
        if (active) this.loadRouteForCurrentOrder(active);
      },
      error: (err) => {
        this.errorMessage = err;
      }
    });
  }

  // -------------------------------------------------------------------------
  // Claim an available order
  // -------------------------------------------------------------------------
  claimOrder(order: OrderDTO): void {
    this.isLoading = true;
    this.clearMessages();

    this.driverService.claimOrder(order.orderId).subscribe({
      next: (claimedOrder) => {
        this.successMessage = `Order #${order.orderId} claimed successfully`;
        
        this.currentOrder = claimedOrder;
        this.loadRouteForCurrentOrder(claimedOrder);

        // Remove from available, reload all
        this.loadAvailableOrders();
        this.loadMyOrders();
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = err;
        this.isLoading = false;
      }
    });
  }

  // -------------------------------------------------------------------------
  // Mark current order as delivered
  // -------------------------------------------------------------------------
  markAsDelivered(): void {
    if (!this.currentOrder) return;
    this.clearMessages();

    this.driverService.updateOrderStatus(this.currentOrder.orderId, 'DELIVERED').subscribe({
      next: (order) => {
        const id = order.orderId ?? order.id;
        this.successMessage = `Order #${id} marked as delivered`;
        this.currentOrder = null;
        this.loadMyOrders(); // Refresh the list
      },
      error: (err) => {
        this.errorMessage = err;
      }
    });
  }

  // -------------------------------------------------------------------------
  // Mark current order as failed delivery
  // -------------------------------------------------------------------------
  markAsFailed(): void {
    if (!this.currentOrder) return;
    this.clearMessages();

    this.driverService.updateOrderStatus(this.currentOrder.orderId, 'FAILED').subscribe({
      next: (order) => {
        const id = order.orderId ?? order.id;
        this.successMessage = `Order #${id} marked as failed delivery`;
        this.currentOrder = null;
        this.loadMyOrders();
      },
      error: (err) => {
        this.errorMessage = err;
      }
    });
  }

  // -------------------------------------------------------------------------
  // Call customer (uses phone from delivery address area — adjust if needed)
  // -------------------------------------------------------------------------
  callCustomer(): void {
    if (!this.currentOrder?.driver?.phoneNumber) {
      this.errorMessage = 'No contact number available';
      return;
    }
    window.location.href = `tel:${this.currentOrder.driver.phoneNumber}`;
  }

  private clearMessages(): void {
    this.errorMessage = '';
    this.successMessage = '';
  }

  /**
   * Geocodes store address + delivery address via Photon in the browser,
   * then calls POST /api/map/route/by-coords on the backend (ORS routing),
   * and draws the resulting store → delivery route on the Leaflet map.
   */
  private loadRouteForCurrentOrder(order: OrderDTO): void {
    if (!order.deliveryAddress) return;

    const token = this.authService.token;
    if (!token) return;

    const { streetNumber, streetName, city } = order.deliveryAddress;
    const deliveryQuery = `${streetNumber} ${streetName ?? ''} ${city}`.trim();

    // Single-store order
    if (!order.storeAddress) return;
    const storeQuery = order.storeAddress;

    this.mapSerivce.getOrderRoute(storeQuery, deliveryQuery, BACKEND_URL, token)
      .subscribe({
        next: (route) => {
          this.mapSerivce.showOrderRoute(route);
        },
        error: (err) => {
          console.warn('Route load failed:', err);
        }
      });
  }

}
