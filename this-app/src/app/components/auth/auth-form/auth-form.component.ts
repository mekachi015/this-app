import { CommonModule } from '@angular/common';
import { Component, Input, Output, EventEmitter} from '@angular/core';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';


@Component({
  selector: 'app-auth-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './auth-form.component.html',
  styleUrl: './auth-form.component.scss'
})
export class AuthFormComponent {

  @Input() title: string = '';
  @Input() subtitle: string = '';
  @Input() buttonText: string = '';
  @Input() isLoginMode: boolean = true;  // Add this line
  @Input() formGroup!: FormGroup;
  @Output() onSubmit = new EventEmitter<void>();
  

  handleSubmit() {
    if (this.formGroup.valid) {
      this.onSubmit.emit();
    }
  }
}
