import { Component } from '@angular/core';
import { CommonModule } from '@angular/common'; // Import CommonModule for Angular directives
import { NgModule } from '@angular/core';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.scss'
})
export class ProfileComponent {

  userName = 'Penny Pennyworth'; // Replace with actual user name
  userPhotoUrl = 'assets/profile-photos/profile-picture.jpg'; // Replace with actual photo URL

settings = [
  { icon: 'fas fa-user-cog', name: 'Account Settings', description: 'Manage your account information and preferences.' },
  { icon: 'fas fa-lock', name: 'Privacy', description: 'Control your privacy settings and data.' },
  { icon: 'fas fa-bell', name: 'Notifications', description: 'Set your notification preferences.' },
  { icon: 'fas fa-map-marker-alt', name: 'Addresses', description: 'Manage your saved addresses.' },
  { icon: 'fas fa-credit-card', name: 'Payment Methods', description: 'Add or remove payment methods.' },
  { icon: 'fas fa-question-circle', name: 'Help & Support', description: 'Get help or contact support.' }
];

}
