import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Event } from '@angular/router';
import { debounceTime, distinctUntilChanged, Subject } from 'rxjs';
import { ProductService } from '../../services/product-service/product.service';
import { StoreAdminServiceService } from '../../services/store-admin-service/store-admin-service.service';
import { Router } from '@angular/router';


@Component({
  selector: 'app-search-bar',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './search-bar.component.html',
  styleUrl: './search-bar.component.scss'
})
export class SearchBarComponent {
   @Input() searchType: 'products' | 'stores' = 'products';
  @Input() storeId?: number; // Optional: for searching products within a specific store
  @Input() storeName?: string; // Optional: display store name
  
  // These now correctly reference Angular's EventEmitter type
  @Output() searchResults = new EventEmitter<any[]>();
  @Output() searchError = new EventEmitter<string>();
  
  searchQuery: string = '';
  private searchSubject = new Subject<string>();
  isSearching = false;
  searchResultsList: any[] = []; // To store search results

  constructor(
    private productService: ProductService,
    private storeService: StoreAdminServiceService,
    private router: Router // Inject Angular Router
  ) {
    // Debounce search input
    this.searchSubject.pipe(
      debounceTime(300),
      distinctUntilChanged()
    ).subscribe(searchTerm => {
      this.performSearch(searchTerm);
    });
  }

  onSearchChange(): void {
    this.searchSubject.next(this.searchQuery);
    // If search bar is cleared, also clear the dropdown for stores
    if (!this.searchQuery && this.searchType === 'stores') {
      this.searchResultsList = [];
    }
  }

  private performSearch(searchTerm: string): void {
    if (!searchTerm || searchTerm.trim().length < 2) {
      // This emit call is now valid
      this.searchResults.emit([]);
      return;
    }

    this.isSearching = true;

    if (this.searchType === 'products') {
      this.searchProducts(searchTerm);
    } else {
      this.searchStores(searchTerm);
    }
  }

  private searchProducts(searchTerm: string): void {
    const searchObservable = this.storeId
      ? this.productService.searchProductsByStore(this.storeId, searchTerm)
      : this.productService.searchProducts(searchTerm);

    searchObservable.subscribe({
      next: (results) => {
        // This emit call is now valid
        this.searchResults.emit(results);
        this.isSearching = false;
      },
      error: (error) => {
        console.error('Product search error:', error);
        // This emit call is now valid
        this.searchError.emit('Failed to search products');
        this.isSearching = false;
      }
    });
  }

  private searchStores(searchTerm: string): void {
    this.storeService.searchStores(searchTerm).subscribe({
      next: (results) => {
        this.searchResultsList = results; // Store results in the array
        this.searchResults.emit(results);
        this.isSearching = false;
      },
      error: (error) => {
        console.error('Store search error:', error);
        this.searchError.emit('Failed to search stores');
        this.isSearching = false;
      }
    });
  }

  onStoreSelect(store: any): void {
    console.log('Selected store:', store);
    this.searchQuery = store.storeName; // Update search bar with the store name
    this.searchResults.emit([store]); // Emit the selected store
    this.searchResultsList = []; // Clear the dropdown

    // Navigate to the selected store's page
    this.router.navigate(['/store', store.storeId]);
  }

  clearSearch(): void {
    this.searchQuery = '';
    this.searchResults.emit([]);
    if (this.searchType === 'stores') {
      this.searchResultsList = [];
    }
  }

  get placeholderText(): string {
    return this.searchType === 'products' ? 'Search products' : 'Search stores';
  }

}
