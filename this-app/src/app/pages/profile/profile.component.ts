import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common'; // Import CommonModule for Angular directives
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { User } from '../../models/user/user';
import { AuthService } from '../../services/authentication-service/auth.service';
@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.scss',
})
export class ProfileComponent implements OnInit, OnDestroy {
  private userSubscription: Subscription = new Subscription();

  constructor(private router: Router, private authService: AuthService) {}

  userName = 'Guest User'; //default fallback
  userPhotoUrl = 'assets/profile-photos/profile-picture.jpg'; // Replace with actual photo URL
  currentUser: User | null = null;

  ngOnInit(): void {
    //subscribe to current user
    this.userSubscription = this.authService.currentUser.subscribe((user) => {
      this.currentUser = user;
      if (user) {
        const firstName = this.initCap(user.firstname);
        const lastName = this.initCap(user.lastname);
        this.userName =
          `${user.firstname} ${user.lastname}`.trim() ||
          user.username ||
          'User';
      } else {
        this.userName = 'Guest User';
        this.userPhotoUrl = 'assets/profile-photos/profile-picture.jpg';
      }
    });
  }

  ngOnDestroy(): void {
    this.userSubscription.unsubscribe();
  }

  goToWishlist() {
    if (this.authService.isLoggedIn()) {
      this.router.navigate(['/wishlist']);
      console.log('Navigating to wishlist');
    }

    console.log('Navigating to wishlist');
  }

  logOut(){
    if(this.authService.isLoggedIn()){
      this.authService.logout();
      this.router.navigate(['/login']);
    }
  }

  settings = [
    {
      icon: 'fas fa-user-cog',
      name: 'Account Settings',
      description: 'Manage your account information and preferences.',
    },
    {
      icon: 'fas fa-lock',
      name: 'Privacy',
      description: 'Control your privacy settings and data.',
    },
    {
      icon: 'fas fa-bell',
      name: 'Notifications',
      description: 'Set your notification preferences.',
    },
    {
      icon: 'fas fa-map-marker-alt',
      name: 'Addresses',
      description: 'Manage your saved addresses.',
    },
    {
      icon: 'fas fa-credit-card',
      name: 'Payment Methods',
      description: 'Add or remove payment methods.',
    },
    {
      icon: 'fas fa-question-circle',
      name: 'Help & Support',
      description: 'Get help or contact support.',
    },
    {
      icon: 'fas fa-sign-out-alt',
      name: 'Logout',
      description: 'Log out of the current account.',
    },
  ];

  onSettingClick(settingName: string): void {
  switch(settingName) {
    case 'Logout':
      this.logOut();
      break;
    case 'Account Settings':
      // Navigate to account settings page when implemented
      // this.router.navigate(['/account-settings']);
      break;
    case 'Privacy':
      // Navigate to privacy page when implemented
      // this.router.navigate(['/privacy']);
      break;
    // Add other cases as needed
    default:
      console.log(`${settingName} clicked - not implemented yet`);
  }
}

  // Helper method for initial capitalization
  private initCap(str: string): string {
    if (!str) return '';
    return str.charAt(0).toUpperCase() + str.slice(1).toLowerCase();
  }
}
