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

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.signupForm = this.fb.group({
      fullName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', Validators.required],
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
      const { fullName, email, password } = this.signupForm.value;
      
      const userData = {
        fullName,
        email,
        password,
        username: fullName.toLowerCase().replace(/\s+/g, ''), // Simple username generation
      };

      this.authService.register(userData).subscribe({
        next: (user) => {
          console.log('Registration successful:', user);
          // Navigate to login page or directly log in the user
          this.router.navigate(['/login']);
        },
        error: (error) => {
          console.error('Registration failed:', error);
          // Handle registration error (show error message)
        }
      });
    }
  }
}