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
import Swal from 'sweetalert2';
import { Store } from '../../../models/store-admin-models/store-admin/Store'; // Import the base interface
import { WishlistService } from '../../../services/wishlist-service/wishlist.service';

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

  // To store search results
  searchResultsList: Product[] = [];
  wishlisted: Set<number> = new Set();

  constructor(
    private route: ActivatedRoute,
    private storeService: StoreAdminServiceService,
    private productService: ProductService,
    private authService: AuthService,
    private cartService: CartService,
    private wishlistService: WishlistService
  ) {}

  ngOnInit(): void {
    this.getStoreFromRoute();
    this.currentUserId = Number(this.authService.currentUserValue?.id || 0);
    this.loadWishlistState();
  }

  loadWishlistState(): void {
      if (!this.currentUserId) return;

    this.wishlistService.getUserWishlist(this.currentUserId).subscribe({
      next: (response) => {
        if (response.success && response.data) {
          const items = Array.isArray(response.data) ? response.data : [response.data];
          items.forEach((item: any) => {
            if (item.productId) this.wishlisted.add(item.productId);
          });
        }
      },
      error: () => {} // silently fail — not critical
    });
  }

  isWishlisted(productId: number): boolean {
    return this.wishlisted.has(productId);
  }

  onSearch(): void {
    if (!this.searchQuery || this.searchQuery.trim().length < 2) {
      console.warn('Search query is too short.');
      return;
    }

    if (!this.storeId) {
      console.error('Store ID is not available. Cannot perform search.');
      return;
    }

    const storeId = Number(this.storeId);
    if (isNaN(storeId)) {
      console.error('Invalid store ID. Cannot perform search.');
      return;
    }

    this.isLoading = true;
    this.productService.searchProductsByStore(storeId, this.searchQuery).subscribe({
      next: (products) => {
        console.log('Search results:', products);
        this.products = products; // Update the products list with search results
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error searching products:', err);
        this.isLoading = false;
      },
    });
  }

  onSearchChange(): void {
    if (!this.searchQuery || this.searchQuery.trim().length < 2) {
      this.searchResultsList = []; // Clear results if query is too short
      return;
    }

    if (!this.storeId) {
      console.error('Store ID is not available. Cannot perform search.');
      return;
    }

    const storeId = Number(this.storeId);
    if (isNaN(storeId)) {
      console.error('Invalid store ID. Cannot perform search.');
      return;
    }

    this.productService.searchProductsByStore(storeId, this.searchQuery).subscribe({
      next: (products) => {
        console.log('Search results:', products);
        this.searchResultsList = products; // Update the dropdown with search results
      },
      error: (err) => {
        console.error('Error searching products:', err);
        this.searchResultsList = []; // Clear results on error
      },
    });
  }

  addToCart(product: Product): void {
    if (!this.currentUserId || this.currentUserId === 0) {
      Swal.fire({
        icon: 'info',
        title: 'Login Required',
        text: 'Please log in to add items to your cart.'
      }).then(() => {
        this.authService.redirectToLogin();
      });
      return;
    }

    if (!product.productId) {
      console.error('Invalid product. Cannot add to cart.');
      return;
    }

    // Single-store enforcement: check what store is already in the cart
    this.cartService.getCartByUser(this.currentUserId).subscribe({
      next: (cartResponse: CartResponse) => {
        const existingItems = Array.isArray(cartResponse.data)
          ? cartResponse.data
          : cartResponse.data ? [cartResponse.data] : [];

        if (existingItems.length > 0) {
          const cartStoreId = existingItems[0].storeId;
          const thisStoreId = Number(this.storeId);

          if (cartStoreId !== thisStoreId) {
            const cartStoreName = existingItems[0].storeName;
            Swal.fire({
              icon: 'warning',
              title: 'Different Store',
              html: `Your cart already has items from <strong>${cartStoreName}</strong>.<br>
                     You can only order from <strong>one store at a time</strong>.<br>
                     Clear your cart first, then add from this store.`,
              showCancelButton: true,
              confirmButtonColor: '#e91e8c',
              cancelButtonColor: '#6c757d',
              confirmButtonText: 'Clear Cart & Add',
              cancelButtonText: 'Keep Current Cart',
            }).then(result => {
              if (result.isConfirmed) {
                this.cartService.clearCart(this.currentUserId).subscribe({
                  next: () => this.doAddToCart(product),
                  error: () => Swal.fire({ icon: 'error', title: 'Failed to clear cart', confirmButtonColor: '#e91e8c' })
                });
              }
            });
            return;
          }
        }

        this.doAddToCart(product);
      },
      error: () => {
        // If we can't fetch cart, still try to add
        this.doAddToCart(product);
      }
    });
  }

  private doAddToCart(product: Product): void {
    if (!product.productId) {
      console.error('Invalid product. Cannot add to cart.');
      return;
    }

    this.cartService.addToCart(this.currentUserId, product.productId, 1).subscribe({
      next: (response: CartResponse) => {
        if (response.success) {
          Swal.fire({
            toast: true,
            position: 'bottom-end',
            icon: 'success',
            title: `${product.productName} added to cart!`,
            showConfirmButton: false,
            timer: 1800,
            timerProgressBar: true,
          });
        } else {
          Swal.fire({
            icon: 'error',
            title: 'Add to Cart Failed',
            text: response.message || 'Failed to add product to cart.',
            confirmButtonColor: '#e91e8c',
          });
        }
      },
      error: () => {
        Swal.fire({
          icon: 'error',
          title: 'Add to Cart Failed',
          text: 'Failed to add product to cart. Please try again.',
          confirmButtonColor: '#e91e8c',
        });
      }
    });
  }

   addToWishlist(product: any): void {
    if (!this.currentUserId) {
      Swal.fire({
        icon: 'warning',
        title: 'Not Logged In',
        text: 'Please log in to add items to your wishlist.',
        confirmButtonColor: '#e91e8c',
      });
      return;
    }

    // If already wishlisted — remove it
    if (this.isWishlisted(product.productId)) {
      this.wishlistService.getUserWishlist(this.currentUserId).subscribe({
        next: (response) => {
          if (response.success && response.data) {
            const items = Array.isArray(response.data) ? response.data : [response.data];
            const match = items.find((i: any) => i.productId === product.productId);
            if (match) {
              this.wishlistService.removeFromWishlist(match.wishlistId, this.currentUserId).subscribe({
                next: (res) => {
                  if (res.success) {
                    this.wishlisted.delete(product.productId);
                    Swal.fire({
                      toast: true,
                      position: 'bottom-end',
                      icon: 'info',
                      title: `"${product.productName}" removed from wishlist`,
                      showConfirmButton: false,
                      timer: 2000,
                      timerProgressBar: true,
                    });
                  }
                },
                error: () => {
                  Swal.fire({
                    icon: 'error',
                    title: 'Error',
                    text: 'Could not remove from wishlist. Please try again.',
                    confirmButtonColor: '#e91e8c',
                  });
                }
              });
            }
          }
        }
      });
      return;
    }

    // Otherwise — add it
    this.wishlistService.addProductToWishlist(this.currentUserId, product.productId).subscribe({
      next: (response) => {
        if (response.success) {
          this.wishlisted.add(product.productId);
          Swal.fire({
            toast: true,
            position: 'bottom-end',
            icon: 'success',
            title: `"${product.productName}" added to wishlist ❤️`,
            showConfirmButton: false,
            timer: 2000,
            timerProgressBar: true,
          });
        }
      },
      error: () => {
        Swal.fire({
          icon: 'error',
          title: 'Wishlist Error',
          text: 'Could not add to wishlist. Please try again.',
          confirmButtonColor: '#e91e8c',
        });
      }
    });
  }

  onProductSelect(product: Product): void {
    console.log('Selected product:', product);
    this.searchQuery = product.productName; // Update search bar with the product name
    this.searchResultsList = []; // Clear the dropdown
    this.products = [product]; // Show only the selected product in the main content
  }

  clearSearch(): void {
    this.searchQuery = ''; // Clear the search query
    this.searchResultsList = []; // Clear the dropdown
    this.loadProducts(this.storeId!); // Reload all products for the store
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