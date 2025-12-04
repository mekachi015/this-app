import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
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
private baseUrl = 'http://localhost:9091/api/products/redefine';

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  /**
   * Create a new product for a specific store
   * POST: /api/products/redefine/users/{userId}/stores/{storeId}
   */
  createProduct(
 storeId: number,
  productData: CreateProductDTO,
  logoFile: File | null
): Observable<Product> {
  const formData = new FormData();
  
  formData.append('storeId', storeId.toString());
  formData.append('productName', productData.productName);
  formData.append('productDescription', productData.productDescription);
  formData.append('productPrice', productData.productPrice.toString());
  formData.append('stockQuantity', productData.stockQuantity.toString());
  formData.append('category', productData.category);
  
  if (logoFile) {
    formData.append('logoFile', logoFile);
  }

  // Get ONLY the auth token - NO Content-Type!
  const token = this.authService.token;
  
  const headers = new HttpHeaders({
    'Authorization': `Bearer ${token}`
    // ⚠️ DO NOT set Content-Type here!
  });

  const url = `${this.baseUrl}/post/products`;

  console.log('🔑 Token exists:', !!token);
  console.log('📍 URL:', url);

  return this.http.post<Product>(url, formData, {
    headers,
    withCredentials: true
  });
}

  /**
   * Update an existing product
   * PUT: /api/products/redefine/users/{userId}/stores/{storeId}/{productId}
   */
  updateProduct(
    productId: number,
    productData: CreateProductDTO,
    logoFile: File | null
  ): Observable<Product> {
    const formData = new FormData();

     

    // Append each field individually
    formData.append('productName', productData.productName);
    formData.append('productDescription', productData.productDescription);
    formData.append('productPrice', productData.productPrice.toString());
    formData.append('stockQuantity', productData.stockQuantity.toString());
    formData.append('category', productData.category);

    // Append the logo file if provided
    if (logoFile) {
      formData.append('logoFile', logoFile, logoFile.name);
    }

    const token = this.authService.token;

    const headers = new HttpHeaders({
      'Authorization' : `Bearer ${token}`
    });

    const url = `${this.baseUrl}/${productId}`;

    console.log('Updating product:', {
      url,
      productId,
      productData,
      hasFile: !!logoFile
    });

    return this.http.put<Product>(url, formData, {
      headers,
      withCredentials: true
    });
  }

  /**
   * Get all products for a store
   * Note: You'll need to add this endpoint to your backend if it doesn't exist
   */
  getStoreProducts(storeId: number): Observable<Product[]> {
   const token = this.authService.token;

   const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
   })

   const url = `${this.baseUrl}/stores/${storeId}/products`;

   return this.http.get<Product[]>(url, {
      headers,
      withCredentials: true
   });
  }

   /**
   * Get all products for a store
   * Note: You'll need to add this endpoint to your backend if it doesn't exist
   */
  getAllStoreProductsPublic(storeId: number): Observable<Product[]> {
  const url = `${this.baseUrl}/stores/${storeId}/products/public`;

  return this.http.get<Product[]>(url); 
}
  
  deleteProduct(storeId: number, productId: number){
    return (console.log('Deleting product not implemented yet'));
  }
}
