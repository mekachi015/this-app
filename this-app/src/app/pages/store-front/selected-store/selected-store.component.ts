import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { StoreAdminServiceService } from '../../../services/store-admin-service/store-admin-service.service';
import { ActivatedRoute } from '@angular/router';
import { Product } from '../../../models/store-admin-models/product-admin/product';
import { ProductService } from '../../../services/product-service/product.service';
import { AuthService } from '../../../services/authentication-service/auth.service';
import { CartService } from '../../../services/cart-service/cart.service';
import { CartRequest } from '../../../models/cart-model/cart-request';
import { response } from 'express';



@Component({
  selector: 'app-selected-store',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './selected-store.component.html',
  styleUrl: './selected-store.component.scss',
})
export class SelectedStoreComponent {
  ngOnInit(): void {
    // Initialization logic can go here
    this.getStoreFromRoute();
    this.loadStoreData(this.storeId!);

    this.currentUserId = Number(this.authService.currentUserValue?.id || 0);
  }

  constructor( private route: ActivatedRoute,
    private storeService: StoreAdminServiceService,
    private productService: ProductService,
    private authService: AuthService,
    private cartService: CartService

  ) {
    
  }

  searchQuery: string = '';

  products: Product[] = [];
  public storeId: string | null = null;

  private currentUserId: number = 0;

  onSearch(): void {
    // Implement search functionality
    console.log('Searching for:', this.searchQuery);
  }

  addToCart(product: Product): void {
    if(!this.currentUserId || this.currentUserId === 0){
      console.error('User not logged in. Cannot add to cart.');
      return;
    }

    if(!product.productId){
      console.error('Invalid product. Cannot add to cart.');
      return;
    }

    //default qunantity to 1 for a standard "Add to cart" button
    const request: CartRequest = {
      productId: product.productId,
      quantity: 1
    };

    this.cartService.addToCart(this.currentUserId, request).subscribe({
      next: (response) => {
        console.log('Product added to cart successfully:', response);
        alert(`Added ${product.productName} to cart.`);
      },
      error: (err) => {
        console.error('Failed to add product to cart:', err);
        alert('Failed to add product to cart. Please try again.');
      }
    });

  }

  zoomProduct(product: Product): void {
    // Implement zoom functionality
    console.log('Zooming product:', product);
  }

  private getStoreFromRoute(): void { 
    // Accessing the parameter named 'storeId' from the route defined as /store/:storeId
        this.storeId = this.route.snapshot.paramMap.get('storeId');

        if (this.storeId) {
            console.log('Successfully retrieved Store ID from route:', this.storeId);
            // Convert string id to number and load store data
            this.loadStoreData(this.storeId);
        } else {
            console.error('Error: Store ID not found in route parameters.');
            // Handle case where ID is missing, perhaps redirect to a 404 or store-page
        }
  }

  private loadStoreData(storeId: string): void {
    const idNum = Number(storeId);
    if (isNaN(idNum)) {
      console.error('Invalid store ID, cannot load store:', storeId);
      return;
    }

    console.log('Loading store data for ID:', idNum);

    // this.storeService.getStoreById(idNum).subscribe({
    //   next: (store) => {
    //     console.log('Loaded store:', store);
    //     // TODO: assign store data to component properties as needed
    //   },
    //   error: (err) => {
    //     console.error('Failed to load store:', err);
    //   }
    // });

    this.productService.getAllStoreProductsPublic(idNum).subscribe({
  next: (products) => {
    console.log('Loaded products for store:', products);
    this.products = products;
    console.log('Products assigned to component:', this.products);
  },
  error: (err) => {
    console.error('Failed to load products for store:', err);
  }
});
  }

  

  
}
