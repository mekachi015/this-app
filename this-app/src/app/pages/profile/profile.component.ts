import {
  Component,
  OnDestroy,
  OnInit,
  ViewChild,
  ElementRef,
} from '@angular/core';
import { CommonModule } from '@angular/common'; // Import CommonModule for Angular directives
import { Router } from '@angular/router';
import { of, Subscription, switchMap } from 'rxjs';
import { User, UserType } from '../../models/user/user';
import { AuthService } from '../../services/authentication-service/auth.service';
@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.scss',
})
export class ProfileComponent implements OnInit, OnDestroy {
  @ViewChild('fileInput') fileInput!: ElementRef<HTMLInputElement>;

  private userSubscription: Subscription = new Subscription();

  // Add these properties
  userType: UserType = UserType.CUSTOMER; // Default
  UserType = UserType; // Expose enum to template
  isPhotoChanging = false;

  constructor(private router: Router, private authService: AuthService) { }

  userName = 'Guest User';
  userPhotoUrl = 'assets/profile-photos/profile-picture.jpg';
  currentUser: User | null = null;

  ngOnInit(): void {
    this.userSubscription = this.authService.currentUser.subscribe((user) => {
      this.currentUser = user;
      if (user) {
        const firstName = this.initCap(user.firstname);
        const lastName = this.initCap(user.lastname);
        this.userName =
          `${firstName} ${lastName}`.trim() || user.username || 'User';
        // Handle userType conversion from string to enum
        this.userType =
          this.convertStringToUserType(user.userType) || UserType.CUSTOMER;
        this.userPhotoUrl =
          user.profilePhotoUrl + '?t=' + new Date().getTime();
      } else {
        this.userName = 'Guest User';
        this.userPhotoUrl = 'assets/profile-photos/profile-picture.jpg';
        this.userType = UserType.CUSTOMER;
      }
    });
    // }

    //   ngOnInit(): void {
    //   this.userSubscription = this.authService.currentUser
    //     .pipe(
    //       switchMap(user => user ? this.authService.getLatestUserProfile() : of(null))
    //     )
    //     .subscribe(user => {
    //       this.currentUser = user;
    //       if (user) {
    //         const firstName = this.initCap(user.firstname || user.firstname || '');
    //         const lastName = this.initCap(user.lastname || user.lastname || '');
    //         this.userName = `${firstName} ${lastName}`.trim() || user.username || 'User';
    //         this.userPhotoUrl = user.profilePhotoUrl || 'assets/profile-photos/profile-picture.jpg';
    //       }
    //     });
    // }

    // ngOnInit(): void {
    //   // Subscribe to currentUser BehaviorSubject
    //   this.userSubscription = this.authService.currentUser
    //     .pipe(
    //       // If there is a user, fetch latest profile from backend
    //       switchMap((user) =>
    //         user ? this.authService.getLatestUserProfile() : of(null)
    //       )
    //     )
    //     .subscribe((user) => {
    //       this.currentUser = user;

    //       if (user) {
    //         // Handle different backend naming styles safely
    //         const firstName = this.initCap(
    //           user.firstname || user.firstname || ''
    //         );
    //         const lastName = this.initCap(user.lastname || user.lastname || '');
    //         this.userName =
    //           `${firstName} ${lastName}`.trim() || user.firstname || 'User';

    //         // Convert userType safely
    //         this.userType = this.convertStringToUserType(user.userType);

    //         // Display latest Cloudinary profile photo with cache-busting
    //         this.userPhotoUrl = user.profilePhotoUrl
    //           ? `${user.profilePhotoUrl}?t=${new Date().getTime()}`
    //           : 'assets/profile-photos/profile-picture.jpg';
    //       } else {
    //         this.userName = 'Guest User';
    //         this.userPhotoUrl = 'assets/profile-photos/profile-picture.jpg';
    //         this.userType = this.UserType.CUSTOMER;
    //       }
    //     });
  }

  private convertStringToUserType(userTypeString: string | UserType): UserType {
    if (typeof userTypeString === 'string') {
      switch (userTypeString.toUpperCase()) {
        case 'CUSTOMER':
          return UserType.CUSTOMER;
        case 'DRIVER':
          return UserType.DRIVER;
        case 'ADMIN':
          return UserType.ADMIN;
        default:
          return UserType.CUSTOMER;
      }
    }
    return userTypeString || UserType.CUSTOMER;
  }

  // Add these new methods
  onProfilePhotoClick(): void {
    this.fileInput.nativeElement.click();
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files[0]) {
      const file = input.files[0];

      // Validate file type
      if (!file.type.startsWith('image/')) {
        alert('Please select a valid image file.');
        return;
      }

      // Validate file size (max 5MB)
      if (file.size > 5 * 1024 * 1024) {
        alert('File size must be less than 5MB.');
        return;
      }

      this.uploadProfilePhoto(file);
    }
  }

  private uploadProfilePhoto(file: File): void {
    this.isPhotoChanging = true;

    // Create FormData for file upload
    const formData = new FormData();
    formData.append('profilePhoto', file);

    // Call your auth service or create a profile service
    console.log(this.userPhotoUrl, 'User url');
    this.authService.uploadProfilePhotoRefactored(formData).subscribe({
      next: (response: any) => {
        this.userPhotoUrl = response.photoUrl;
        this.currentUser!.profilePhotoUrl = response.photoUrl;
        this.isPhotoChanging = false;

        console.log(this.userPhotoUrl, 'User url after');
        // Update current user data if needed
        if (this.currentUser) {
          this.currentUser.profilePhotoUrl = response.photoUrl;

          // persist change in localStorage
          if (typeof window !== 'undefined' && window.localStorage) {
            localStorage.setItem(
              'currentUser',
              JSON.stringify(this.currentUser)
            );
          }

          //update AuthService BehaviorSubject
          this.authService['currentUserSubject'].next(this.currentUser);
        }
      },
      error: (error) => {
        console.error('Error uploading photo:', error);
        this.isPhotoChanging = false;
        alert('Failed to upload photo. Please try again.');
        // Reset to previous photo on error
        this.userPhotoUrl =
          this.currentUser?.profilePhotoUrl ||
          'assets/profile-photos/profile-picture.jpg';
        console.log(this.userPhotoUrl, 'User url');
      },
    });
  }

  // Update settings based on user type
  getSettingsForUserType(): any[] {
    const baseSettings = [
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
    ];

    const customerSettings = [
      ...baseSettings,
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
    ];

    const driverSettings = [
      ...baseSettings,
      {
        icon: 'fas fa-car',
        name: 'Vehicle Information',
        description: 'Manage your vehicle details and documents.',
      },
      {
        icon: 'fas fa-route',
        name: 'Delivery History',
        description: 'View your completed deliveries and earnings.',
      },
      {
        icon: 'fas fa-clock',
        name: 'Work Schedule',
        description: 'Set your availability and working hours.',
      },
      {
        icon: 'fas fa-wallet',
        name: 'Earnings & Payouts',
        description: 'Track earnings and manage payout methods.',
      },
      {
        icon: 'fas fa-question-circle',
        name: 'Driver Support',
        description: 'Get help with delivery-related issues.',
      },
    ];

    const adminSettings = [
      ...baseSettings,
      {
        icon: 'fas fa-users-cog',
        name: 'User Management',
        description: 'Manage customers, drivers, and other admins.',
      },
      {
        icon: 'fas fa-chart-bar',
        name: 'Analytics Dashboard',
        description: 'View system analytics and reports.',
      },
      {
        icon: 'fas fa-cog',
        name: 'System Settings',
        description: 'Configure system-wide settings.',
      },
      {
        icon: 'fas fa-database',
        name: 'Data Management',
        description: 'Manage application data and backups.',
      },
      {
        icon: 'fas fa-shield-alt',
        name: 'Security Settings',
        description: 'Configure security and access controls.',
      },
    ];

    const logoutSetting = {
      icon: 'fas fa-sign-out-alt',
      name: 'Logout',
      description: 'Log out of the current account.',
    };

    switch (this.userType) {
      case UserType.DRIVER:
        return [...driverSettings, logoutSetting];
      case UserType.ADMIN:
        return [...adminSettings, logoutSetting];
      default:
        return [...customerSettings, logoutSetting];
    }
  }

  // Update the settings getter
  get settings() {
    return this.getSettingsForUserType();
  }

  // Update onSettingClick to handle new settings
  onSettingClick(settingName: string): void {
    switch (settingName) {
      case 'Logout':
        this.logOut();
        break;
      case 'Account Settings':
        // this.router.navigate(['/account-settings']);
        break;
      case 'Privacy':
        // this.router.navigate(['/privacy']);
        break;
      case 'Vehicle Information':
        // this.router.navigate(['/driver/vehicle']);
        break;
      case 'Delivery History':
        // this.router.navigate(['/driver/history']);
        break;
      case 'Work Schedule':
        // this.router.navigate(['/driver/schedule']);
        break;
      case 'Earnings & Payouts':
        // this.router.navigate(['/driver/earnings']);
        break;
      case 'User Management':
        // this.router.navigate(['/admin/users']);
        break;
      case 'Analytics Dashboard':
        // this.router.navigate(['/admin/analytics']);
        break;
      case 'System Settings':
        // this.router.navigate(['/admin/settings']);
        break;
      case 'Data Management':
        // this.router.navigate(['/admin/data']);
        break;
      case 'Security Settings':
        // this.router.navigate(['/admin/security']);
        break;
      default:
        console.log(`${settingName} clicked - not implemented yet`);
    }
  }

  // Rest of your existing methods remain the same...
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

  logOut() {
    if (this.authService.isLoggedIn()) {
      this.authService.logout();
      this.router.navigate(['/login']);
    }
  }

  private initCap(str?: string): string {
    if (!str) return '';
    return str.charAt(0).toUpperCase() + str.slice(1).toLowerCase();
  }
}
