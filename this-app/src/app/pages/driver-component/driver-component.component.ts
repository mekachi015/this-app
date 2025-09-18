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
      status: 'cancelled', // 'pending' | 'in-progress' | 'delivered' | 'cancelled'
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
    id: 'del_003',
    customerId: 'cust_003',
    customerName: 'Michael Johnson',
    customerPhone: '+1234567892',
    location: {
      latitude: 40.7589,
      longitude: -73.9851,
      address: '789 Fifth Avenue',
      city: 'New York',
      postalCode: '10019'
    },
    status: {
      id: 'status_003',
      status: 'pending',
      timestamp: new Date()
    },
    items: [
      { id: 'item_005', name: 'Leather Jacket', quantity: 1, price: 299.99 },
      { id: 'item_006', name: 'Designer Scarf', quantity: 1, price: 89.99 }
    ],
    estimatedDeliveryTime: new Date(Date.now() + 90 * 60000),
    createdAt: new Date(),
    updatedAt: new Date()
  },
  {
    id: 'del_004',
    customerId: 'cust_004',
    customerName: 'Emma Davis',
    customerPhone: '+1234567893',
    location: {
      latitude: 40.7527,
      longitude: -73.9772,
      address: '321 Park Avenue',
      city: 'New York',
      postalCode: '10016'
    },
    status: {
      id: 'status_004',
      status: 'pending',
      timestamp: new Date()
    },
    items: [
      { id: 'item_007', name: 'Evening Gown', quantity: 1, price: 459.99 },
      { id: 'item_008', name: 'Clutch Purse', quantity: 1, price: 129.99 },
      { id: 'item_009', name: 'High Heels', quantity: 1, price: 199.99 }
    ],
    estimatedDeliveryTime: new Date(Date.now() + 120 * 60000),
    createdAt: new Date(),
    updatedAt: new Date()
  },
  {
    id: 'del_005',
    customerId: 'cust_005',
    customerName: 'Robert Wilson',
    customerPhone: '+1234567894',
    location: {
      latitude: 40.7484,
      longitude: -73.9857,
      address: '567 Broadway',
      city: 'New York',
      postalCode: '10012'
    },
    status: {
      id: 'status_005',
      status: 'pending',
      timestamp: new Date()
    },
    items: [
      { id: 'item_010', name: 'Business Suit', quantity: 1, price: 599.99 },
      { id: 'item_011', name: 'Dress Shoes', quantity: 1, price: 249.99 },
      { id: 'item_012', name: 'Tie Set', quantity: 2, price: 79.99 }
    ],
    estimatedDeliveryTime: new Date(Date.now() + 150 * 60000),
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
