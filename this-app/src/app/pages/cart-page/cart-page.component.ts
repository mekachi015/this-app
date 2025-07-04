import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

interface CartItem {
  id: string;
  name: string;
  description: string;
  price: number;
  quantity: number;
  imageUrl: string;
}

@Component({
  selector: 'app-cart-page',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './cart-page.component.html',
  styleUrl: './cart-page.component.scss',
})
export class CartPageComponent {
  cartItems: CartItem[] = [
    {
      id: '1',
      name: 'IFUKU WIDE CUT PANTS',
      description: 'Available in various sizes',
      price: 2500.0,
      quantity: 1,
      imageUrl: 'assets/store-pictures/store-1.jpg',
    },
    {
      id: '1',
      name: 'IFUKU WIDE CUT PANTS',
      description: 'Available in various sizes',
      price: 2500.0,
      quantity: 1,
      imageUrl: 'assets/store-pictures/store-1.jpg',
    },
    {
      id: '1',
      name: 'IFUKU WIDE CUT PANTS',
      description: 'Available in various sizes',
      price: 2500.0,
      quantity: 1,
      imageUrl: 'assets/store-pictures/store-1.jpg',
    },
    // Add more items as needed
  ];

  get subtotal(): number {
    return this.cartItems.reduce(
      (sum, item) => sum + item.price * item.quantity,
      0
    );
  }

  shipping: number = 150.0;

  get total(): number {
    return this.subtotal + this.shipping;
  }

  increaseQuantity(item: CartItem): void {
    item.quantity++;
  }

  decreaseQuantity(item: CartItem): void {
    if (item.quantity > 1) {
      item.quantity--;
    }
  }

  removeItem(item: CartItem): void {
    this.cartItems = this.cartItems.filter(
      (cartItem) => cartItem.id !== item.id
    );
  }
}
