import { Component, OnInit } from '@angular/core';
import { Store } from '../../../models/store-front/store.model';
import { StoreCardComponent } from "../../../components/store-front/store-card/store-card.component";
import { NearYouComponent } from '../../../components/store-front/near-you/near-you.component';
import { NavBarComponent } from "../../../components/nav-bar/nav-bar/nav-bar.component";
import { CommonModule } from '@angular/common';
import { SearchBarComponent } from '../../../components/search-bar/search-bar.component';
import { StoreAdminServiceService } from '../../../services/store-admin-service/store-admin-service.service';
import { Router } from '@angular/router'; //

@Component({
  selector: 'app-store-page',
  standalone: true,
  imports: [StoreCardComponent, NearYouComponent,CommonModule, SearchBarComponent],
  templateUrl: './store-page.component.html',
  styleUrl: './store-page.component.scss'
})
export class StorePageComponent implements OnInit{

  featuredStores: Store[] = [];
  nearbyStores: Store[] = [];
  isLoading = false;
  errorMessage = '';

  constructor(private storeService: StoreAdminServiceService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadAllStores();
  }

  loadAllStores(): void {
    this.isLoading = true;
    
    // Call the public endpoint to get all stores
    this.storeService.getAllPublicStores().subscribe({
      next: (stores) => {
        console.log('Loaded stores from backend:', stores);
        
        // Transform backend Store[] to your frontend Store model
        const transformedStores = stores.map(backendStore => this.transformToFrontendStore(backendStore));
        
        // Split stores: First 5 for featured, rest for nearby
        this.featuredStores = transformedStores.slice(0, 5);
        this.nearbyStores = transformedStores; // Show all stores in nearby section
        
        console.log('Transformed featured stores:', this.featuredStores);
          console.log('Transformed nearby stores:', this.nearbyStores);
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading stores:', error);
        this.errorMessage = 'Failed to load stores';
        this.isLoading = false;
        
        // Fallback to empty arrays if backend fails
        this.featuredStores = [];
        this.nearbyStores = [];
      }
    });
  }


   private transformToFrontendStore(backendStore: any): Store {
    return {
      id: backendStore.storeId?.toString() || '',
      name: backendStore.storeName || 'Unknown Store',
      imageUrl: backendStore.storeLogo || 'assets/default-store.png',
      description: backendStore.storeDescription || 'No description available',
      bestseller: 'Featured Items', // You can add this to backend later
      category: backendStore.storeCategory || this.inferCategory(backendStore.storeName || ''),
      rating: 90, // You can add ratings to backend later
      operatingHours: backendStore.storeBusinessHours || 'Hours not available'
    };
  }

  /**
   * Helper method to infer category from store name
   * You can make this more sophisticated or add category field to backend
   */
  private inferCategory(storeName: string): string {
    const name = storeName.toLowerCase();
    
    if (name.includes('vintage') || name.includes('retro')) {
      return 'Vintage Fashion';
    } else if (name.includes('luxury') || name.includes('premium')) {
      return 'Luxury Fashion';
    } else if (name.includes('street') || name.includes('urban')) {
      return 'Street Fashion';
    } else if (name.includes('japanese') || name.includes('japan')) {
      return 'Japanese Fashion';
    } else {
      return 'Fashion & Lifestyle';
    }
  }

  //FUnction to navigate to selected store page
  public navigateToStoreProducts(store: Store): void{
    console.log(`Navigating to store: ${store.name} (ID: ${store.id})`);
    console.log('Constructed URL:', `/store/${store.id}`);
    this.router.navigate(['/store', store.id]);
  }


}
