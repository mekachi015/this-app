import { Component, Input } from '@angular/core';
import { Store } from '../../../models/store-front/store.model';

@Component({
  selector: 'app-store-card',
  standalone: true,
  imports: [],
  templateUrl: './store-card.component.html',
  styleUrl: './store-card.component.scss'
})
export class StoreCardComponent {
  @Input() store!: Store;
  
  selectStore() {
    // Handle store selection logic
  }
}
