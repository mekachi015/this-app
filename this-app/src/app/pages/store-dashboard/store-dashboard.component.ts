import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

interface Order {
  id: string;
  customer: string;
  products: string;
  total: number;
  status: 'Completed' | 'Pending' | 'Cancelled';
}

@Component({
  selector: 'app-store-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './store-dashboard.component.html',
  styleUrl: './store-dashboard.component.scss'
})
export class StoreDashboardComponent {
  recentOrders: Order[] = [
    {
      id: 'ORD-001',
      customer: 'John Doe',
      products: '2 items',
      total: 4999.99,
      status: 'Completed'
    },
    {
      id: 'ORD-002',
      customer: 'Jane Smith',
      products: '1 item',
      total: 2500.00,
      status: 'Pending'
    },
    {
      id: 'ORD-003',
      customer: 'Mike Johnson',
      products: '3 items',
      total: 7500.00,
      status: 'Cancelled'
    }
  ];
}