import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {Store} from "../../models/store-admin-models/store-admin/Store";
import {StoreDTO} from "../../models/store-admin-models/store-admin/StoreDTO";
import {catchError, Observable, throwError} from "rxjs";
import { AuthService } from "../authentication-service/auth.service";

@Injectable({
  providedIn: 'root'
})
export class StoreAdminServiceService {

  private apiUrl = 'http://localhost:9091/api/stores';
  private apiStoreProductsUrl = 'http://localhost:9091/api/products/store';

  constructor(private http: HttpClient, private authService: AuthService) { }

  createStore(storeDto: StoreDTO): Observable<Store> {
    const token = this.authService.token;
    if (!token) {
      return throwError(() => new Error('No authentication token found'));
    }

    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    });

    return this.http.post<Store>(this.apiUrl, storeDto, { 
      headers,
      withCredentials: true 
    }).pipe(
      catchError(error => {
        console.error('Store creation error:', error);
        return throwError(() => error);
      })
    );
  }

  createStoreWithLogo(storeDto: StoreDTO, logoFile: File): Observable<Store>{
    const formData = new FormData();

    formData.append('storeData', new Blob([JSON.stringify(storeDto)],
     {type : 'application/json'}));

     if (logoFile){
      formData.append('logo', logoFile);
     }
     
     const token = this.authService.token;
     const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
     });

    return this.http.post<Store>(this.apiUrl, formData, { 
    headers,
    withCredentials: true 
  }).pipe(
    catchError(error => {
      console.error('Store creation error:', error);
      return throwError(() => error);
    })
  );
  }

   updateStore(id: number, storeDto: StoreDTO): Observable<Store> {
    const headers = this.authService.getAuthHeaders();
    return this.http.put<Store>(`${this.apiUrl}/${id}`, storeDto, { 
      headers,
      withCredentials: true 
    });
  }

  deleteStore(id: number): Observable<void> {
    const headers = this.authService.getAuthHeaders();
    return this.http.delete<void>(`${this.apiUrl}/${id}`, { 
      headers,
      withCredentials: true 
    });
  }

  getAllStores(): Observable<Store[]> {
    const headers = this.authService.getAuthHeaders();
    return this.http.get<Store[]>(this.apiUrl, { 
      headers,
      withCredentials: true 
    });
  }

  // getStoreById(storeId: number): Observable<Store> {
  //   const headers = this.authService.getAuthHeaders();
  //   return this.http.get<Store>(`${this.apiUrl}/${storeId}`, { 
  //     headers,
  //     withCredentials: true 
  //   });
  // }

  getStoreById(storeId: number): Observable<Store> {
    const headers = this.authService.getAuthHeaders();
    return this.http.get<Store>(`${this.apiStoreProductsUrl}/${storeId}`, { 
      headers,
      withCredentials: true 
    });
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
      responseType: 'text'
    });
  }

   // Get all stores for logged-in user
  getUserStores(): Observable<Store[]> {
    const headers = this.getAuthenticatedHeaders();
  return this.http.get<Store[]>(`${this.apiUrl}/my-stores`, {
    headers,
    withCredentials: true
  }).pipe(
    catchError(error => {
      console.error('Error fetching user stores:', error);
      return throwError(() => error);
    })
  );
  }

  getStoresByUserId(userId: number): Observable<Store[]> {
  const headers = this.getAuthenticatedHeaders();
  return this.http.get<Store[]>(`${this.apiUrl}/my-stores/${userId}`, {
    headers,
    withCredentials: true
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
    ownwerId: store.ownerId
  };
  }

  toComponentStore(backendStore: Store): any{
    return{
      id: backendStore.storeId!.toString(),
      name: backendStore.storeName,
      description: backendStore.storeDescription,
      address: backendStore.storeAddress,
      contactEmail: backendStore.storeEmail,
      contactPhone: backendStore.storePhoneNumber,
      businessHours: backendStore.storeBusinessHours,
      logo: backendStore.storeLogo,
      createdAt: new Date(backendStore.createdAt),
      ownerId: backendStore.ownerId // This would come from storeOwner if needed
    };
  }

   getAuthenticatedHeaders(): HttpHeaders {
    const token = this.authService.token;
    if (!token) {
      throw new Error('No authentication token found');
    }
    
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    });
  }
}
