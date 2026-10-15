import { Component, EventEmitter, Input, Output } from '@angular/core';
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
  @Output() storeSelected = new EventEmitter<Store>();
  
  selectStore() {
    // Handle store selection logic
    this.storeSelected.emit(this.store);
  }

  
}
