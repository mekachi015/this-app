import { Component } from '@angular/core';

import { AuthFormComponent } from "../auth-form/auth-form.component";


@Component({
  selector: 'app-sign-up',
  standalone: true,
  imports: [AuthFormComponent],
  templateUrl: './sign-up.component.html',
  styleUrl: './sign-up.component.scss'
})
export class SignUpComponent {
  
}