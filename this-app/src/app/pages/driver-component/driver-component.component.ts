import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { OrderDTO } from '../../models/order-model/OrderDTO';
import { AuthService } from '../../services/authentication-service/auth.service';
import { DriverService } from '../../services/driver-service/driver.service';
import { MapService } from '../../services/map-service/map.service';
import { forkJoin } from 'rxjs';
import Swal from 'sweetalert2';

const BACKEND_URL = 'http://localhost:9091';

interface GroupedDriverOrder {
  checkoutSessionId: string | null;
  orderId: number; // Primary order ID for display
  storeNames: string;
  deliveryAddress: any;
  itemCount: number;
  totalAmount: number;
  orderStatus: string;
  orders: OrderDTO[]; // All orders in this group
}

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
  groupedAvailableOrders: GroupedDriverOrder[] = [];
  myOrders: OrderDTO[] = [];
  groupedMyOrders: GroupedDriverOrder[] = [];

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
        this.groupedAvailableOrders = this.groupOrders(orders);
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
        this.groupedMyOrders = this.groupOrders(orders);

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
  // Claim an available order (or all orders in multi-store group)
  // -------------------------------------------------------------------------
  claimOrder(groupedOrder: GroupedDriverOrder): void {
    this.isLoading = true;
    this.clearMessages();

    // Claim all orders in the group
    const claimRequests = groupedOrder.orders.map(order => 
      this.driverService.claimOrder(order.orderId)
    );

    forkJoin(claimRequests).subscribe({
      next: (claimedOrders) => {
        const firstOrder = claimedOrders[0];
        
        // Show SweetAlert popup based on whether it's multi-store
        if (groupedOrder.orders.length > 1) {
          Swal.fire({
            icon: 'success',
            title: 'Claim Successful!',
            text: `Multi-store order #${groupedOrder.orderId} claimed (${groupedOrder.orders.length} stores)`,
            timer: 3000,
            showConfirmButton: false
          });
        } else {
          Swal.fire({
            icon: 'success',
            title: 'Claim Successful!',
            text: `Order #${groupedOrder.orderId} claimed successfully`,
            timer: 3000,
            showConfirmButton: false
          });
        }
        
        this.currentOrder = firstOrder;
        this.loadRouteForCurrentOrder(firstOrder);

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
        Swal.fire({
          icon: 'success',
          title: 'Delivery Complete!',
          text: `Order #${id} marked as delivered`,
          timer: 3000,
          showConfirmButton: false
        });
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
   * Group orders by checkoutSessionId for multi-store display
   */
  private groupOrders(orders: OrderDTO[]): GroupedDriverOrder[] {
    const sessionMap = new Map<string, OrderDTO[]>();
    const standaloneOrders: OrderDTO[] = [];

    orders.forEach(order => {
      if (order.checkoutSessionId) {
        if (!sessionMap.has(order.checkoutSessionId)) {
          sessionMap.set(order.checkoutSessionId, []);
        }
        sessionMap.get(order.checkoutSessionId)!.push(order);
      } else {
        standaloneOrders.push(order);
      }
    });

    const grouped: GroupedDriverOrder[] = [];

    // Multi-store groups
    sessionMap.forEach((ordersInSession, sessionId) => {
      const itemCount = ordersInSession.reduce((sum, o) => sum + o.itemCount, 0);
      const totalAmount = ordersInSession.reduce((sum, o) => sum + o.totalAmount, 0);
      const storeNames = [...new Set(ordersInSession.map(o => o.storeName))].join(' + ');
      const firstOrder = ordersInSession[0];

      grouped.push({
        checkoutSessionId: sessionId,
        orderId: firstOrder.orderId,
        storeNames: storeNames,
        deliveryAddress: firstOrder.deliveryAddress,
        itemCount: itemCount,
        totalAmount: totalAmount,
        orderStatus: firstOrder.orderStatus,
        orders: ordersInSession
      });
    });

    // Standalone orders
    standaloneOrders.forEach(order => {
      grouped.push({
        checkoutSessionId: null,
        orderId: order.orderId,
        storeNames: order.storeName,
        deliveryAddress: order.deliveryAddress,
        itemCount: order.itemCount,
        totalAmount: order.totalAmount,
        orderStatus: order.orderStatus,
        orders: [order]
      });
    });

    return grouped;
  }

  /**
   * Geocodes store address + delivery address via Photon in the browser,
   * then calls POST /api/map/route/by-coords on the backend (ORS routing),
   * and draws the resulting store → delivery route on the Leaflet map.
   * For multi-store orders (with checkoutSessionId), routes to all stores before delivery.
   */
  private loadRouteForCurrentOrder(order: OrderDTO): void {
    if (!order.deliveryAddress) return;

    const token = this.authService.token;
    if (!token) return;

    const { streetNumber, streetName, city } = order.deliveryAddress;
    const deliveryQuery = `${streetNumber} ${streetName ?? ''} ${city}`.trim();

    // Check if this is a multi-store order (has checkoutSessionId)
    if (order.checkoutSessionId) {
      // Find all orders with the same checkoutSessionId
      const relatedOrders = this.myOrders.filter(
        o => o.checkoutSessionId === order.checkoutSessionId
      );

      // Collect all unique store addresses
      const storeAddresses = relatedOrders
        .map(o => o.storeAddress)
        .filter((addr): addr is string => !!addr);

      if (storeAddresses.length > 0) {
        this.mapSerivce.getMultiStopRoute(storeAddresses, deliveryQuery, BACKEND_URL, token)
          .subscribe({
            next: (route) => {
              this.mapSerivce.showMultiStopRoute(route);
            },
            error: (err) => {
              console.warn('Multi-stop route load failed:', err);
            }
          });
        return;
      }
    }

    // Single-store order (no checkoutSessionId or fallback)
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
