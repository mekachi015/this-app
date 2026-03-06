import { Component, OnInit } from '@angular/core';
import { OrderDTO } from '../../../models/order-model/OrderDTO';
import { OrderService } from '../../../services/order-service/order.service';
import { AuthService } from '../../../services/authentication-service/auth.service';
import { Router } from '@angular/router';
import { OrderResponse } from '../../../models/order-model/OrderResponse';
import { DatePipe } from '@angular/common';
import { CommonModule } from '@angular/common';
import { OrderItemDTO } from '../../../models/order-model/OrderItemDTO';

interface GroupedOrder {
  checkoutSessionId: string | null;
  orderId: number;
  storeNames: string;
  createdAt: string;
  totalAmount: number;
  shippingAmount: number;
  orderStatus: string;
  deliveryAddress: any;
  items: OrderItemDTO[];
  itemCount: number;
  orders: OrderDTO[]; // underlying orders for this group
}

@Component({
  selector: 'app-customer-orders',
  standalone: true,
  imports: [CommonModule, DatePipe], // Added CommonModule for *ngIf and *ngFor
  templateUrl: './customer-orders.component.html',
  styleUrl: './customer-orders.component.scss'
})
export class CustomerOrdersComponent implements OnInit{

  orders: OrderDTO[] = [];
  groupedOrders: GroupedOrder[] = [];
  currentUserId: number = 0;
  orderCount: number = 0;
  isLoading: boolean = false;
  errorMessage: string = '';
  selectedOrder: GroupedOrder | null = null;

  constructor(
    private orderService: OrderService,
    private authService: AuthService,
    public router: Router
  ){}

  ngOnInit(): void {
    this.currentUserId = Number(this.authService.currentUserValue?.id||0);

    if(this.currentUserId > 0){
      this.loadUserOrders();
      this.loadOrderCount();
    } else {
      this.errorMessage = 'User not authenticated.';
    }
  }

  //load all the orders for the user
  loadUserOrders(): void{
    this.isLoading = true;
    this.errorMessage = '';

    this.orderService.getUserOrders(this.currentUserId).subscribe({
      next: (response: OrderResponse) => {
        if (response.success && response.data){
          this.orders = Array.isArray(response.data) ? response.data : [response.data];
          this.groupOrders();
          console.log('Orders Loaded:', this.orders);
          console.log('Grouped Orders:', this.groupedOrders);
        } else {
          this.errorMessage = response.message || 'Failed to load orders.';
        } this.isLoading = false;
      }, 
       error: (err) => {
        this.errorMessage = 'An error occurred while fetching orders.';
        console.error('Error loading orders:', err);
        this.isLoading = false
       }
    });
  }

  // Group orders by checkoutSessionId for multi-store
  groupOrders(): void {
    const sessionMap = new Map<string, OrderDTO[]>();
    const standaloneOrders: OrderDTO[] = [];

    // Separate multi-store from standalone orders
    this.orders.forEach(order => {
      if (order.checkoutSessionId) {
        if (!sessionMap.has(order.checkoutSessionId)) {
          sessionMap.set(order.checkoutSessionId, []);
        }
        sessionMap.get(order.checkoutSessionId)!.push(order);
      } else {
        standaloneOrders.push(order);
      }
    });

    this.groupedOrders = [];

    // Create grouped entries for multi-store orders
    sessionMap.forEach((ordersInSession, sessionId) => {
      const allItems = ordersInSession.flatMap(o => o.items);
      const storeNames = [...new Set(ordersInSession.map(o => o.storeName))].join(' + ');
      const totalAmount = ordersInSession.reduce((sum, o) => sum + o.totalAmount, 0);
      const shippingAmount = ordersInSession.reduce((sum, o) => sum + o.shippingAmount, 0);
      const firstOrder = ordersInSession[0];

      this.groupedOrders.push({
        checkoutSessionId: sessionId,
        orderId: firstOrder.orderId, // Use first order ID for display
        storeNames: storeNames,
        createdAt: firstOrder.createdAt,
        totalAmount: totalAmount,
        shippingAmount: shippingAmount,
        orderStatus: firstOrder.orderStatus,
        deliveryAddress: firstOrder.deliveryAddress,
        items: allItems,
        itemCount: allItems.length,
        orders: ordersInSession
      });
    });

    // Add standalone orders as single-order groups
    standaloneOrders.forEach(order => {
      this.groupedOrders.push({
        checkoutSessionId: null,
        orderId: order.orderId,
        storeNames: order.storeName,
        createdAt: order.createdAt,
        totalAmount: order.totalAmount,
        shippingAmount: order.shippingAmount,
        orderStatus: order.orderStatus,
        deliveryAddress: order.deliveryAddress,
        items: order.items,
        itemCount: order.items.length,
        orders: [order]
      });
    });

    // Sort by date descending
    this.groupedOrders.sort((a, b) => 
      new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
    );
  }


  //load order count
  loadOrderCount(): void{
    this.orderService.getOrderCount(this.currentUserId).subscribe({
      next: (response: OrderResponse) => {
        if (response.success && response.count !== undefined){
          this.orderCount = response.count;
          console.log('Order Count:', this.orderCount);
        }
      }, 
      error: (err) => {
        console.error('Error loading order count:', err);
      }
    });
  }

  //view order details 
  viewOrderDetails(order: GroupedOrder): void {
    this.selectedOrder = order;
  }

  closeOrderDetails(): void {
    this.selectedOrder = null;
  }

}
