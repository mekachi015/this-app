import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { StoreAdminServiceService } from '../../../services/store-admin-service/store-admin-service.service';
import { ActivatedRoute } from '@angular/router';
import { Product } from '../../../models/store-admin-models/product-admin/product';
import { ProductService } from '../../../services/product-service/product.service';
import { AuthService } from '../../../services/authentication-service/auth.service';
import { CartService } from '../../../services/cart-service/cart.service';
import { CartResponse } from '../../../models/cart-model/CartResponse'; 
import { Store } from '../../../models/store-admin-models/store-admin/Store'; // Import the base interface

// Create a more specific interface for this component
interface SelectedStore extends Store {
  storeId: number; // Override to make it required
  storeName: string; // Override to make it required
  storeLogo?: string;
  // All other properties remain optional as inherited from Store
}

@Component({
  selector: 'app-selected-store',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './selected-store.component.html',
  styleUrl: './selected-store.component.scss',
})
export class SelectedStoreComponent implements OnInit {
  searchQuery: string = '';

  // Use the more specific SelectedStore interface
  store: SelectedStore | null = null;
  products: Product[] = [];
  isLoading = false;
  public storeId: string | null = null;

  private currentUserId: number = 0;

  constructor(
    private route: ActivatedRoute,
    private storeService: StoreAdminServiceService,
    private productService: ProductService,
    private authService: AuthService,
    private cartService: CartService
  ) {}

  ngOnInit(): void {
    this.getStoreFromRoute();
    this.currentUserId = Number(this.authService.currentUserValue?.id || 0);
  }

  onSearch(): void {
    console.log('Searching for:', this.searchQuery);
  }

  addToCart(product: Product): void {
    if (!this.currentUserId || this.currentUserId === 0) {
      alert('Please log in to add items to your cart.');
      console.error('User not logged in. Cannot add to cart.');
      return;
    }

    if (!product.productId) {
      console.error('Invalid product. Cannot add to cart.');
      return;
    }

    this.cartService.addToCart(this.currentUserId, product.productId, 1).subscribe({
      next: (response: CartResponse) => {
        if (response.success) {
          console.log('Product added to cart successfully:', response);
          alert(`${product.productName} has been added to your cart!`);
        } else {
          console.error('Failed to add product:', response.message);
          alert(response.message || 'Failed to add product to cart.');
        }
      },
      error: (err) => {
        console.error('Failed to add product to cart:', err);
        alert('Failed to add product to cart. Please try again.');
      }
    });
  }

  zoomProduct(product: Product): void {
    console.log('Zooming product:', product);
  }

  private getStoreFromRoute(): void { 
    this.storeId = this.route.snapshot.paramMap.get('storeId');

    if (this.storeId) {
      console.log('Successfully retrieved Store ID from route:', this.storeId);
      this.loadStoreData(this.storeId);
      this.loadProducts(this.storeId);
    } else {
      console.error('Error: Store ID not found in route parameters.');
    }
  }

  private loadStoreData(storeId: string): void {
    const idNum = Number(storeId);
    if (isNaN(idNum)) {
      console.error('Invalid store ID, cannot load store:', storeId);
      return;
    }

    console.log('Loading store data for ID:', idNum);
    this.isLoading = true;

    this.storeService.getStoreForPublicView(idNum).subscribe({
      next: (apiStore) => {
        console.log('Loaded API store data:', apiStore);
        
        // Type assertion to convert Store to SelectedStore
        // This is safe because we know the API will return these required fields
        const selectedStore: SelectedStore = {
          ...apiStore,
          storeId: apiStore.storeId || idNum, // Use the API storeId or fallback to route ID
          storeName: apiStore.storeName || 'Unnamed Store' // Ensure we have a name
        };
        
        this.store = selectedStore;
        console.log('Store data assigned:', this.store);
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Failed to load store:', err);
        this.isLoading = false;
      }
    });
  }

  private loadProducts(storeId: string): void {
    const idNum = Number(storeId);
    if (isNaN(idNum)) return;

    this.productService.getAllStoreProductsPublic(idNum).subscribe({
      next: (products) => {
        console.log('Loaded products for store:', products);
        this.products = products;
      },
      error: (err) => {
        console.error('Failed to load products for store:', err);
      }
    });
  }
}