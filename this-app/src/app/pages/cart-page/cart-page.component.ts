import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CartDTO }  from '../../models/cart-model/CartDTO';
import { CartService } from '../../services/cart-service/cart.service';
import {  CartResponse } from '../../models/cart-model/CartResponse';
import { AuthService } from '../../services/authentication-service/auth.service';
import { Router } from '@angular/router';




@Component({
  selector: 'app-cart-page',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './cart-page.component.html',
  styleUrls: ['./cart-page.component.scss'],
})
export class CartPageComponent implements OnInit {

  cartItems: CartDTO[] = [];
  currentUserId: number = 0;
  shipping: number = 150.00;
  isLoading: boolean = false;
  errorMessage: string = '';

  constructor(
    private cartService: CartService,
    private authService: AuthService,
    private router: Router
  ){}

  ngOnInit(): void {
    this.currentUserId = Number(this.authService.currentUserValue?.id || 0);

    if (this.currentUserId > 0){
      this.loadUserCart();
    } else{
      this.errorMessage = 'User not authenticated.';
    }
  }

  navigateToStores(): void {
    this.router.navigate(['/stores']);
  }


  loadUserCart(): void{
    this.isLoading = true;
    this.errorMessage = '';

    this.cartService.getCartByUser(this.currentUserId).subscribe({
      next: (response: CartResponse) => {
        if (response && response.data){
          this.cartItems = Array.isArray(response.data) ? response.data : [response.data];
          console.log('Cart items loaded:', this.cartItems);
        } else {
          this.errorMessage = response.message || 'failed to load cart.';
        }
        this.isLoading = false;
      }, 
      error: (err) => {
        this.errorMessage = 'An error occurred while loading the cart.';
        console.error('Error loading cart:', err);
        this.isLoading = false;
      }
    });
  }

  get subtotal(): number {
    return this.cartItems.reduce((total, item) => total + (item.productPrice * item.quantity), 0);
  }

  get total(): number {
    return this.subtotal + this.shipping;
  }

   increaseQuantity(item: CartDTO): void {
    const newQuantity = item.quantity + 1;
    this.updateQuantity(item, newQuantity);
  }

  decreaseQuantity(item: CartDTO): void {
    if (item.quantity > 1) {
      const newQuantity = item.quantity - 1;
      this.updateQuantity(item, newQuantity);
    }
  }

  updateQuantity(item: CartDTO, newQuantity: number): void {
    this.cartService.updateQuantity(item.cartItemId, this.currentUserId, newQuantity).subscribe({
      next: (response: CartResponse) => {
        if (response.success && response.data) {
          const updatedItem = response.data as CartDTO;
          const index = this.cartItems.findIndex(i => i.cartItemId === item.cartItemId);
          if (index !== -1) {
            this.cartItems[index] = updatedItem;
          }
          console.log('Quantity updated successfully');
        }
      },
      error: (err) => {
        console.error('Failed to update quantity:', err);
        alert('Failed to update quantity. Please try again.');
      }
    });
  }

  removeItem(item: CartDTO): void {
    if (!confirm(`Are you sure you want to remove ${item.productName} from your cart?`)) {
      return;
    }

    this.cartService.removeCartItem(item.cartItemId, this.currentUserId).subscribe({
      next: (response: CartResponse) => {
        if (response.success) {
          this.cartItems = this.cartItems.filter(i => i.cartItemId !== item.cartItemId);
          console.log('Item removed successfully');
        }
      },
      error: (err) => {
        console.error('Failed to remove item:', err);
        alert('Failed to remove item. Please try again.');
      }
    });
  }

  clearCart(): void {
    if (!confirm('Are you sure you want to clear your entire cart?')) {
      return;
    }

    this.cartService.clearCart(this.currentUserId).subscribe({
      next: (response: CartResponse) => {
        if (response.success) {
          this.cartItems = [];
          console.log('Cart cleared successfully');
        }
      },
      error: (err) => {
        console.error('Failed to clear cart:', err);
        alert('Failed to clear cart. Please try again.');
      }
    });
  }

  proceedToCheckout(): void {
  if (this.cartItems.length === 0) {
    alert('Your cart is empty');
    return;
  }

  this.isLoading = true;
  this.errorMessage = '';

  this.cartService.checkout(this.currentUserId).subscribe({
    next: (response: CartResponse) => {
      if (response.success) {
        console.log('Checkout successful:', response);
        
        // Clear cart items from UI
        this.cartItems = [];
        
        // Show success message
        alert(`Checkout successful! ${response.orderCount} order(s) placed. Total: R${response.grandTotal}`);
        
        // Navigate to orders page or order confirmation
        this.router.navigate(['/orders']);
      } else {
        this.errorMessage = response.message || 'Checkout failed';
        alert(this.errorMessage);
      }
      this.isLoading = false;
    },
    error: (err) => {
      this.errorMessage = 'An error occurred during checkout.';
      console.error('Checkout error:', err);
      alert(this.errorMessage);
      this.isLoading = false;
    }
  });
}

  

}
