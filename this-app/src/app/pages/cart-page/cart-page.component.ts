import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Cart }  from '../../models/cart-model/cart';
import { CartService } from '../../services/cart-service/cart.service';
import { CartRequest } from '../../models/cart-model/cart-request';
import { AuthService } from '../../services/authentication-service/auth.service';


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
  styleUrls: ['./cart-page.component.scss'],
})
export class CartPageComponent implements OnInit {

  ngOnInit(): void {
     this.currentUserId = Number(this.authService.currentUserValue?.id || 0);
     this.loadUserCart();
  }
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

  

  cart: Cart[] = [];
  currentUserId: number = 0;

  constructor(
    private cartService: CartService,
    private authService: AuthService
  ) {}

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

  loadUserCart(): void {
    this.cartService.getCartByUser(this.currentUserId).subscribe({
      next: (response) => {
        // Ensure we always store an array of Cart items
        this.cart = Array.isArray(response) ? response : [response];
        console.log('Cart loaded successfully:', this.cart);
      },
      error: (err) => {
        console.error('Failed to load cart:', err);
      }
    });
  }

  removeItem(cartItem: Cart): void {
    if (!confirm(`Are you sure you want to remove product ${cartItem.productId}?`)) {
      return;
    }
    // Note: The DELETE endpoint requires userId and productId
    this.cartService.removeCartItem(this.currentUserId, cartItem.productId).subscribe({
      next: () => {
        console.log('Item removed successfully.');
        // Remove the item from the local arrays (match by productId/cartId)
        this.cartItems = this.cartItems.filter(item => item.id !== String(cartItem.productId));
        this.cart = this.cart.filter(c => c.cartId !== cartItem.cartId);
      },
      error: (err) => {
        console.error('Failed to remove item:', err);
      }
    });
  }
}
