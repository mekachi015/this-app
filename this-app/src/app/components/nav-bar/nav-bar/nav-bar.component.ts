import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../services/authentication-service/auth.service';
import { Router } from '@angular/router';

interface SidebarItem {
  icon: string;
  label: string;
  route: string;
  requiredRole?: string;
}

@Component({
  selector: 'app-nav-bar',
  standalone: true,
  imports: [ RouterModule, CommonModule],
  templateUrl: './nav-bar.component.html',
  styleUrl: './nav-bar.component.scss'
})
export class NavBarComponent {

  constructor(private router: Router, private authService: AuthService) {}

  sidebarVisible: boolean = false;

   sidebarItems: SidebarItem[] = [
    { label: 'Home', route: '/home', icon: 'home' },
    { label: 'Profile', route: '/profile', icon: 'user-circle' },
    { label: 'Settings', route: '/settings', icon: 'cog' },
    { label: 'Administration', route: '/login/admin', icon: 'cogs' },
    {label: 'Driver', route: '/login/driver', icon: 'car'},
    {label: 'Cart', route: '/cart', icon: 'shopping-cart'},
    { label: "Wishlist", route: "/wishlist", icon: "heart" },
    
  ];

  toggleSidebar(): void {
    this.sidebarVisible = !this.sidebarVisible;
    console.log('Sidebar visibility:', this.sidebarVisible); // Debug log
  }

  async handleNavigation(item: SidebarItem): Promise<void> {
    const isLoggedIn = this.authService.isLoggedIn();
    const userRole = this.authService.getUserRole();

    // // If user is not logged in
    // if (!isLoggedIn) {
    //   console.log('User not logged in, redirecting to login...');
    //   this.router.navigate(['/login']);
    //   return;
    // }

    // // // If route has a required role, check it
    // // if (item.requiredRole && userRole !== item.requiredRole) {
    // //   alert(`Unauthorized access. You are logged in as ${userRole}`);
    // //   return;
    // // }

    // Otherwise proceed with navigation
    this.router.navigate([item.route]);
    this.toggleSidebar();
  }
}
