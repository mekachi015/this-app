import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { CartRequest } from '../../models/cart-model/cart-request';
import { Observable } from 'rxjs';
import { Cart } from '../../models/cart-model/cart';
import { QuantityUpdateRequest } from '../../models/cart-model/quantity-update-request';
import { AuthService } from '../authentication-service/auth.service';

@Injectable({
  providedIn: 'root'
})
export class CartService {
private baseUrl = 'http://localhost:9091/api/cart';

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  /** Get Auth Headers */
  private getAuthHeaders(): HttpHeaders {
    const token = this.authService.token;

    return new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
  }

  

  /** Add item to cart */
  addToCart(userId: number, request: CartRequest): Observable<Cart> {
    const url = `${this.baseUrl}/${userId}/items`;

    return this.http.post<Cart>(url, request, {
      headers: this.getAuthHeaders(),
      withCredentials: true,
      responseType: 'text' as 'json'
    });
  }

  /** Get cart for a specific user */
  getCartByUser(userId: number): Observable<Cart> {
    const url = `${this.baseUrl}/${userId}`;

    return this.http.get<Cart>(url, {
      headers: this.getAuthHeaders(),
      withCredentials: true,
      responseType: 'text' as 'json'
    });
  }

  /** Update cart item quantity */
  updateQuantity(cartItemId: number, request: QuantityUpdateRequest): Observable<Cart> {
    const url = `${this.baseUrl}/items/${cartItemId}`;

    return this.http.put<Cart>(url, request, {
      headers: this.getAuthHeaders(),
      withCredentials: true
    });
  }

  /** Remove cart item */
  removeCartItem(userId: number, productId: number): Observable<void> {
    const url = `${this.baseUrl}/${userId}/items/${productId}`;

    return this.http.delete<void>(url, {
      headers: this.getAuthHeaders(),
      withCredentials: true
    });
  }
}
