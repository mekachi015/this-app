import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Store } from '../../../models/store-front/store.model';
import { emit } from 'node:process';

@Component({
  selector: 'app-near-you',
  standalone: true,
  imports: [],
  templateUrl: './near-you.component.html',
  styleUrl: './near-you.component.scss'
})
export class NearYouComponent {
  @Input() store!: Store;
  @Output() storeSelected = new EventEmitter<Store>();

   selectStore() {
    // Handle store selection logic
    this.storeSelected.emit(this.store);
  }

}
