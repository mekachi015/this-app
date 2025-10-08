import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Product } from '../../models/store-admin-models/product-admin/product';
import { CreateProductDTO } from '../../models/store-admin-models/product-admin/CreateProductDTO';
import { UpdateProductDTO } from '../../models/store-admin-models/product-admin/UpdateProductDTO'; 
import { AuthService } from '../authentication-service/auth.service';

@Injectable({
  providedIn: 'root'
})
export class ProductService {

  constructor(private http: HttpClient, private authService: AuthService) { }

  private apiUrl = 'http://localhost:9091/api/products';

  createProduct(storeId: number, productData: CreateProductDTO): Observable<Product> {
    const headers = this.authService.getAuthHeaders();
    return this.http.post<Product>(`${this.apiUrl}/store/${storeId}`, productData, { 
      headers,
    withCredentials: true 
  });
  }

  getStoreProducts(storeId: number): Observable<Product[]> {
    const headers = this.authService.getAuthHeaders();
    return this.http.get<Product[]>(`${this.apiUrl}/store/${storeId}`, { 
      headers, 
      withCredentials: true
     });
  }

  updateProduct(productId: number, productData: UpdateProductDTO): Observable<Product> {
    const headers = this.authService.getAuthHeaders();
    return this.http.put<Product>(`${this.apiUrl}/${productId}`, productData, { 
      headers,
      withCredentials: true 
    });
  }

  deleteProduct(productId: number): Observable<void> {
    const headers = this.authService.getAuthHeaders();
    return this.http.delete<void>(`${this.apiUrl}/${productId}`, {
      headers,
      withCredentials: true
    });
  }

  uploadProductImage(productId: number, file: File): Observable<string> {
    const formData = new FormData();
    formData.append('file', file);
    
    const headers = this.authService.getAuthHeaders();
    headers.delete('Content-Type'); // Let browser set the content type for file upload
    
    return this.http.post(`${this.apiUrl}/${productId}/image`, formData, {
      headers,
      withCredentials: true,
      responseType: 'text'
    });
  }
}
