import { Component, Input } from '@angular/core';
import { Store } from '../../../models/store-front/store.model';

@Component({
  selector: 'app-near-you',
  standalone: true,
  imports: [],
  templateUrl: './near-you.component.html',
  styleUrl: './near-you.component.scss'
})
export class NearYouComponent {
  @Input() store!: Store;

}
