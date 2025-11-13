import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Product } from '../../models/store-admin-models/product-admin/product';
import { CreateProductDTO } from '../../models/store-admin-models/product-admin/CreateProductDTO';
import { UpdateProductDTO } from '../../models/store-admin-models/product-admin/UpdateProductDTO'; 
import { AuthService } from '../authentication-service/auth.service';
import { Store } from '../../models/store-admin-models/store-admin/Store';

@Injectable({
  providedIn: 'root'
})
export class ProductService {

  constructor(private http: HttpClient, private authService: AuthService) { }

  private apiUrl = 'http://localhost:9091/api/products';


  //Get products for a specific store using the id
  getStoreById(storeId: number): Observable<Product> {
      const headers = this.authService.getAuthHeaders();
      return this.http.get<Product>(`${this.apiUrl}/store/${storeId}`, { 
        headers,
        withCredentials: true 
        
      });
    }
  
 createProduct(storeId: number, productData: CreateProductDTO, logoFile: File | null): Observable<Product> {
     const formData = new FormData();

  // Remove imageUrl from the data being sent (backend will set it)
  const { imageUrl, ...productDataWithoutImage } = productData;

  // Send productData as a plain JSON string (matches @RequestParam("productData") in backend)
  formData.append('productData', JSON.stringify(productDataWithoutImage));

  // Append the file (matches @RequestPart("logoFile") in backend)
  if (logoFile) {
    formData.append('logoFile', logoFile, logoFile.name);
  }

  const headers = this.authService.getAuthHeaders();
  // CRITICAL: Remove Content-Type header to let browser set multipart boundary
  headers.delete('Content-Type');

  console.log('Sending product data:', productDataWithoutImage);
  console.log('Sending file:', logoFile);

  return this.http.post<Product>(`${this.apiUrl}/store/${storeId}`, formData, {
    headers,
    withCredentials: true
  });
  }

  //Create a product with image using multipart form data
  createProductWithImage(storeId: number,  productData: CreateProductDTO, logoFile: File | null): Observable<Product>{
     const formData = new FormData();

  // Remove imageUrl from the data being sent (backend will set it)
  const { imageUrl, ...productDataWithoutImage } = productData;

  // Send productData as a plain JSON string (matches @RequestParam("productData") in backend)
  formData.append('productData', JSON.stringify(productDataWithoutImage));

  // Append the file (matches @RequestPart("logoFile") in backend)
  if (logoFile) {
    formData.append('logoFile', logoFile, logoFile.name);
  }

  const headers = this.authService.getAuthHeaders();

  headers.delete('Content-Type');

    return this.http.post<Product>(`${this.apiUrl}/store/${storeId}`, formData, {
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
