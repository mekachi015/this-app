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
  
}
