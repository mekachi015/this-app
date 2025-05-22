import { Component } from '@angular/core';
import { Store } from '../../../models/store-front/store.model';
import { StoreCardComponent } from "../../../components/store-front/store-card/store-card.component";
import { NearYouComponent } from '../../../components/store-front/near-you/near-you.component';
import { NavBarComponent } from "../../../components/nav-bar/nav-bar/nav-bar.component";
import { CommonModule } from '@angular/common';
import { SearchBarComponent } from '../../../components/store-searchbar/search-bar/search-bar.component';


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
      imageUrl: 'assets/stores/ifuku.jpg',
    },
    {
      id: '2',
      name: 'BROKE',
      imageUrl: 'assets/stores/broke.jpg',
    },
    {
      id: '5',
      name: 'VINTAGE',
      imageUrl: 'assets/stores/vintage.jpg',
    },
    {
      id: '6',
      name: 'STREETWEAR',
      imageUrl: 'assets/stores/streetwear.jpg',
    },
    {
      id: '7',
      name: 'LUXURY',
      imageUrl: 'assets/stores/luxury.jpg',
    },
    {
      id: '1',
      name: 'IFUKU',
      imageUrl: 'assets/stores/ifuku.jpg',
    },
    {
      id: '2',
      name: 'BROKE',
      imageUrl: 'assets/stores/broke.jpg',
    },
    {
      id: '5',
      name: 'VINTAGE',
      imageUrl: 'assets/stores/vintage.jpg',
    },
    {
      id: '6',
      name: 'STREETWEAR',
      imageUrl: 'assets/stores/streetwear.jpg',
    },
    {
      id: '7',
      name: 'LUXURY',
      imageUrl: 'assets/stores/luxury.jpg',
    }
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
