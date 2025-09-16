import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';



@Component({
  selector: 'app-new-auth',
  standalone: true,
  imports: [CommonModule, RouterLink, FormGroup],
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
        email: ['', [Validators.required, Validators.email]],
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
    if (this.formGroup.invalid) {
      this.markFormGroupTouched();
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
    const { email, password } = this.formGroup.value;
    this.authService.login(email, password).subscribe({
      next: (user) => {
        this.isLoading = false;
        this.router.navigate(['/']); // Redirect to home or desired route
      },
      error: (error) => {
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
        this.router.navigate(['/login'], { 
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

}
