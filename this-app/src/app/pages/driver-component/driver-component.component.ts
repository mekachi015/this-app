import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Delivery } from '../../models/delivery-models/delivery/delivery';
import { DeliveryItem } from '../../models/delivery-models/delivery-item/DeliveryItem';
import { OrderDTO } from '../../models/order-model/OrderDTO';
import { AuthService } from '../../services/authentication-service/auth.service';
import { DriverService } from '../../services/driver-service/driver.service';

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

  constructor(
    private driverService: DriverService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadAvailableOrders();
    this.loadMyOrders();
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
        this.currentOrder = orders.find(o => o.orderStatus === 'OUT_FOR_DELIVERY') || null;
      },
      error: (err) => {
        this.errorMessage = err;
      }
    });
  }

  // -------------------------------------------------------------------------
  // Claim an available order
  // -------------------------------------------------------------------------
  claimOrder(orderId: number): void {
    this.isLoading = true;
    this.clearMessages();

    this.driverService.claimOrder(orderId).subscribe({
      next: (order) => {
        this.successMessage = `Order #${order.orderId} claimed successfully`;
        this.currentOrder = order;

        // Remove from available, add to my orders
        this.availableOrders = this.availableOrders.filter(o => o.orderId !== orderId);
        this.myOrders.push(order);
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
        this.successMessage = `Order #${order.orderId} marked as delivered`;
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

    this.driverService.updateOrderStatus(this.currentOrder.orderId, 'FAILED_DELIVERY').subscribe({
      next: (order) => {
        this.successMessage = `Order #${order.orderId} marked as failed delivery`;
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

}
