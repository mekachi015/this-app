import { CommonModule } from '@angular/common';
import { Component, Input, Output, EventEmitter} from '@angular/core';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';


@Component({
  selector: 'app-auth-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './auth-form.component.html',
  styleUrl: './auth-form.component.scss'
})
export class AuthFormComponent {

  @Input() title!: string;
  @Input() subtitle!: string;
  @Input() buttonText!: string;
  @Input() formGroup!: FormGroup;
  @Output() onSubmit = new EventEmitter<void>();
  @Output() formSubmit = new EventEmitter<void>();
  

  handleSubmit() {
    if (this.formGroup.valid) {
      this.onSubmit.emit();
    }
  }
}
