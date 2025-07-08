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
      password: ['', [Validators.required, Validators.minLength(6)]],
    });

    this.signupForm = this.fb.group({
      fullName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', Validators.required],
    }, {
      validators: this.passwordMatchValidator
    });
  }

  switchMode(isLogin: boolean) {
    this.isLoginMode = isLogin;
  }

  onLogin() {
    if (this.loginForm.valid) {
      // TODO: Implement login logic here (call AuthService, etc.)
      console.log('Login form value:', this.loginForm.value);
    }
  }

  onSignup() {
    if (this.signupForm.valid) {
      // TODO: Implement signup logic here (call AuthService, etc.)
      console.log('Signup form value:', this.signupForm.value);
    }
  }

  passwordMatchValidator(g: FormGroup) {
    return g.get('password')?.value === g.get('confirmPassword')?.value
      ? null : { mismatch: true };
  }
}
