import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  Validators,
  AbstractControl,
  ValidationErrors,
  ReactiveFormsModule,
} from '@angular/forms';
import { Router, ActivatedRoute, RouterLink } from '@angular/router';
import { AuthService } from '../../../services/authentication-service/auth.service';
import { User } from '../../../models/user/user';
import { Observable } from 'rxjs/internal/Observable';

@Component({
  selector: 'app-new-auth',
  standalone: true,
  imports: [CommonModule, RouterLink, ReactiveFormsModule],
  templateUrl: './new-auth.component.html',
  styleUrl: './new-auth.component.scss',
})
export class NewAuthComponent implements OnInit {
  userType: string = 'CUSTOMER';

  formGroup: FormGroup;
  title!: string;
  subtitle!: string;
  buttonText!: string;
  isLoginMode: boolean = true;
  errorMessage: string = '';
  isLoading: boolean = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.formGroup = this.fb.group({});
  }

  ngOnInit(): void {
    // Determine if we're in login or registration mode based on route
    this.isLoginMode = this.router.url.includes('login');

    //determin usertype based on route
    this.setUserTypeFromRoute();
    this.initializeForm();
    this.setTitles();
  }

  private setUserTypeFromRoute(): void {
    const url = this.router.url;
    if (url.includes('admin')) {
      this.userType = 'ADMIN';
    } else if (url.includes('driver')) {
      this.userType = 'DRIVER';
    } else {
      this.userType = 'CUSTOMER';
    }
    console.log('User type set to:', this.userType);
  }

  private initializeForm(): void {
    if (this.isLoginMode) {
      this.formGroup = this.fb.group({
        username: ['', [Validators.required]],
        password: ['', Validators.required],
      });
    } else {
      this.formGroup = this.fb.group(
        {
          fullName: ['', Validators.required],
          username: ['', Validators.required],
          email: ['', [Validators.required, Validators.email]],
          phoneNumber: [''],
          password: ['', [Validators.required, Validators.minLength(6)]],
          confirmPassword: ['', Validators.required],
        },
        { validators: this.passwordMatchValidator }
      );
    }
  }

  private setTitles(): void {
    const userTypeLabel = this.userType.toLowerCase();
    if (this.isLoginMode) {
      this.title = `${this.userType} Login`;
      this.subtitle = `Please enter your ${userTypeLabel} credentials`;
      this.buttonText = 'Sign In';
    } else {
      this.title = `Create ${this.userType} Account`;
      this.subtitle = `Fill in your details to create a ${userTypeLabel} account`;
      this.buttonText = 'Sign Up';
    }
  }

  private passwordMatchValidator(
    control: AbstractControl
  ): ValidationErrors | null {
    const password = control.get('password')?.value;
    const confirmPassword = control.get('confirmPassword')?.value;
    return password === confirmPassword ? null : { passwordMismatch: true };
  }

  handleSubmit(): void {
    console.log('HandleSubmit called');
    console.log('Form valid:', this.formGroup.valid);
    console.log('Form value:', this.formGroup.value);
    console.log('Is login mode:', this.isLoginMode);

    if (this.formGroup.invalid) {
      console.log('Form is invalid, marking as touched');
      this.markFormGroupTouched();
      return;
    }

    this.errorMessage = '';
    this.isLoading = true;

    if (this.isLoginMode) {
      console.log('Calling login method');
      this.login();
    } else {
      console.log('Calling register method');
      this.register();
    }
  }

  private login(): void {
    console.log('Login method called');
    console.log('Form valid:', this.formGroup.valid);
    console.log('Form errors:', this.formGroup.errors);

    const { username, password } = this.formGroup.value;
    console.log('Login attempt with:', { username, password: password });

    // Use the appropriate login method based on userType
    let loginObservable: Observable<User>;

    switch (this.userType) {
      case 'DRIVER':
        loginObservable = this.authService.loginDriver(username, password);
        break;
      case 'ADMIN':
        loginObservable = this.authService.loginAdmin(username, password);
        break;
      case 'CUSTOMER':
      default:
        loginObservable = this.authService.loginCustomer(username, password);
        break;
    }

    loginObservable.subscribe({
      next: (user) => {
        console.log('Login successful, user:', user);
        this.router.navigate(['/dashboard']);
        this.isLoading = false;
        if (user.userType === 'ADMIN') {
          this.navigateAfterSuccess();
        } else {
          this.errorMessage = `Access denied. This page is for ${this.userType} users only.`;
        }
        
      },
       error: (error) => {
      this.isLoading = false;
      // Enhanced error message handling
      if (error.status === 401) {
        this.errorMessage = 'Invalid username or password. Please check your credentials and try again.';
      } else if (error.status === 403) {
        this.errorMessage = `Access denied. You don't have ${this.userType.toLowerCase()} privileges.`;
      } else if (error.status === 404) {
        this.errorMessage = 'User account not found. Please check your username or create a new account.';
      } else {
        this.errorMessage = error.message || 'Login failed. Please try again or contact support if the problem persists.';
      }
    },
    });
  }

  private validateUserTypeAccess(userType: string): boolean {
    return userType === this.userType;
  }
  getSignUpRoute(): string {
    switch (this.userType) {
      case 'ADMIN':
        return '/sign-up/admin';
      case 'DRIVER':
        return '/sign-up/driver';
      case 'CUSTOMER':
      default:
        return '/sign-up';
    }
  }

  getLoginRoute(): string {
    switch (this.userType) {
      case 'ADMIN':
        return '/login/admin';
      case 'DRIVER':
        return '/login/driver';
      case 'CUSTOMER':
      default:
        return '/login';
    }
  }

  private navigateAfterSuccess(): void {
    switch (this.userType) {
      case 'ADMIN':
        this.router.navigate(['/dashboard']);
        break;
      case 'DRIVER':
        this.router.navigate(['/driver']);
        break;
      case 'CUSTOMER':
      default:
        this.router.navigate(['/stores']);
        break;
    }
  }

  private register(): void {
    if (this.formGroup.hasError('passwordMismatch')) {
      this.errorMessage = 'Passwords do not match';
      this.isLoading = false;
      return;
    }

    // Use the appropriate registration method based on userType
    let registerObservable: Observable<User>;

    switch (this.userType) {
      case 'DRIVER':
        registerObservable = this.authService.registerDriver(
          this.formGroup.value
        );
        break;
      case 'ADMIN':
        registerObservable = this.authService.registerAdmin(
          this.formGroup.value
        );
        break;
      case 'CUSTOMER':
      default:
        registerObservable = this.authService.register(
          this.formGroup.value,
          this.userType
        );
        break;
    }

    registerObservable.subscribe({
      next: (user) => {
        this.isLoading = false;
        this.navigateAfterSuccess();
      },
      error: (error) => {
      this.isLoading = false;
      // Enhanced error message handling
      if (error.status === 409) {
        this.errorMessage = 'An account with this email or username already exists. Please use different credentials or try logging in.';
      } else if (error.status === 400) {
        this.errorMessage = error.message || 'Invalid registration data. Please check all fields and try again.';
      } else {
        this.errorMessage = error.message || 'Registration failed. Please try again or contact support if the problem persists.';
      }
    },
    });
  }

  private markFormGroupTouched(): void {
    Object.keys(this.formGroup.controls).forEach((key) => {
      this.formGroup.get(key)?.markAsTouched();
    });
  }

  passwordVisibility: { [key: string]: boolean } = {
    password: false,
    'reg-password': false,
    confirmPassword: false,
  };

  // Add this method to your NewAuthComponent class
  togglePasswordVisibility(fieldId: string): void {
    this.passwordVisibility[fieldId] = !this.passwordVisibility[fieldId];

    const passwordInput = document.getElementById(fieldId) as HTMLInputElement;
    if (passwordInput) {
      passwordInput.type = this.passwordVisibility[fieldId]
        ? 'text'
        : 'password';
    }

    // Update the eye icon
    const toggleButton = passwordInput?.nextElementSibling as HTMLButtonElement;
    const icon = toggleButton?.querySelector('i');
    if (icon) {
      icon.className = this.passwordVisibility[fieldId]
        ? 'fas fa-eye-slash'
        : 'fas fa-eye';
    }
  }
}
