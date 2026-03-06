import { Injectable } from '@angular/core';
import { CheckoutInitiateResponse } from '../../models/checkout-models/checkoutInitiateResponse';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AuthService } from '../authentication-service/auth.service';

@Injectable({
  providedIn: 'root'
})
export class CheckoutService {

  private baseUrl = 'http://localhost:9091/api/checkout';

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) { }

  //initiate the checkout 
  initiateCheckout(userId: number, deliveryAddressId: number): Observable<CheckoutInitiateResponse>{
    const headers = this.authService.getAuthHeaders();
    let params = new HttpParams().set('userId', userId.toString());
    const body = deliveryAddressId ? { deliveryAddressId } : {};

    return this.http.post<CheckoutInitiateResponse>(
      `${this.baseUrl}/initiate`,
      body,
      { headers, params }
    ) .pipe(
      catchError(err => throwError(
        () => err.error?.message || 'Checkout initiation failed'
      ))
    );
  }

  //get shipping amount 
  getCheckoutConfig(): Observable<{ shippingFee: number; multiStoreShippingFee: number }> {
    const headers = this.authService.getAuthHeaders();
    return this.http.get<{ shippingFee: number; multiStoreShippingFee: number }>(
      `${this.baseUrl}/config`,
      { headers }
    );
  }

}
