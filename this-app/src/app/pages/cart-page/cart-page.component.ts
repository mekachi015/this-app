import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

interface CartItem {
  id: string;
  brand: string;
  name: string;
  price: number;
  color: string;
  size: string;
  imageUrl: string;
  quantity: number;
}

@Component({
  selector: 'app-cart-page',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './cart-page.component.html',
  styleUrl: './cart-page.component.scss'
})
export class CartPageComponent {
  cartItems: CartItem[] = [
    {
      id: '1',
      brand: 'Needles x Reebok',
      name: 'Instapump Fury 94',
      price: 4399.00,
      color: 'Canvas',
      size: '7',
      imageUrl: 'assets/products/instapump.jpg',
      quantity: 1
    }
  ];

  currentStep: number = 1;
  steps = ['Cart', 'Delivery', 'Payment'];

  get subtotal(): number {
    return this.cartItems.reduce((sum, item) => sum + (item.price * item.quantity), 0);
  }

  removeItem(itemId: string): void {
    this.cartItems = this.cartItems.filter(item => item.id !== itemId);
  }

  updateQuantity(item: CartItem, newQuantity: number): void {
    if (newQuantity > 0) {
      item.quantity = newQuantity;
    }
  }
}