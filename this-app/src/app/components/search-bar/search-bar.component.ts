import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Event } from '@angular/router';
import { debounceTime, distinctUntilChanged, Subject } from 'rxjs';
import { ProductService } from '../../services/product-service/product.service';
import { StoreAdminServiceService } from '../../services/store-admin-service/store-admin-service.service';


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

  constructor(
    private productService: ProductService,
    private storeService: StoreAdminServiceService
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
        // This emit call is now valid
        this.searchResults.emit(results);
        this.isSearching = false;
      },
      error: (error) => {
        console.error('Store search error:', error);
        // This emit call is now valid
        this.searchError.emit('Failed to search stores');
        this.isSearching = false;
      }
    });
  }

  clearSearch(): void {
    this.searchQuery = '';
    // This emit call is now valid
    this.searchResults.emit([]);
  }

}
