import { Component, OnInit } from '@angular/core';
import { FormBuilder, Validators, FormGroup } from '@angular/forms';
import { AuthFormComponent } from "../auth-form/auth-form.component";
import { AuthService } from '../../../services/authentication-service/auth.service';
import { Router, ActivatedRoute } from '@angular/router';
@Component({
  selector: 'app-login',
  standalone: true,
  imports: [AuthFormComponent],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent implements OnInit {
  loginForm: FormGroup;
  errorMessage: string = '';
  successMessage: string = '';
  isLoading = false;

  constructor(
    private fb: FormBuilder, 
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
    });
  }

  ngOnInit() {
    // Check for registration success message
    this.route.queryParams.subscribe(params => {
      if (params['registered'] === 'true') {
        this.successMessage = params['message'] || 'Registration successful! Please log in.';
        // Pre-fill email if provided
        if (params['email']) {
          this.loginForm.patchValue({ email: params['email'] });
        }
      }
    });

    // Clear any existing auth state
    if (this.authService.isLoggedIn()) {
      this.authService.logout();
    }
  }

  onLogin() {
    if (this.loginForm.valid) {
      this.errorMessage = '';
      this.successMessage = '';
      this.isLoading = true;
      
      const { email, password } = this.loginForm.value;
      
      this.authService.login(email, password).subscribe({
        next: (user) => {
          console.log('Login successful:', user);
          this.isLoading = false;
          
          // Navigate to stores page
          this.router.navigate(['/stores']);
        },
        error: (error) => {
          console.error('Login failed:', error);
          this.isLoading = false;
          
          // Handle specific error cases
          if (error.status === 401) {
            this.errorMessage = 'Invalid email or password';
          } else if (error.status === 0) {
            this.errorMessage = 'Cannot connect to server. Please try again later.';
          } else {
            this.errorMessage = error.message || 'Login failed. Please check your credentials.';
          }
        }
      });
    } else {
      this.errorMessage = 'Please fill in all required fields correctly.';
    }
  }
}