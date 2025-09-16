import { Component } from '@angular/core';
import { FormBuilder, Validators, FormGroup } from '@angular/forms';
import { AuthFormComponent } from "../auth-form/auth-form.component";
import { AuthService } from '../../../services/authentication-service/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-sign-up',
  standalone: true,
  imports: [AuthFormComponent],
  templateUrl: './sign-up.component.html',
  styleUrl: './sign-up.component.scss'
})
export class SignUpComponent {
  signupForm: FormGroup;
  errorMessage = "";
  isLoading = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.signupForm = this.fb.group({
      fullName: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', Validators.required],
      phoneNumber: ['', Validators.required],
    }, {
      validators: this.passwordMatchValidator
    });
  }

  passwordMatchValidator(g: FormGroup) {
    return g.get('password')?.value === g.get('confirmPassword')?.value
      ? null : { mismatch: true };
  }

  onSignup() {
    if (this.signupForm.valid) {
      this.errorMessage = '';
      this.isLoading = true;
      
      const { fullName, email, password, phoneNumber } = this.signupForm.value;
      
      // Generate username from full name (make it more unique)
      const username = fullName.toLowerCase()
        .replace(/\s+/g, '') + Math.floor(Math.random() * 1000);
      
      const userData = {
        fullName,
        email,
        password,
        username,
        phoneNumber
      };

      this.authService.register(userData).subscribe({
        next: (user) => {
          console.log('Registration successful:', user);
          this.isLoading = false;
          
          // Navigate to login page with success message
          this.router.navigate(['/login'], { 
            queryParams: { 
              registered: 'true', 
              email: email,
              message: 'Registration successful! Please log in.'
            } 
          });
        },
        error: (error) => {
          console.error('Registration failed:', error);
          this.isLoading = false;
          
          // Handle specific error cases
          if (error.status === 409) {
            this.errorMessage = 'Email or username already exists. Please use different credentials.';
          } else if (error.status === 0) {
            this.errorMessage = 'Cannot connect to server. Please try again later.';
          } else {
            this.errorMessage = error.message || 'Registration failed. Please try again.';
          }
        }
      });
    } else {
      // Handle form validation errors
      if (this.signupForm.hasError('mismatch')) {
        this.errorMessage = 'Passwords do not match.';
      } else {
        this.errorMessage = 'Please fill in all required fields correctly.';
      }
    }
  }
}