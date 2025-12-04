import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { CartResponse } from '../../models/cart-model/CartResponse';
import { Observable } from 'rxjs';
import { CartDTO } from '../../models/cart-model/CartDTO';
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
   addToCart(userId: number, productId: number, quantity: number = 1): Observable<CartResponse> {
    const url = `${this.baseUrl}/add`;
    const params = new HttpParams()
      .set('userId', userId.toString())
      .set('productId', productId.toString())
      .set('quantity', quantity.toString());

    return this.http.post<CartResponse>(url, null, {
      headers: this.getAuthHeaders(),
      params: params,
      withCredentials: true
    });
  }

  /** Get cart for a specific user */
  getCartByUser(userId: number): Observable<CartResponse> {
    const url = `${this.baseUrl}/${userId}`;

    return this.http.get<CartResponse>(url, {
      headers: this.getAuthHeaders(),
      withCredentials: true
    });
  }

  /** Update cart item quantity */
 updateQuantity(cartItemId: number, userId: number, quantity: number): Observable<CartResponse> {
    const url = `${this.baseUrl}/${cartItemId}`;
    const params = new HttpParams()
      .set('userId', userId.toString())
      .set('quantity', quantity.toString());

    return this.http.put<CartResponse>(url, null, {
      headers: this.getAuthHeaders(),
      params: params,
      withCredentials: true
    });
  }

  /** Remove cart item */
  removeCartItem(cartItemId: number, userId: number): Observable<CartResponse> {
    const url = `${this.baseUrl}/${cartItemId}`;
    const params = new HttpParams()
      .set('userId', userId.toString());

    return this.http.delete<CartResponse>(url, {
      headers: this.getAuthHeaders(),
      params: params,
      withCredentials: true
    });
  }

   /** Clear entire cart */
  clearCart(userId: number): Observable<CartResponse> {
    const url = `${this.baseUrl}/clear/${userId}`;

    return this.http.delete<CartResponse>(url, {
      headers: this.getAuthHeaders(),
      withCredentials: true
    });
  }

  /** Get cart item count */
  getCartItemCount(userId: number): Observable<CartResponse> {
    const url = `${this.baseUrl}/count/${userId}`;

    return this.http.get<CartResponse>(url, {
      headers: this.getAuthHeaders(),
      withCredentials: true
    });
  }
}
