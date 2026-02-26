import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Store } from '../../models/store-admin-models/store-admin/Store';
import { StoreDTO } from '../../models/store-admin-models/store-admin/StoreDTO';
import { catchError, Observable, throwError } from 'rxjs';
import { AuthService } from '../authentication-service/auth.service';

@Injectable({
  providedIn: 'root',
})
export class StoreAdminServiceService {
  private apiUrl = 'http://localhost:9091/api/stores';
  private apiStoreProductsUrl = 'http://localhost:9091/api/products/store';
  private countApi = 'http://localhost:9091/api/products/redefine';

  constructor(
    private http: HttpClient,
    private authService: AuthService,
  ) {}

  createStore(storeDto: StoreDTO): Observable<Store> {
    const token = this.authService.token;
    if (!token) {
      return throwError(() => new Error('No authentication token found'));
    }

    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    });

    return this.http
      .post<Store>(this.apiUrl, storeDto, {
        headers,
        withCredentials: true,
      })
      .pipe(
        catchError((error) => {
          console.error('Store creation error:', error);
          return throwError(() => error);
        }),
      );
  }

  createStoreWithLogo(
    storeData: any,
    logoFile: File | null,
  ): Observable<Store> {
    const formData = new FormData();

    // Create the store DTO object
    const storeDTO: StoreDTO = {
      storeName: storeData.storeName,
      storeDescription: storeData.storeDescription,
      storeAddress: storeData.storeAddress,
      storeEmail: storeData.storeEmail,
      storePhoneNumber: storeData.storePhoneNumber,
      storeBusinessHours: storeData.storeBusinessHours,
      // Don't send ownerId - backend gets it from Authentication
    };

    console.log('Store DTO being sent:', storeDTO);

    // CRITICAL: Append storeData as a JSON Blob with correct content type
    formData.append(
      'storeData',
      new Blob([JSON.stringify(storeDTO)], {
        type: 'application/json',
      }),
    );

    // Append logo file if provided
    if (logoFile) {
      console.log('Appending logo file:', logoFile.name, logoFile.type);
      formData.append('logoFile', logoFile, logoFile.name);
    }

    // CRITICAL: Get auth headers but DON'T set Content-Type
    // Let browser set it automatically for multipart/form-data
    const token = this.authService.token;
    const headers = new HttpHeaders({
      Authorization: `Bearer ${token}`,
      // NO Content-Type header - browser will set it with boundary
    });

    return this.http
      .post<Store>(this.apiUrl, formData, {
        headers,
        withCredentials: true,
      })
      .pipe(
        catchError((error) => {
          console.error('Store creation error:', error);
          console.error('Error details:', error.error);
          return throwError(() => error);
        }),
      );
  }

  updateStore(id: number, storeDto: StoreDTO): Observable<Store> {
    const headers = this.authService.getAuthHeaders();
    return this.http.put<Store>(`${this.apiUrl}/${id}`, storeDto, {
      headers,
      withCredentials: true,
    });
  }

  deleteStore(id: number): Observable<void> {
    const headers = this.authService.getAuthHeaders();
    return this.http.delete<void>(`${this.apiUrl}/${id}`, {
      headers,
      withCredentials: true,
    });
  }

  getAllPublicStores(): Observable<Store[]> {
    return this.http.get<Store[]>(`${this.apiUrl}/public`).pipe(
      catchError((error) => {
        console.error('Error fetching public stores:', error);
        return throwError(() => error);
      }),
    );
  }

  getAllStores(): Observable<Store[]> {
    const headers = this.authService.getAuthHeaders();
    return this.http.get<Store[]>(this.apiUrl, {
      headers,
      withCredentials: true,
    });
  }

  //for authenicated users
  getStoreById(storeId: number): Observable<Store> {
    const headers = this.authService.getAuthHeaders();
    return this.http.get<Store>(`${this.apiStoreProductsUrl}/${storeId}`, {
      headers,
      withCredentials: true,
    });
  }
  getStoreForPublicView(storeId: number): Observable<Store> {
    // Use the correct endpoint from your controller
    return this.http.get<Store>(`${this.apiUrl}/public/${storeId}`);
  }

  uploadStoreLogo(id: number, file: File): Observable<string> {
    const formData = new FormData();
    formData.append('file', file);

    const headers = this.authService.getAuthHeaders();
    // Remove content-type for FormData
    headers.delete('Content-Type');

    return this.http.post(`${this.apiUrl}/${id}/logo`, formData, {
      headers,
      withCredentials: true,
      responseType: 'text',
    });
  }

  // Get all stores for logged-in user
  getUserStores(): Observable<Store[]> {
    const headers = this.getAuthenticatedHeaders();
    return this.http
      .get<Store[]>(`${this.apiUrl}/my-stores`, {
        headers,
        withCredentials: true,
      })
      .pipe(
        catchError((error) => {
          console.error('Error fetching user stores:', error);
          return throwError(() => error);
        }),
      );
  }

  getStoresByUserId(userId: number): Observable<Store[]> {
    const headers = this.getAuthenticatedHeaders();
    return this.http.get<Store[]>(`${this.apiUrl}/my-stores/${userId}`, {
      headers,
      withCredentials: true,
    });
  }

  //Helper method to convert component store model to storeDto
  toStoreDTO(store: any): StoreDTO {
    return {
      storeName: store.storeName,
      storeDescription: store.storeDescription,
      storeAddress: store.storeAddress,
      storeEmail: store.storeEmail,
      storePhoneNumber: store.storePhoneNumber,
      storeBusinessHours: store.storeBusinessHours,
      ownerId: store.ownerId,
    };
  }

  toComponentStore(backendStore: Store): any {
    return {
      id: backendStore.storeId!.toString(),
      name: backendStore.storeName,
      description: backendStore.storeDescription,
      address: backendStore.storeAddress,
      contactEmail: backendStore.storeEmail,
      contactPhone: backendStore.storePhoneNumber,
      businessHours: backendStore.storeBusinessHours,
      logo: backendStore.storeLogo,
      createdAt: new Date(backendStore.createdAt),
      ownerId: backendStore.ownerId, // This would come from storeOwner if needed
    };
  }

  getAuthenticatedHeaders(): HttpHeaders {
    const token = this.authService.token;
    if (!token) {
      throw new Error('No authentication token found');
    }

    return new HttpHeaders({
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    });
  }

  //Search stores
  searchStores(searchTerm: string): Observable<Store[]> {
    const url = `${this.apiUrl}/search?q=${encodeURIComponent(searchTerm)}`;
    return this.http.get<Store[]>(url).pipe(
      catchError((error) => {
        console.error('Error searching stores:', error);
        return throwError(() => error);
      }),
    );
  }

  //get all orders for a store
  getStoreOrders(ownerId: number): Observable<any[]> {
    const headers = this.authService.getAuthHeaders();
    return this.http
      .get<any[]>(`${this.apiUrl}/owner/${ownerId}/orders`, {
        headers,
        withCredentials: true,
      })
      .pipe(
        catchError((error) => {
          console.error('Error fetching store orders:', error);
          return throwError(() => error);
        }),
      );
  }

  //get specific order by id for a store
  getStoreOrderById(storeId: number, orderId: number): Observable<any> {
    const headers = this.authService.getAuthHeaders();
    return this.http
      .get<any>(`${this.apiUrl}/${storeId}/orders/${orderId}`, {
        headers,
        withCredentials: true,
      })
      .pipe(
        catchError((error) => {
          console.error('Error fetching store order by id:', error);
          return throwError(() => error);
        }),
      );
  }

  getStoreOrderCount(storeId: number): Observable<{ count: number }> {
    const headers = this.authService.getAuthHeaders();
    return this.http
      .get<{ count: number }>(`${this.apiUrl}/${storeId}/orders/count`, {
        headers,
        withCredentials: true,
      })
      .pipe(
        catchError((error) => {
          console.error('Error fetching store order count:', error);
          return throwError(() => error);
        }),
      );
  }

  getStoreCountByOwner(
    userId: number,
  ): Observable<{ userId: number; totalStores: number }> {
    const headers = this.getAuthenticatedHeaders();
    return this.http
      .get<{
        userId: number;
        totalStores: number;
      }>(`${this.countApi}/users/${userId}/stores/count`, { headers, withCredentials: true })
      .pipe(
        catchError((error) => {
          console.error('Error fetching store count:', error);
          return throwError(() => error);
        }),
      );
  }

  getOrderCountByUserId(userId: number): Observable<{ userId: number; totalOrders: number }> {
   const headers = this.getAuthenticatedHeaders();
  return this.http.get<{ userId: number; totalOrders: number }>(
    `http://localhost:9091/api/orders/owner/count/${userId}`,  // was /owner/${userId}/count
    { headers, withCredentials: true }
  ).pipe(
    catchError((error) => {
      console.error('Error fetching order count by user:', error);
      return throwError(() => error);
    })
  );
}
}
