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
  styleUrl: './selected-store.component.scss'
})
export class SelectedStoreComponent {

  searchQuery: string = '';

  storeName = 'IFUKU ONE';
  products: Product[] = [
    {
      id: '1',
      name: 'IFUKU WIDE CUT PANTS',
      price: 2500.00,
      imageUrl: 'assets/products/pants.jpg',
      description: 'Available in various sizes'
    },
    {
      id: '2',
      name: 'IFUKU BLACK T-SHIRT',
      price: 500.00,
      imageUrl: 'assets/products/tshirt.jpg',
      description: 'Layer it up with style. Limited edition'
    },
    {
      id: '3',
      name: 'IFUKU DUNGAREE',
      price: 3000.00,
      imageUrl: 'assets/products/dungaree.jpg',
      description: 'Available now'
    },{
      id: '1',
      name: 'IFUKU WIDE CUT PANTS',
      price: 2500.00,
      imageUrl: 'assets/products/pants.jpg',
      description: 'Available in various sizes'
    },
    {
      id: '2',
      name: 'IFUKU BLACK T-SHIRT',
      price: 500.00,
      imageUrl: 'assets/products/tshirt.jpg',
      description: 'Layer it up with style. Limited edition'
    },
    {
      id: '3',
      name: 'IFUKU DUNGAREE',
      price: 3000.00,
      imageUrl: 'assets/products/dungaree.jpg',
      description: 'Available now'
    },{
      id: '1',
      name: 'IFUKU WIDE CUT PANTS',
      price: 2500.00,
      imageUrl: 'assets/products/pants.jpg',
      description: 'Available in various sizes'
    },
    {
      id: '2',
      name: 'IFUKU BLACK T-SHIRT',
      price: 500.00,
      imageUrl: 'assets/products/tshirt.jpg',
      description: 'Layer it up with style. Limited edition'
    },
    {
      id: '3',
      name: 'IFUKU DUNGAREE',
      price: 3000.00,
      imageUrl: 'assets/products/dungaree.jpg',
      description: 'Available now'
    }];
}