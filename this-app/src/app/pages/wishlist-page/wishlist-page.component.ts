import { Component } from '@angular/core';
import { StoreCardComponent } from '../../components/store-front/store-card/store-card.component';
import { SearchBarComponent } from '../../components/search-bar/search-bar.component';
import { CommonModule } from '@angular/common';


@Component({
  selector: 'app-wishlist-page',
  standalone: true,
  imports: [CommonModule, StoreCardComponent, SearchBarComponent],
  templateUrl: './wishlist-page.component.html',
  styleUrl: './wishlist-page.component.scss'
})
export class WishlistPageComponent {
   favoriteStores = [
  {
      id: '1',
      name: 'IFUKU Store',
      imageUrl: 'assets/store-pictures/store-3.jpg',
      bestseller: 'Japanese Denim Collection',
      category: 'Japanese Fashion',
      rating: 95,
      operatingHours: '09:00 - 21:00',
      description: 'Premium Japanese streetwear and authentic denim pieces.'
    },
    {
      id: '2',
      name: 'VINTAGE Corner',
      imageUrl: 'assets/store-pictures/store-1.jpg',
      bestseller: 'Vintage Leather Jackets',
      category: 'Vintage Fashion',
      rating: 92,
      operatingHours: '10:00 - 19:00',
      description: 'Curated vintage pieces from the 80s and 90s.'
    },
    {
      id: '3',
      name: 'STREETWEAR Hub',
      imageUrl: 'assets/store-pictures/store-6.jpg',
      bestseller: 'Urban Streetwear Collection',
      category: 'Street Fashion',
      rating: 94,
      operatingHours: '11:00 - 20:00',
      description: 'Contemporary urban fashion for street style enthusiasts.'
    },
    {
      id: '4',
      name: 'LUXURY Boutique',
      imageUrl: 'assets/store-pictures/store-4.jpg',
      bestseller: 'Designer Collections',
      category: 'Luxury Fashion',
      rating: 97,
      operatingHours: '10:00 - 18:00',
      description: 'Exclusive designer pieces and high-end fashion.'
    },
    {
      id: '1',
      name: 'IFUKU Store',
      imageUrl: 'assets/store-pictures/store-3.jpg',
      bestseller: 'Japanese Denim Collection',
      category: 'Japanese Fashion',
      rating: 95,
      operatingHours: '09:00 - 21:00',
      description: 'Premium Japanese streetwear and authentic denim pieces.'
    },
    {
      id: '2',
      name: 'VINTAGE Corner',
      imageUrl: 'assets/store-pictures/store-1.jpg',
      bestseller: 'Vintage Leather Jackets',
      category: 'Vintage Fashion',
      rating: 92,
      operatingHours: '10:00 - 19:00',
      description: 'Curated vintage pieces from the 80s and 90s.'
    },
    {
      id: '3',
      name: 'STREETWEAR Hub',
      imageUrl: 'assets/store-pictures/store-6.jpg',
      bestseller: 'Urban Streetwear Collection',
      category: 'Street Fashion',
      rating: 94,
      operatingHours: '11:00 - 20:00',
      description: 'Contemporary urban fashion for street style enthusiasts.'
    },
    {
      id: '4',
      name: 'LUXURY Boutique',
      imageUrl: 'assets/store-pictures/store-4.jpg',
      bestseller: 'Designer Collections',
      category: 'Luxury Fashion',
      rating: 97,
      operatingHours: '10:00 - 18:00',
      description: 'Exclusive designer pieces and high-end fashion.'
    }
];

  favoriteClothes = [
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

}
