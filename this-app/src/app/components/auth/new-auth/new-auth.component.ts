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
import Swal  from 'sweetalert2';

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
    // console.log('User type set to:', this.userType);
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
  if (this.formGroup.invalid) {
      this.markFormGroupTouched();
      Swal.fire({
        icon: 'warning',
        title: 'Incomplete Form',
        text: 'Please fill in all required fields correctly before submitting.',
        confirmButtonText: 'OK',
        confirmButtonColor: '#e91e8c',
      });
      return;
    }

    this.errorMessage = '';
    this.isLoading = true;

    if (this.isLoginMode) {
      this.login();
    } else {
      this.register();
    }
  }

  private login(): void {
    const { username, password } = this.formGroup.value;
    
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

    Swal.fire({
      title: 'Signing you in...',
      allowOutsideClick: false,
      didOpen: () => Swal.showLoading(),
      timer: 2000, // Simulate a delay for better UX
      timerProgressBar: true,
      confirmButtonColor: '#e91e8c',
    });

    loginObservable.subscribe({
      next: (user) => {
        this.isLoading = false;

        // Validate user type access
        if (this.validateUserTypeAccess(user.userType)) {
          Swal.fire({
            icon: 'success',
            title: 'Login Successful',
            text: `Welcome back, ${user.firstname} ${user.lastname} !`,
            confirmButtonText: 'Continue',
            confirmButtonColor: '#e91e8c',
          }).then(() => {
            this.navigateAfterSuccess();
          })
        } else {
          // User logged in with wrong user type - logout immediately
          this.authService.logout(false);
          Swal.fire({
            icon: 'error',
            title: 'Access Denied',
            text: `Your account does not have ${this.userType.toLowerCase()} privileges. Please use the correct login page or contact support.`,
          });
        }
      },
      error: (error) => {
        this.isLoading = false;
        // Enhanced error message handling
       Swal.fire({
          icon: 'error',
          title: 'Login Failed',
          text: this.getLoginErrorMessage(error),
          confirmButtonColor: '#e91e8c',
        });
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
      this.isLoading = false;
      Swal.fire({
        icon: 'warning',
        title: 'Passwords Don\'t Match',
        text: 'Please make sure your passwords match before continuing.',
        confirmButtonColor: '#e91e8c',
      });
      return;
    }

    let registerObservable: Observable<User>;

    switch (this.userType) {
      case 'DRIVER':
        registerObservable = this.authService.registerDriver(this.formGroup.value);
        break;
      case 'ADMIN':
        registerObservable = this.authService.registerAdmin(this.formGroup.value);
        break;
      default:
        registerObservable = this.authService.register(this.formGroup.value, this.userType);
        break;
    }

    // Show loading state
    Swal.fire({
      title: 'Creating your account...',
      allowOutsideClick: false,
      didOpen: () => Swal.showLoading(),
    });

    registerObservable.subscribe({
      next: (user) => {
        this.isLoading = false;
        Swal.fire({
          icon: 'success',
          title: 'Account Created!',
          text: `Your ${this.userType.toLowerCase()} account has been created successfully.`,
          confirmButtonColor: '#e91e8c',
          timer: 2000,
          showConfirmButton: false,
        }).then(() => this.navigateAfterSuccess());
      },
      error: (error) => {
        this.isLoading = false;
        Swal.fire({
          icon: 'error',
          title: 'Registration Failed',
          text: this.getRegisterErrorMessage(error),
          confirmButtonColor: '#e91e8c',
        });
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

   private getLoginErrorMessage(error: any): string {
    switch (error.status) {
      case 401: return 'Invalid username or password. Please check your credentials and try again.';
      case 403: return `Access denied. You don't have ${this.userType.toLowerCase()} privileges.`;
      case 404: return 'User account not found. Please check your username or create a new account.';
      default:  return error.message || 'Login failed. Please try again or contact support.';
    }
  }

  private getRegisterErrorMessage(error: any): string {
    switch (error.status) {
      case 409: return 'An account with this email or username already exists. Try logging in instead.';
      case 400: return error.message || 'Invalid registration data. Please check all fields and try again.';
      default:  return error.message || 'Registration failed. Please try again or contact support.';
    }
  }
}
