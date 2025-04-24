import { Component } from '@angular/core';
import { Store } from '../../../models/store-front/store.model';
import { StoreCardComponent } from "../../../components/store-front/store-card/store-card.component";
import { NearYouComponent } from '../../../components/store-front/near-you/near-you.component';
import { NavBarComponent } from "../../../components/nav-bar/nav-bar/nav-bar.component";
@Component({
  selector: 'app-store-page',
  standalone: true,
  imports: [StoreCardComponent, NearYouComponent,],
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
    }
  ];
}
