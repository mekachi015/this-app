import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { AuthService } from '../authentication-service/auth.service';
import { OrderResponse } from '../../models/order-model/OrderResponse';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class OrderService {

  // Update the baseUrl to use HTTP instead of HTTPS for local development
  private baseUrl = 'http://localhost:9091/api/orders';

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) { }

  //Get auth headers
    private getAuthHeaders(): HttpHeaders {
    const token = this.authService.token;

    return new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
  }

  //Get all orders for a specific user
  getUserOrders(userId: number): Observable<OrderResponse> {
    const url = `${this.baseUrl}/user/${userId}`;

    return this.http.get<OrderResponse>(url,
      { headers: this.getAuthHeaders(),
        withCredentials: true 
      });
  }


  //get a specific order by id
  getOrderById(orderId: number, userId: number): Observable<OrderResponse> {
    const url = `${this.baseUrl}/${orderId}`;
    const params = new HttpParams().set('userId', userId.toString());

    return this.http.get<OrderResponse>(url,{
      headers: this.getAuthHeaders(),
      params: params,
      withCredentials: true
    });
  }


  //get order count for a user
  getOrderCount(userId: number): Observable<OrderResponse> {
    const url = `${this.baseUrl}/user/${userId}/count`;

    return this.http.get<OrderResponse>(url, 
      {
        headers: this.getAuthHeaders(),
        withCredentials: true
      });
  }

  
}
