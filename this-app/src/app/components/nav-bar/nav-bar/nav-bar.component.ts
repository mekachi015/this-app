import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';

interface SidebarItem {
  icon: string;
  label: string;
  route: string;
}

@Component({
  selector: 'app-nav-bar',
  standalone: true,
  imports: [ RouterModule],
  templateUrl: './nav-bar.component.html',
  styleUrl: './nav-bar.component.scss'
})
export class NavBarComponent {
  sidebarVisible = false;
  sidebarItems = [
    { label: 'Google Analytics', route: '/analytics' },
    { label: 'Google Drive', route: '/drive' },
    { label: 'Google Maps', route: '/maps' },
    { label: 'Google Mail', route: '/mail' }
  ];

  toggleSidebar() {
    this.sidebarVisible = !this.sidebarVisible;
  }
}
