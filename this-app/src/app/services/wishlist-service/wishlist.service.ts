import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { AuthService } from '../authentication-service/auth.service';
import { WishlistResponse } from '../../models/wishlist-models/wishlistResponse';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class WishlistService {

  private baseUrl = 'http://localhost:9091/api/wishlist';

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) { }

  private getAuthHeaders(){
    const token = this.authService.token;

    return new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
  }

  //add product to wishlist
  addProductToWishlist(userId: number, productId: number): Observable<WishlistResponse> {
    const url = `${this.baseUrl}/add/product`;

    const params =new HttpParams()
    .set('userId', userId.toString())
    .set('productId', productId.toString());

    return this.http.post<WishlistResponse>(url, null,{
      headers: this.getAuthHeaders(),
      params: params,
      withCredentials: true
    })
  }
  
  //Add a store to wishlist
  addStoreToWishlist(userId: number, storeId: number): Observable<WishlistResponse> {
    const url = `${this.baseUrl}/add/store`;
    const params = new HttpParams()
    .set('userId', userId.toString())
    .set('storeId', storeId.toString());

    return this.http.post<WishlistResponse>(url, null, {
      headers: this.getAuthHeaders(),
      params: params,
      withCredentials: true
    });
  }

  //Get wishlist for a specific user
  getUserWishlist(userId: number): Observable<WishlistResponse> {
   const url = `${this.baseUrl}/${userId}`;

    return this.http.get<WishlistResponse>(url, {
      headers: this.getAuthHeaders(),
      withCredentials: true
    });
  }

  //Remove item from wishlist
    removeFromWishlist(wishlistId: number, userId: number): Observable<WishlistResponse> {
    const url = `${this.baseUrl}/${wishlistId}`;
    const params = new HttpParams()
      .set('userId', userId.toString());

    return this.http.delete<WishlistResponse>(url, {
      headers: this.getAuthHeaders(),
      params: params,
      withCredentials: true
    });
  }

  // Get wishlist item count for a user
   getWishlistItemCount(userId: number): Observable<WishlistResponse> {
    const url = `${this.baseUrl}/count/${userId}`;

    return this.http.get<WishlistResponse>(url, {
      headers: this.getAuthHeaders(),
      withCredentials: true
    });
  }

  // Check if product is in wishlist
  isProductInWishlist(userId: number, productId: number): Observable<WishlistResponse> {
    const url = `${this.baseUrl}/check/product`;
    const params = new HttpParams()
      .set('userId', userId.toString())
      .set('productId', productId.toString());

    return this.http.get<WishlistResponse>(url, {
      headers: this.getAuthHeaders(),
      params: params,
      withCredentials: true
    });
  }


  // Check if store is in wishlist
  isStoreInWishlist(userId: number, storeId: number): Observable<WishlistResponse> {
    const url = `${this.baseUrl}/check/store`;
    const params = new HttpParams()
      .set('userId', userId.toString())
      .set('storeId', storeId.toString());

    return this.http.get<WishlistResponse>(url, {
      headers: this.getAuthHeaders(),
      params: params,
      withCredentials: true
    });
  }





}
