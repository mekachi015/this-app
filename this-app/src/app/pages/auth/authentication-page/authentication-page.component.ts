import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthFormComponent } from "../../../components/auth/auth-form/auth-form.component";
import { CommonModule } from '@angular/common';


@Component({
  selector: 'app-authentication-page',
  standalone: true,
  imports: [AuthFormComponent, CommonModule],
  templateUrl: './authentication-page.component.html',
  styleUrl: './authentication-page.component.scss'
})
export class AuthenticationPageComponent {
  isLoginMode = true;
  
  loginForm: FormGroup;
  signupForm: FormGroup;

  constructor(private fb: FormBuilder) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required]
    });

    this.signupForm = this.fb.group({
      fullName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', Validators.required]
    });
  }

  switchMode(loginMode: boolean) {
    this.isLoginMode = loginMode;
  }

  onLogin() {
    if (this.loginForm.valid) {
      console.log('Login form submitted', this.loginForm.value);
      // Add your authentication logic here
    }
  }

  onSignup() {
    if (this.signupForm.valid) {
      console.log('Signup form submitted', this.signupForm.value);
      // Add your registration logic here
    }
  }
}
