import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CartDTO }  from '../../models/cart-model/CartDTO';
import { CartService } from '../../services/cart-service/cart.service';
import {  CartResponse } from '../../models/cart-model/CartResponse';
import { AuthService } from '../../services/authentication-service/auth.service';
import { Router } from '@angular/router';
import Swal from 'sweetalert2';




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

   if (this.currentUserId > 0) {
      this.loadUserCart();
    } else {
      Swal.fire({
        icon: 'warning',
        title: 'Not Authenticated',
        text: 'Please log in to view your cart.',
        confirmButtonColor: '#e91e8c',
      }).then(() => this.router.navigate(['/login']));
    }
  }

  navigateToStores(): void {
    this.router.navigate(['/stores']);
  }


  loadUserCart(): void{
    this.isLoading = true;

    this.cartService.getCartByUser(this.currentUserId).subscribe({
      next: (response: CartResponse) => {
        if (response && response.data) {
          this.cartItems = Array.isArray(response.data) ? response.data : [response.data];
        } else {
          Swal.fire({
            icon: 'info',
            title: 'Cart Empty',
            text: response.message || 'Your cart has no items yet.',
            confirmButtonColor: '#e91e8c',
          });
        }
        this.isLoading = false;
      },
      error: (err) => {
        this.isLoading = false;
        Swal.fire({
          icon: 'error',
          title: 'Failed to Load Cart',
          text: 'An error occurred while loading your cart. Please try again.',
          confirmButtonColor: '#e91e8c',
        });
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

          // Subtle toast — doesn't interrupt the user
          Swal.fire({
            toast: true,
            position: 'bottom-end',
            icon: 'success',
            title: 'Quantity updated',
            showConfirmButton: false,
            timer: 1500,
            timerProgressBar: true,
          });
        }
      },
      error: () => {
        Swal.fire({
          icon: 'error',
          title: 'Update Failed',
          text: 'Failed to update quantity. Please try again.',
          confirmButtonColor: '#e91e8c',
        });
      }
    });
  }

  removeItem(item: CartDTO): void {
    Swal.fire({
      icon: 'warning',
      title: 'Remove Item?',
      text: `Are you sure you want to remove "${item.productName}" from your cart?`,
      showCancelButton: true,
      confirmButtonColor: '#e91e8c',
      cancelButtonColor: '#6c757d',
      confirmButtonText: 'Yes, remove it',
      cancelButtonText: 'Keep it',
    }).then((result) => {
      if (!result.isConfirmed) return;

      this.cartService.removeCartItem(item.cartItemId, this.currentUserId).subscribe({
        next: (response: CartResponse) => {
          if (response.success) {
            this.cartItems = this.cartItems.filter(i => i.cartItemId !== item.cartItemId);
            Swal.fire({
              toast: true,
              position: 'bottom-end',
              icon: 'success',
              title: `"${item.productName}" removed`,
              showConfirmButton: false,
              timer: 2000,
              timerProgressBar: true,
            });
          }
        },
        error: () => {
          Swal.fire({
            icon: 'error',
            title: 'Remove Failed',
            text: 'Failed to remove the item. Please try again.',
            confirmButtonColor: '#e91e8c',
          });
        }
      });
    });
  }

  clearCart(): void {
    Swal.fire({
      icon: 'warning',
      title: 'Clear Entire Cart?',
      text: 'This will remove all items from your cart. This action cannot be undone.',
      showCancelButton: true,
      confirmButtonColor: '#e91e8c',
      cancelButtonColor: '#6c757d',
      confirmButtonText: 'Yes, clear it',
      cancelButtonText: 'Cancel',
    }).then((result) => {
      if (!result.isConfirmed) return;

      this.cartService.clearCart(this.currentUserId).subscribe({
        next: (response: CartResponse) => {
          if (response.success) {
            this.cartItems = [];
            Swal.fire({
              icon: 'success',
              title: 'Cart Cleared',
              text: 'All items have been removed from your cart.',
              confirmButtonColor: '#e91e8c',
              timer: 2000,
              showConfirmButton: false,
            });
          }
        },
        error: () => {
          Swal.fire({
            icon: 'error',
            title: 'Failed to Clear Cart',
            text: 'Something went wrong. Please try again.',
            confirmButtonColor: '#e91e8c',
          });
        }
      });
    });
  }

  proceedToCheckout(): void {
   if (this.cartItems.length === 0) {
      Swal.fire({
        icon: 'info',
        title: 'Cart is Empty',
        text: 'Add some items before checking out.',
        confirmButtonColor: '#e91e8c',
      });
      return;
    }

    // Confirm before checkout
    Swal.fire({
      icon: 'question',
      title: 'Confirm Checkout',
      html: `
        <p>You're about to place an order for:</p>
        <strong>R${this.total.toFixed(2)}</strong>
        <p style="font-size:0.85rem;color:#888">(includes R${this.shipping.toFixed(2)} shipping)</p>
      `,
      showCancelButton: true,
      confirmButtonColor: '#e91e8c',
      cancelButtonColor: '#6c757d',
      confirmButtonText: 'Place Order',
      cancelButtonText: 'Review Cart',
    }).then((result) => {
      if (!result.isConfirmed) return;

      this.isLoading = true;

      Swal.fire({
        title: 'Processing your order...',
        allowOutsideClick: false,
        didOpen: () => Swal.showLoading(),
      });

      this.cartService.checkout(this.currentUserId).subscribe({
        next: (response: CartResponse) => {
          this.isLoading = false;

          if (response.success) {
            this.cartItems = [];
            Swal.fire({
              icon: 'success',
              title: 'Order Placed!',
              html: `
                <p>${response.orderCount} order(s) placed successfully.</p>
                <p><strong>Total: R${response.grandTotal}</strong></p>
              `,
              confirmButtonColor: '#e91e8c',
              confirmButtonText: 'View My Orders',
            }).then(() => this.router.navigate(['/customer-orders']));
          } else {
            Swal.fire({
              icon: 'error',
              title: 'Checkout Failed',
              text: response.message || 'Something went wrong during checkout.',
              confirmButtonColor: '#e91e8c',
            });
          }
        },
        error: () => {
          this.isLoading = false;
          Swal.fire({
            icon: 'error',
            title: 'Checkout Error',
            text: 'An unexpected error occurred. Please try again.',
            confirmButtonColor: '#e91e8c',
          });
        }
      });
    });
  }

  

}
