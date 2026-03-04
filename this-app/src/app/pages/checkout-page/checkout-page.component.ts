import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { CartService } from '../../services/cart-service/cart.service';
import { CheckoutService } from '../../services/checkout-service/checkout.service';
import { AuthService } from '../../services/authentication-service/auth.service';
import { CartDTO } from '../../models/cart-model/CartDTO';
import { AddressService } from '../../services/address-service/address.service';
import { Address } from '../../models/address-model/address';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-checkout-page',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './checkout-page.component.html',
  styleUrl: './checkout-page.component.scss'
})
export class CheckoutPageComponent implements OnInit {

  cartItems: CartDTO[] = [];
  addresses: Address[] = [];
  selectedAddressId: number | null = null;
  isLoading = false;
  isAddressLoading = false;
  isCartLoading = false;
  paymentStatus: 'idle' | 'success' | 'cancelled' = 'idle';
  shipping = 0;

  constructor(
    private cartService: CartService,
    private checkoutService: CheckoutService,
    private authService: AuthService,
    public router: Router,
    private route: ActivatedRoute,
    private addressService: AddressService
  ) {}

  ngOnInit(): void {
    // Detect PayFast return routes
    const path = this.route.snapshot.routeConfig?.path ?? '';
    if (path.includes('success')) {
      this.paymentStatus = 'success';
      this.showPaymentResult('success');
      return;
    }
    if (path.includes('cancel')) {
      this.paymentStatus = 'cancelled';
      this.showPaymentResult('cancelled');
      return;
    }

    this.loadCart();
    this.loadAddresses();

    this.checkoutService.getCheckoutConfig().subscribe({
      next: (config) => this.shipping = config.shippingFee,
      error: () => this.shipping = 150 // safety fallback
    });
  }

  private showPaymentResult(status: 'success' | 'cancelled'): void {
    if (status === 'success') {
      Swal.fire({
        icon: 'success',
        title: 'Payment Successful!',
        text: 'Your order has been placed. You will receive a confirmation shortly.',
        confirmButtonColor: '#e91e8c',
        confirmButtonText: 'View My Orders',
      }).then(() => this.router.navigate(['/customer-orders']));
    } else {
      Swal.fire({
        icon: 'info',
        title: 'Payment Cancelled',
        text: 'Your payment was cancelled. Your cart has been kept.',
        confirmButtonColor: '#e91e8c',
        confirmButtonText: 'Return to Cart',
      }).then(() => this.router.navigate(['/cart']));
    }
  }

  loadCart(): void {
    const userId = this.authService.currentUserValue?.id;
    if (!userId) return;

    this.isCartLoading = true;
    this.cartService.getCartByUser(+userId).subscribe({
      next: (res: any) => {
        this.cartItems = Array.isArray(res.data) ? res.data : (res.data ? [res.data] : []);
        this.isCartLoading = false;

        // Enforce single-store rule
        const storeIds = new Set(this.cartItems.map((i: CartDTO) => i.storeId));
        if (storeIds.size > 1) {
          Swal.fire({
            icon: 'error',
            title: 'Mixed Stores in Cart',
            html: `Your cart has items from <strong>${storeIds.size} different stores</strong>.<br>
                   Orders can only be placed from <strong>one store at a time</strong>.<br>
                   Please go back and clear your cart.`,
            confirmButtonColor: '#e91e8c',
            confirmButtonText: 'Back to Cart',
          }).then(() => this.router.navigate(['/cart']));
        }
      },
      error: () => {
        this.isCartLoading = false;
        Swal.fire({
          icon: 'error',
          title: 'Failed to Load Cart',
          text: 'Could not retrieve your cart items.',
          confirmButtonColor: '#e91e8c',
        });
      }
    });
  }

  loadAddresses(): void {
    this.isAddressLoading = true;
    this.addressService.getUserAddresses().subscribe({
      next: (addresses: Address[]) => {
        this.addresses = addresses;
        const defaultAddr = addresses.find(a => a.isDefault);
        this.selectedAddressId = defaultAddr?.addressId ?? addresses[0]?.addressId ?? null;
        this.isAddressLoading = false;
      },
      error: () => {
        this.isAddressLoading = false;
      }
    });
  }

  get selectedAddress(): Address | undefined {
    return this.addresses.find(a => a.addressId === this.selectedAddressId);
  }

  get subtotal(): number {
    return this.cartItems.reduce((sum, item) => sum + item.subtotal, 0);
  }

  get total(): number {
    return this.subtotal + this.shipping;
  }

  get storeName(): string {
    return this.cartItems[0]?.storeName ?? '';
  }

  navigateToAddresses(): void {
    this.router.navigate(['/addresses'], { queryParams: { returnTo: 'checkout' } });
  }

  proceedToPayment(): void {
    const userId = this.authService.currentUserValue?.id;
    if (!userId) {
      this.router.navigate(['/login']);
      return;
    }

    if (this.cartItems.length === 0) {
      Swal.fire({ icon: 'info', title: 'Cart is Empty', text: 'Add items before checking out.', confirmButtonColor: '#e91e8c' });
      return;
    }

    if (!this.selectedAddressId) {
      Swal.fire({ icon: 'warning', title: 'No Address Selected', text: 'Please select or add a delivery address.', confirmButtonColor: '#e91e8c' });
      return;
    }

    Swal.fire({
      icon: 'question',
      title: 'Confirm Payment',
      html: `
        <p>You are about to pay via <strong>PayFast</strong>.</p>
        <p>Order from <strong>${this.storeName}</strong></p>
        <p>Total: <strong>R${this.total.toFixed(2)}</strong></p>
      `,
      showCancelButton: true,
      confirmButtonColor: '#e91e8c',
      cancelButtonColor: '#6c757d',
      confirmButtonText: 'Pay Now',
      cancelButtonText: 'Review Cart',
    }).then(result => {
      if (!result.isConfirmed) return;
      this.initiatePayment(+userId);
    });
  }

  private initiatePayment(userId: number): void {
    this.isLoading = true;

    Swal.fire({
      title: 'Redirecting to PayFast...',
      text: 'Please wait while we prepare your payment.',
      allowOutsideClick: false,
      didOpen: () => Swal.showLoading(),
    });

    this.checkoutService.initiateCheckout(userId, this.selectedAddressId!).subscribe({
      next: (response) => {
        this.isLoading = false;
        Swal.close();
        if (response.paymentUrl) {
          window.location.href = response.paymentUrl;
        } else {
          Swal.fire({ icon: 'error', title: 'Payment Error', text: 'No payment URL received.', confirmButtonColor: '#e91e8c' });
        }
      },
      error: (err) => {
        this.isLoading = false;
        Swal.fire({
          icon: 'error',
          title: 'Checkout Failed',
          text: err?.message ?? 'An unexpected error occurred. Please try again.',
          confirmButtonColor: '#e91e8c',
        });
      }
    });
  }
}
