import { Component } from '@angular/core';
import { FormBuilder, Validators, FormGroup } from '@angular/forms';
import { AuthFormComponent } from "../auth-form/auth-form.component";
import { AuthService } from '../../../services/authentication-service/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [AuthFormComponent],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {
  loginForm: FormGroup;

  constructor(
    private fb: FormBuilder, 
    private authService: AuthService,
    private router: Router
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
    });
  }

  onLogin() {
    if (this.loginForm.valid) {
      const { email, password } = this.loginForm.value;
      
      this.authService.login(email, password).subscribe({
        next: (user) => {
          console.log('Login successful:', user);
          // Navigate to home page or dashboard
          this.router.navigate(['stores']);
        },
        error: (error) => {
          console.error('Login failed:', error);
          // Handle login error (show error message)
        }
      });
    }
  }
}