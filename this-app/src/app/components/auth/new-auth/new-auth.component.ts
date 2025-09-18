import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors, ReactiveFormsModule } from '@angular/forms';
import { Router, ActivatedRoute, RouterLink } from '@angular/router';
import { AuthService } from '../../../services/authentication-service/auth.service';



@Component({
  selector: 'app-new-auth',
  standalone: true,
  imports: [CommonModule, RouterLink, ReactiveFormsModule],
  templateUrl: './new-auth.component.html',
  styleUrl: './new-auth.component.scss'
})
export class NewAuthComponent  implements OnInit{

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
    this.initializeForm();
    this.setTitles();
  }

  private initializeForm(): void {
    if (this.isLoginMode) {
      this.formGroup = this.fb.group({
        username: ['', [Validators.required]],
        password: ['', Validators.required]
      });
    } else {
      this.formGroup = this.fb.group({
        fullName: ['', Validators.required],
        username: ['', Validators.required],
        email: ['', [Validators.required, Validators.email]],
        phoneNumber: [''],
        password: ['', [Validators.required, Validators.minLength(6)]],
        confirmPassword: ['', Validators.required]
      }, { validators: this.passwordMatchValidator });
    }
  }

  private setTitles(): void {
    if (this.isLoginMode) {
      this.title = 'Welcome Back';
      this.subtitle = 'Please enter your details to sign in';
      this.buttonText = 'Sign In';
    } else {
      this.title = 'Create Account';
      this.subtitle = 'Fill in your details to get started';
      this.buttonText = 'Sign Up';
    }
  }

  private passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
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
  console.log('Login attempt with:', { username, password: password});
  
  this.authService.login(username, password).subscribe({
    next: (user) => {
      console.log('Login successful, user:', user);
      this.isLoading = false;
      console.log("Logged in");
      this.router.navigate(['/stores']); // Redirect to home or desired route
    },
    error: (error) => {
      console.error('Login failed:', error);
      this.isLoading = false;
      this.errorMessage = error.message || 'Login failed. Please try again.';
    }
  });
}

  private register(): void {
    if (this.formGroup.hasError('passwordMismatch')) {
      this.errorMessage = 'Passwords do not match';
      this.isLoading = false;
      return;
    }

    this.authService.register(this.formGroup.value).subscribe({
      next: (user) => {
        this.isLoading = false;
        // Optionally auto-login after registration
        this.router.navigate(['/stores'], { 
          queryParams: { registered: true } 
        });
      },
      error: (error) => {
        this.isLoading = false;
        this.errorMessage = error.message || 'Registration failed. Please try again.';
      }
    });
  }

  private markFormGroupTouched(): void {
    Object.keys(this.formGroup.controls).forEach(key => {
      this.formGroup.get(key)?.markAsTouched();
    });
  }

  passwordVisibility: { [key: string]: boolean } = {
    'password': false,
    'reg-password': false,
    'confirmPassword': false
  };

  // Add this method to your NewAuthComponent class
togglePasswordVisibility(fieldId: string): void {
  this.passwordVisibility[fieldId] = !this.passwordVisibility[fieldId];
  
  const passwordInput = document.getElementById(fieldId) as HTMLInputElement;
  if (passwordInput) {
    passwordInput.type = this.passwordVisibility[fieldId] ? 'text' : 'password';
  }
  
  // Update the eye icon
  const toggleButton = passwordInput?.nextElementSibling as HTMLButtonElement;
  const icon = toggleButton?.querySelector('i');
  if (icon) {
    icon.className = this.passwordVisibility[fieldId] ? 'fas fa-eye-slash' : 'fas fa-eye';
  }

}}
 


