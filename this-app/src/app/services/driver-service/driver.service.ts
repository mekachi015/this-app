import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { catchError, Observable, throwError } from 'rxjs';
import { OrderDTO } from '../../models/order-model/OrderDTO';
import { AuthService } from '../authentication-service/auth.service';

@Injectable({
  providedIn: 'root'
})
export class DriverService {

 private apiUrl = 'http://localhost:9091/api';

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  private get userId(): number {
    const user = this.authService.currentUserValue;
    if (!user?.id) throw new Error('User not logged in');
    return Number(user.id);
  }

  private get baseUrl(): string {
    return `${this.apiUrl}/driver/${this.userId}/orders`;
  }

  /**
   * GET /api/driver/{userId}/orders/available
   * View all unclaimed orders available to pick up
   */
  getAvailableOrders(): Observable<OrderDTO[]> {
    const headers = this.authService.getAuthHeaders();
    return this.http.get<OrderDTO[]>(`${this.baseUrl}/available`, { headers }).pipe(
      catchError(err => throwError(() => err.error?.message || 'Failed to fetch available orders'))
    );
  }

  /**
   * GET /api/driver/{userId}/orders/my-orders
   * View all orders claimed by this driver
   */
  getMyOrders(): Observable<OrderDTO[]> {
    const headers = this.authService.getAuthHeaders();
    return this.http.get<OrderDTO[]>(`${this.baseUrl}/my-orders`, { headers }).pipe(
      catchError(err => throwError(() => err.error?.message || 'Failed to fetch your orders'))
    );
  }

  /**
   * GET /api/driver/{userId}/orders/{orderId}
   * Get a specific order assigned to this driver
   */
  getOrderById(orderId: number): Observable<OrderDTO> {
    const headers = this.authService.getAuthHeaders();
    return this.http.get<OrderDTO>(`${this.baseUrl}/${orderId}`, { headers }).pipe(
      catchError(err => throwError(() => err.error?.message || 'Failed to fetch order'))
    );
  }

  /**
   * POST /api/driver/{userId}/orders/{orderId}/claim
   * Claim an available order
   */
  claimOrder(orderId: number): Observable<OrderDTO> {
    const headers = this.authService.getAuthHeaders();
    return this.http.post<OrderDTO>(`${this.baseUrl}/${orderId}/claim`, {}, { headers }).pipe(
      catchError(err => throwError(() => err.error?.message || 'Failed to claim order'))
    );
  }

  /**
   * PATCH /api/driver/{userId}/orders/{orderId}/status
   * Update the status of a claimed order
   * Valid transitions: OUT_FOR_DELIVERY → DELIVERED | FAILED_DELIVERY
   */
  updateOrderStatus(orderId: number, status: 'DELIVERED' | 'FAILED_DELIVERY'): Observable<OrderDTO> {
    const headers = this.authService.getAuthHeaders();
    return this.http.patch<OrderDTO>(
      `${this.baseUrl}/${orderId}/status`,
      { status },
      { headers }
    ).pipe(
      catchError(err => throwError(() => err.error?.message || 'Failed to update order status'))
    );
  }
}
