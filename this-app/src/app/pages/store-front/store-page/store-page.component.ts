import { Component } from '@angular/core';
import { Store } from '../../../models/store-front/store.model';
import { StoreCardComponent } from "../../../components/store-front/store-card/store-card.component";
import { NearYouComponent } from '../../../components/store-front/near-you/near-you.component';
import { NavBarComponent } from "../../../components/nav-bar/nav-bar/nav-bar.component";
import { CommonModule } from '@angular/common';
import { SearchBarComponent } from '../../../components/search-bar/search-bar.component';


@Component({
  selector: 'app-store-page',
  standalone: true,
  imports: [StoreCardComponent, NearYouComponent,CommonModule, SearchBarComponent],
  templateUrl: './store-page.component.html',
  styleUrl: './store-page.component.scss'
})
export class StorePageComponent {

  featuredStores: Store[] = [
    {
      id: '1',
      name: 'IFUKU',
      imageUrl: 'assets/store-pictures/store-3.jpg',
      description: 'Premium Japanese streetwear and denim. Authentic pieces curated from Tokyo.',
    },
    {
      id: '2',
      name: 'BROKE',
      imageUrl: 'assets/store-pictures/store-2.jpg',
      description: 'Affordable streetwear for the fashion-conscious. Style doesn\'t have to break the bank.',
    },
    {
      id: '5',
      name: 'VINTAGE',
      imageUrl: 'assets/store-pictures/store-1.jpg',
      description: 'Carefully selected vintage pieces from the 80s and 90s. Each item tells a story.',
    },
    {
      id: '6',
      name: 'STREETWEAR',
      imageUrl: 'assets/store-pictures/store-6.jpg',
      description: 'Contemporary urban fashion meets classic street style. For those who dare to stand out.',
    },
    {
      id: '7',
      name: 'LUXURY',
      imageUrl: 'assets/store-pictures/store-4.jpg',
      description: 'Exclusive designer pieces and high-end fashion. Luxury reimagined for modern taste.',
    }
    // Remove duplicates for cleaner implementation

  ];

  nearbyStores: Store[] = [
    {
      id: '3',
      name: 'Testseller',
      imageUrl: 'assets/stores/testseller.jpg',
      bestseller: 'Everything',
      category: 'Thrifting',
      rating: 95,
      operatingHours: '09:00 - 21:00'
    },
    {
      id: '4',
      name: 'IFUKU',
      imageUrl: 'assets/stores/ifuku.jpg',
      bestseller: 'Blue Japanese Denim',
      category: 'Japanese Denim',
      rating: 95,
      operatingHours: '09:00 - 17:00'
    },{
      id: '3',
      name: 'Testseller',
      imageUrl: 'assets/stores/testseller.jpg',
      bestseller: 'Everything',
      category: 'Thrifting',
      rating: 95,
      operatingHours: '09:00 - 21:00'
    },
    {
      id: '4',
      name: 'IFUKU',
      imageUrl: 'assets/stores/ifuku.jpg',
      bestseller: 'Blue Japanese Denim',
      category: 'Japanese Denim',
      rating: 95,
      operatingHours: '09:00 - 17:00'
    }
  ];
}
