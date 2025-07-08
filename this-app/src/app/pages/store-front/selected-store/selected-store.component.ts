import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
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
  }

  constructor() {
    // Constructor logic can go here
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
}
