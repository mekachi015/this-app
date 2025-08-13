import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Delivery } from '../../models/delivery-models/delivery/delivery';
import { DeliveryItem } from '../../models/delivery-models/delivery-item/DeliveryItem';

@Component({
  selector: 'app-driver-component',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './driver-component.component.html',
  styleUrl: './driver-component.component.scss'
})
export class DriverComponentComponent implements OnInit{

  currentDelivery: Delivery = {
    id: 'del_001',
    customerId: 'cust_001',
    customerName: 'John Doe',
    customerPhone: '+1234567890',
    location: {
      latitude: 40.7128,
      longitude: -74.0060,
      address: '123 Fashion Street',
      city: 'New York',
      postalCode: '10001'
    },
    status: {
      id: 'status_001',
      status: 'pending',
      timestamp: new Date()
    },
    items: [
      { id: 'item_001', name: 'Designer Jeans', quantity: 1, price: 99.99 },
      { id: 'item_002', name: 'Cotton T-Shirt', quantity: 2, price: 29.99 }
    ],
    estimatedDeliveryTime: new Date(Date.now() + 30 * 60000),
    createdAt: new Date(),
    updatedAt: new Date()
  };

  upcomingDeliveries: Delivery[] = [
    {
      id: 'del_002',
    customerId: 'cust_002',
    customerName: 'Sarah Williams',
    customerPhone: '+1234567891',
    location: {
      latitude: 40.7142,
      longitude: -74.0064,
      address: '456 Madison Avenue',
      city: 'New York',
      postalCode: '10022'
    },
    status: {
      id: 'status_002',
      status: 'pending',
      timestamp: new Date()
    },
    items: [
      { id: 'item_003', name: 'Summer Dress', quantity: 1, price: 129.99 },
      { id: 'item_004', name: 'Sandals', quantity: 1, price: 59.99 }
    ],
    estimatedDeliveryTime: new Date(Date.now() + 60 * 60000),
    createdAt: new Date(),
    updatedAt: new Date()
    }
  ];

  constructor() {}

  ngOnInit(): void {
    //Initialize map and other resources
  }

   async startDelivery(): Promise<void> {
    // Implementation
    console.log('Delivery started for:', this.currentDelivery.customerName);
  }

  async markAsDelivered(): Promise<void> {
    // Implementation
    console.log('Delivery marked as delivered for:', this.currentDelivery.customerName);
  }

  async callCustomer(): Promise<void> {
    // Implementation
    console.log('Calling customer:', this.currentDelivery.customerName);
  }

}
