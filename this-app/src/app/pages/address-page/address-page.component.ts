import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { AddressService } from '../../services/address-service/address.service';
import { Address } from '../../models/address-model/address';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-address-page',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule],
  templateUrl: './address-page.component.html',
  styleUrls: ['./address-page.component.scss']
})
export class AddressPageComponent implements OnInit {
  addressForm: FormGroup;
  addresses: Address[] = [];

  constructor(private fb: FormBuilder, private addressService: AddressService) {
    this.addressForm = this.fb.group({
      addressLine1: ['', Validators.required],
      addressLine2: [''],
      addressLine3: [''],
      city: ['', Validators.required],
      province: ['', Validators.required],
      postalCode: ['', Validators.required],
      addressType: ['SHIPPING', Validators.required],
      isDefault: [false]
    });
  }

  ngOnInit(): void {
    this.loadAddresses();
  }

  loadAddresses(): void {
   this.addressService.getUserAddresses().subscribe({
    next: (data: Address[]) => {
      this.addresses = data;
    },
    error: (error) => {
      console.error('Error loading addresses:', error);
    }
  });
  }

  onSubmit(): void {
    if (this.addressForm.valid) {
    this.addressService.createAddress(this.addressForm.value).subscribe({
      next: () => {
        this.loadAddresses();
        console.log('Address added successfully');
        this.addressForm.reset();
      },
      error: (error) => {
        console.error('Error creating address:', error);
        // Display error to user
      }
    });
  }
  }

  deleteAddress(addressId: number): void {
    this.addressService.deleteAddress(addressId).subscribe({
    next: () => {
      this.loadAddresses();
      console.log('Address deleted successfully');
    },
    error: (error) => {
      console.error('Error deleting address:', error);
    }
  });
  }
}
