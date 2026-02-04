import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { Address } from '../../models/address-model/address';
import { AuthService } from '../authentication-service/auth.service';

@Injectable({
  providedIn: 'root'
})
export class AddressService {
 private baseUrl = 'http://localhost:9091/api/addresses';

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  getUserAddresses(): Observable<Address[]> {
    const userId = this.getUserId();
    if (!userId) {
      return throwError(() => new Error('User not logged in'));
    }
    
    const headers = this.authService.getAuthHeaders();
    return this.http.get<Address[]>(
      `${this.baseUrl}/user/${userId}`,
      { headers }
    );
  }

  createAddress(address: Address): Observable<Address> {
    const userId = this.getUserId();
    if (!userId) {
      return throwError(() => new Error('User not logged in'));
    }
    
    const headers = this.authService.getAuthHeaders();
    return this.http.post<Address>(
      `${this.baseUrl}/user/${userId}`,
      address,
      { headers }
    );
  }

  updateAddress(addressId: number, address: Partial<Address>): Observable<Address> {
    const userId = this.getUserId();
    if (!userId) {
      return throwError(() => new Error('User not logged in'));
    }
    
    const headers = this.authService.getAuthHeaders();
    return this.http.put<Address>(
      `${this.baseUrl}/user/${userId}/address/${addressId}`,
      address,
      { headers }
    );
  }

  deleteAddress(addressId: number): Observable<void> {
    const userId = this.getUserId();
    if (!userId) {
      return throwError(() => new Error('User not logged in'));
    }
    
    const headers = this.authService.getAuthHeaders();
    return this.http.delete<void>(
      `${this.baseUrl}/user/${userId}/address/${addressId}`,
      { headers }
    );
  }

  getAddressById(addressId: number): Observable<Address> {
    const userId = this.getUserId();
    if (!userId) {
      return throwError(() => new Error('User not logged in'));
    }
    
    const headers = this.authService.getAuthHeaders();
    return this.http.get<Address>(
      `${this.baseUrl}/user/${userId}/address/${addressId}`,
      { headers }
    );
  }

  setDefaultAddress(addressId: number): Observable<Address> {
    const userId = this.getUserId();
    if (!userId) {
      return throwError(() => new Error('User not logged in'));
    }
    
    const headers = this.authService.getAuthHeaders();
    return this.http.patch<Address>(
      `${this.baseUrl}/user/${userId}/address/${addressId}/set-default`,
      {},
      { headers }
    );
  }

  getDefaultAddress(): Observable<Address> {
    const userId = this.getUserId();
    if (!userId) {
      return throwError(() => new Error('User not logged in'));
    }
    
    const headers = this.authService.getAuthHeaders();
    return this.http.get<Address>(
      `${this.baseUrl}/user/${userId}/default`,
      { headers }
    );
  }

  private getUserId(): number | null {
    const user = this.authService.currentUserValue;
    return user ? Number(user.id) : null;
  }
}