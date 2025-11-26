import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { StoreAdminServiceService } from '../../../services/store-admin-service/store-admin-service.service';
import { ActivatedRoute } from '@angular/router';
//import { Products} from '../../../models/store-front/products.model';

interface Product {
  id: string;
  name: string;
  price: number;
  imageUrl: string;
  description: string;
}

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
  }

  constructor( private route: ActivatedRoute,
    private storeService: StoreAdminServiceService
  ) {
    
  }

  searchQuery: string = '';

  storeName = 'IFUKU ONE';
  products: Product[] = [
    {
    id: '1',
    name: 'IFUKU WIDE CUT PANTS',
    price: 2500.0,
    imageUrl: 'assets/store-pictures/store-1.jpg',
    description: 'Available in various sizes',
  },
  {
    id: '2',
    name: 'IFUKU BLACK T-SHIRT',
    price: 500.0,
    imageUrl: 'assets/store-pictures/store-2.jpg',
    description: 'Layer it up with style. Limited edition',
  },
  {
    id: '3',
    name: 'IFUKU DUNGAREE',
    price: 3000.0,
    imageUrl: 'assets/store-pictures/store-3.jpg',
    description: 'Available now',
  },
  {
    id: '4',
    name: 'IFUKU JACKET',
    price: 3500.0,
    imageUrl: 'assets/store-pictures/store-4.jpg',
    description: 'Premium quality jacket',
  },
  {
    id: '5',
    name: 'IFUKU PREMIUM SET',
    price: 4500.0,
    imageUrl: 'assets/store-pictures/store-6.jpg',
    description: 'Complete premium outfit set',
  }
  ];

  public storeId: string | null = null;

  onSearch(): void {
    // Implement search functionality
    console.log('Searching for:', this.searchQuery);
  }

  addToCart(product: Product): void {
    // Implement add to cart functionality
    console.log('Added to cart:', product);
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

    this.storeService.getStoreById(idNum).subscribe({
      next: (store) => {
        console.log('Loaded store:', store);
        // TODO: assign store data to component properties as needed
      },
      error: (err) => {
        console.error('Failed to load store:', err);
      }
    });
  }

  
}
