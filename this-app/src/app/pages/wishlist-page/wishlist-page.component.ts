import { Component } from '@angular/core';
import { StoreCardComponent } from '../../components/store-front/store-card/store-card.component';
import { SearchBarComponent } from '../../components/search-bar/search-bar.component';
import { CommonModule } from '@angular/common';
import { WishlistService } from '../../services/wishlist-service/wishlist.service';
import { AuthService } from '../../services/authentication-service/auth.service';
import { WishlistResponse } from '../../models/wishlist-models/wishlistResponse';

import { StoreWishlistItem } from '../../models/wishlist-models/store-wishlist-item';
import { ProductWishlistItem } from '../../models/wishlist-models/product-wishlist-item';


interface Store {
  id: string;
  name: string;
  imageUrl: string;
  bestseller: string;
  category: string;
  rating: number;
  operatingHours: string;
  description: string;
}

@Component({
  selector: 'app-wishlist-page',
  standalone: true,
  imports: [CommonModule, StoreCardComponent, SearchBarComponent],
  templateUrl: './wishlist-page.component.html',
  styleUrl: './wishlist-page.component.scss'
})
export class WishlistPageComponent {
  favoriteStores: StoreWishlistItem[] = [];
  favoriteClothes: ProductWishlistItem[] = [];
  currentUserId: number = 0;
  isLoading: boolean = false;
  errorMessage: string = '';

  constructor(
    private wishlistService: WishlistService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.currentUserId = Number(this.authService.currentUserValue?.id || 0);
    
    if (this.currentUserId > 0) {
      this.loadUserWishlist();
    } else {
      this.errorMessage = 'Please log in to view your wishlist';
    }
  }

  // Add this helper method to convert StoreWishlistItem to Store
  toStore(item: StoreWishlistItem): Store {
    return {
      id: item.id,
      name: item.name || item.storeName || 'Unknown Store',
      imageUrl: item.imageUrl || '',
      bestseller: item.bestseller || '',
      category: item.category || '',
      rating: item.rating || 0,
      operatingHours: item.operatingHours || '',
      description: item.description || ''
    };
  }

  loadUserWishlist(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.wishlistService.getUserWishlist(this.currentUserId).subscribe({
      next: (response: WishlistResponse) => {
        if (response.success && response.data) {
          const wishlistItems = Array.isArray(response.data) ? response.data : [response.data];
          
          // Map to StoreWishlistItem with all required fields
          this.favoriteStores = wishlistItems
            .filter(item => item.storeId && !item.productId)
            .map(item => ({
              wishlistId: item.wishlistId,
              userId: item.userId,
              storeId: item.storeId,
              storeName: item.storeName,
              createdAt: item.createdAt,
              updatedAt: item.updatedAt,
              id: String(item.storeId),
              name: item.storeName || 'Unknown Store',
              imageUrl: item.storeImage || '',
              bestseller: '',
              category: '',
              rating: 0,
              operatingHours: '',
              description: ''
            }));

          this.favoriteClothes = wishlistItems
            .filter(item => item.productId)
            .map(item => ({
              wishlistId: item.wishlistId,
              userId: item.userId,
              productId: item.productId,
              productName: item.productName,
              productImage: item.productImage,
              productPrice: item.productPrice,
              storeName: item.storeName,
              createdAt: item.createdAt,
              updatedAt: item.updatedAt,
              name: item.productName || 'Unknown Product',
              price: item.productPrice || 0,
              imageUrl: item.productImage || '',
              description: ''
            }));

          console.log('Wishlist loaded successfully');
          console.log('Stores:', this.favoriteStores.length);
          console.log('Products:', this.favoriteClothes.length);
        } else {
          this.errorMessage = response.message || 'Failed to load wishlist';
        }
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Failed to load wishlist:', err);
        this.errorMessage = 'Failed to load wishlist. Please try again.';
        this.isLoading = false;
      }
    });
  }

  // ... rest of your methods remain the same
  removeStoreFromWishlist(store: StoreWishlistItem): void {
    if (!confirm(`Remove ${store.storeName} from your wishlist?`)) {
      return;
    }

    this.wishlistService.removeFromWishlist(store.wishlistId, this.currentUserId).subscribe({
      next: (response: WishlistResponse) => {
        if (response.success) {
          this.favoriteStores = this.favoriteStores.filter(s => s.wishlistId !== store.wishlistId);
          console.log('Store removed from wishlist');
        }
      },
      error: (err) => {
        console.error('Failed to remove store:', err);
        alert('Failed to remove store. Please try again.');
      }
    });
  }

  removeProductFromWishlist(product: ProductWishlistItem): void {
    if (!confirm(`Remove ${product.productName} from your wishlist?`)) {
      return;
    }

    this.wishlistService.removeFromWishlist(product.wishlistId, this.currentUserId).subscribe({
      next: (response: WishlistResponse) => {
        if (response.success) {
          this.favoriteClothes = this.favoriteClothes.filter(p => p.wishlistId !== product.wishlistId);
          console.log('Product removed from wishlist');
        }
      },
      error: (err) => {
        console.error('Failed to remove product:', err);
        alert('Failed to remove product. Please try again.');
      }
    });
  }

  clearWishlist(): void {
    if (!confirm('Are you sure you want to clear your entire wishlist?')) {
      return;
    }
    // Implementation commented out as in original
  }
}