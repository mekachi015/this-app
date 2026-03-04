import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../services/authentication-service/auth.service';
import { Router } from '@angular/router';
import Swal from 'sweetalert2';

interface SidebarItem {
  icon: string;
  label: string;
  route: string;
  requiredRole?: string;
  loginRoute?: string;
}

@Component({
  selector: 'app-nav-bar',
  standalone: true,
  imports: [ RouterModule, CommonModule],
  templateUrl: './nav-bar.component.html',
  styleUrl: './nav-bar.component.scss'
})
export class NavBarComponent {

  constructor(public authService: AuthService, private router: Router) {}

  sidebarVisible: boolean = false;

   sidebarItems: SidebarItem[] = [
    { label: 'Home', route: '/home', icon: 'home' },
    { label: 'Profile', route: '/profile', icon: 'user-circle' },
    { label: 'Settings', route: '/settings', icon: 'cog' },
    { label: 'Address', route: '/addresses', icon: 'map-marker-alt', requiredRole: 'CUSTOMER', loginRoute: '/login' },
    { label: 'Administration', route: '/dashboard', icon: 'cogs', requiredRole: 'ADMIN', loginRoute: '/login/admin' },
    { label: 'Driver', route: '/driver', icon: 'car', requiredRole: 'DRIVER', loginRoute: '/login/driver' },
    { label: 'Cart', route: '/cart', icon: 'shopping-cart', requiredRole: 'CUSTOMER', loginRoute: '/login' },
    { label: 'Wishlist', route: '/wishlist', icon: 'heart', requiredRole: 'CUSTOMER', loginRoute: '/login' },
    { label: 'Orders', route: '/customer-orders', icon: 'box', requiredRole: 'CUSTOMER', loginRoute: '/login' },
  ];

  toggleSidebar(): void {
    this.sidebarVisible = !this.sidebarVisible;
    console.log('Sidebar visibility:', this.sidebarVisible); // Debug log
  }

  async handleNavigation(item: SidebarItem): Promise<void> {
    const isLoggedIn = this.authService.isLoggedIn();
    const userRole = this.authService.getUserRole();

    if (item.requiredRole) {
      if (!isLoggedIn) {
        this.toggleSidebar();
        Swal.fire({
          icon: 'info',
          title: 'Login Required',
          text: `Please log in to access ${item.label}.`,
          confirmButtonText: 'Go to Login'
        }).then(() => {
          this.router.navigate([item.loginRoute ?? '/login']);
        });
        return;
      }

      if (userRole !== item.requiredRole) {
        this.toggleSidebar();
        Swal.fire({
          icon: 'error',
          title: 'Access Denied',
          text: `You need to be logged in as ${item.requiredRole.toLowerCase()} to access ${item.label}.`
        });
        return;
      }
    }

    this.router.navigate([item.route]);
    this.toggleSidebar();
  }

  get loginLogoutLabel(): string {
    return this.authService.isLoggedIn() ? 'Logout' : 'Login';
  }

  handleLoginLogout(): void {
    if (this.authService.isLoggedIn()) {
      this.authService.logout();
    } else {
      console.log('Navigating to login page...');
      this.router.navigate(['/login']);
    }
    this.toggleSidebar();
  }
}
