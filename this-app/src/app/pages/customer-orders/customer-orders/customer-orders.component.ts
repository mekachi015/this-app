import { Component, OnInit } from '@angular/core';
import { OrderDTO } from '../../../models/order-model/OrderDTO';
import { OrderService } from '../../../services/order-service/order.service';
import { AuthService } from '../../../services/authentication-service/auth.service';
import { Router } from '@angular/router';
import { OrderResponse } from '../../../models/order-model/OrderResponse';
import { DatePipe } from '@angular/common';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-customer-orders',
  standalone: true,
  imports: [CommonModule, DatePipe], // Added CommonModule for *ngIf and *ngFor
  templateUrl: './customer-orders.component.html',
  styleUrl: './customer-orders.component.scss'
})
export class CustomerOrdersComponent implements OnInit{

  orders: OrderDTO[] = [];
  currentUserId: number = 0;
  orderCount: number = 0;
  isLoading: boolean = false;
  errorMessage: string = '';
  selectedOrder: OrderDTO | null = null;

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
          console.log('Orders Loaded:', this.orders);
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
  viewOrderDetails(order: OrderDTO): void {
    this.selectedOrder = order;
  }

  closeOrderDetails(): void {
    this.selectedOrder = null;
  }

}
