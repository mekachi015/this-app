import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

interface SalesData {
  period: string;
  amount: number;
}

interface Product {
  name: string;
  stock: number;
}

interface PromoItem {
  name: string;
  type: string;
}

interface Message {
  from: string;
}

@Component({
  selector: 'app-store-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './store-dashboard.component.html',
  styleUrl: './store-dashboard.component.scss'
})
export class StoreDashboardComponent {
  products: Product[] = [
    { name: 'Product A', stock: 138 },
    { name: 'Product B', stock: 138 },
    { name: 'Product C', stock: 138 }
  ];

  sales: SalesData[] = [
    { period: "Today's Sales", amount: 8000 },
    { period: 'Weekly Sales', amount: 80000 },
    { period: 'Monthly Sales', amount: 300000 }
  ];

  promos: PromoItem[] = [
    { name: 'Discount A', type: 'discount' },
    { name: 'Offer B', type: 'offer' },
    { name: 'Campaign C', type: 'campaign' }
  ];

  messages: Message[] = [
    { from: 'John Smith' },
    { from: 'Jane Doe' },
    { from: 'Alice Brown' }
  ];
}