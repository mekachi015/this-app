import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';

interface SidebarItem {
  icon: string;
  label: string;
  route: string;
}

@Component({
  selector: 'app-nav-bar',
  standalone: true,
  imports: [ RouterModule, CommonModule],
  templateUrl: './nav-bar.component.html',
  styleUrl: './nav-bar.component.scss'
})
export class NavBarComponent {
  sidebarVisible: boolean = false;

   sidebarItems: SidebarItem[] = [
    { label: 'Home', route: '/home', icon: 'home' },
    { label: 'Profile', route: '/profile', icon: 'user-circle' },
    { label: 'Settings', route: '/settings', icon: 'cog' }
  ];

  toggleSidebar(): void {
    this.sidebarVisible = !this.sidebarVisible;
    console.log('Sidebar visibility:', this.sidebarVisible); // Debug log
  }
}
