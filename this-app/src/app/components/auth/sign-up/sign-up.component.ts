import { Component } from '@angular/core';
import { FormBuilder, Validators, FormGroup} from '@angular/forms';
import { AuthFormComponent } from "../auth-form/auth-form.component";

@Component({
  selector: 'app-sign-up',
  standalone: true,
  imports: [AuthFormComponent],
  templateUrl: './sign-up.component.html',
  styleUrl: './sign-up.component.scss'
})
export class SignUpComponent {

    signupForm: FormGroup;
  
    constructor(private fb: FormBuilder) {
      this.signupForm = this.fb.group({
        fullName: ['', Validators.required],
        email: ['', [Validators.required, Validators.email]],
        password: ['', [Validators.required, Validators.minLength(6)]],
        confirmPassword: ['', Validators.required],
      });
    }
  
    onSignup() {
      if (this.signupForm.valid) {
        console.log('Signup form submitted:', this.signupForm.value);
      }
    }

}
